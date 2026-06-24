package AI_Study_Hub.service;

import AI_Study_Hub.dto.request.QuizCreateRequest;
import AI_Study_Hub.entity.*;
import AI_Study_Hub.repository.*;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class QuizGeneratorService {

    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;
    private final QuestionOptionRepository optionRepository;
    private final MaterialContextRepository contextRepository;
    private final AccountRepository accountRepository;
    private final StudyMaterialRepository materialRepository;
    private final GeminiService geminiService;

    // HÀM 1: AI SINH CÂU HỎI VÀ LƯU VÀO NGÂN HÀNG (Chưa tạo Quiz)
    @Transactional
    public List<Question> generateQuestionsOnly(Long materialId, int quantity) {

        MaterialContext context = contextRepository.findByStudyMaterial_MaterialId(materialId)
                .orElseThrow(() -> new RuntimeException("Tài liệu chưa được phân tích nội dung văn bản để sinh trắc nghiệm"));

        String prompt = "Dựa trên nội dung tài liệu học tập được cung cấp dưới đây, hãy tạo ra đúng " + quantity + " câu hỏi trắc nghiệm ôn tập. " +
                "Mỗi câu hỏi phải có từ 4 phương án lựa chọn, và chỉ có duy nhất một phương án đúng.\n\n" +
                "YÊU CẦU BẮT BUỘC: Bạn chỉ được phép trả về chuỗi định dạng JSON thuần túy theo cấu trúc mẫu sau, không được chứa bất kỳ từ giải thích nào ngoài khối JSON:\n" +
                "[\n" +
                "  {\n" +
                "    \"questionText\": \"Nội dung câu hỏi trắc nghiệm?\",\n" +
                "    \"options\": [\n" +
                "      {\"text\": \"Phương án A\", \"isCorrect\": true},\n" +
                "      {\"text\": \"Phương án B\", \"isCorrect\": false},\n" +
                "      {\"text\": \"Phương án C\", \"isCorrect\": false},\n" +
                "      {\"text\": \"Phương án D\", \"isCorrect\": false}\n" +
                "    ]\n" +
                "  }\n" +
                "]\n\n" +
                "--- NỘI DUNG TÀI LIỆU ---\n" + context.getExtractedText();

        String aiResponse = geminiService.chatWithGemini(prompt).trim();

        if (aiResponse.startsWith("```json")) {
            aiResponse = aiResponse.substring(7);
        }
        if (aiResponse.endsWith("```")) {
            aiResponse = aiResponse.substring(0, aiResponse.length() - 3);
        }
        aiResponse = aiResponse.trim();

        try {
            JsonArray questionsArray = JsonParser.parseString(aiResponse).getAsJsonArray();
            List<Question> savedQuestions = new ArrayList<>();

            for (JsonElement qElem : questionsArray) {
                JsonObject qObj = qElem.getAsJsonObject();
                String qText = qObj.get("questionText").getAsString();

                Question question = Question.builder()
                        .materialContext(context)
                        .questionText(qText)
                        .build();
                question = questionRepository.save(question);

                JsonArray optionsArray = qObj.getAsJsonArray("options");
                String correctText = "";

                for (JsonElement oElem : optionsArray) {
                    JsonObject oObj = oElem.getAsJsonObject();
                    String oText = oObj.get("text").getAsString();
                    boolean isCorrect = oObj.get("isCorrect").getAsBoolean();

                    QuestionOption option = QuestionOption.builder()
                            .question(question)
                            .optionText(oText)
                            .isCorrect(isCorrect)
                            .build();
                    optionRepository.save(option);

                    if (isCorrect) {
                        correctText = oText;
                    }
                }

                question.setCorrectAnswer(correctText);
                questionRepository.save(question);

                savedQuestions.add(question);
            }

            return savedQuestions;

        } catch (Exception e) {
            throw new RuntimeException("Lỗi phân tích cú pháp dữ liệu câu hỏi từ AI: " + e.getMessage() + ". Nội dung thô: " + aiResponse);
        }
    }

    // HÀM 2: NGƯỜI DÙNG CHỐT DANH SÁCH VÀ TẠO BÀI THI
    @Transactional
    public Quiz createCustomQuiz(Long accountId, QuizCreateRequest request) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản"));

        List<Question> selectedQuestions = questionRepository.findAllById(request.getQuestionIds());

        if (selectedQuestions.isEmpty()) {
            throw new RuntimeException("Bạn phải chọn ít nhất 1 câu hỏi để tạo bài thi!");
        }

        int hours = request.getDurationMinutes() / 60;
        int minutes = request.getDurationMinutes() % 60;
        LocalTime duration = LocalTime.of(hours, minutes);

        // Xử lý logic Visibility an toàn: Ép về PRIVATE nếu Frontend gửi rỗng hoặc sai
        String visibilityStatus = (request.getVisibility() != null && request.getVisibility().equalsIgnoreCase("PUBLIC"))
                ? "PUBLIC" : "PRIVATE";

        Quiz quiz = Quiz.builder()
                .account(account)
                .title(request.getTitle())
                .quantity(selectedQuestions.size())
                .duration(duration)
                .passScore(request.getPassScore())
                .visibility(visibilityStatus) // LƯU THUỘC TÍNH VISIBILITY VÀO DATABASE
                .questions(selectedQuestions)
                .build();

        return quizRepository.save(quiz);
    }

    // HÀM 3: CẬP NHẬT TRẠNG THÁI PUBLIC/PRIVATE SAU KHI ĐÃ TẠO
    @Transactional
    public Quiz updateQuizVisibility(Long quizId, Long accountId, String newVisibility) {
        // 1. Tìm bài thi trong hệ thống
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bài thi với ID: " + quizId));

        // 2. CHỐT CHẶN BẢO MẬT: Kiểm tra xem người yêu cầu đổi có phải là chủ nhân bài thi không?
        if (!quiz.getAccount().getAccountId().equals(accountId)) {
            throw new RuntimeException("Truy cập bị từ chối! Bạn không có quyền chỉnh sửa bài thi của người khác.");
        }

        // 3. Chuẩn hóa dữ liệu đầu vào (Chỉ nhận PUBLIC, còn lại ép về PRIVATE)
        String visibilityStatus = (newVisibility != null && newVisibility.equalsIgnoreCase("PUBLIC"))
                ? "PUBLIC" : "PRIVATE";

        // 4. Cập nhật và lưu lại
        quiz.setVisibility(visibilityStatus);
        return quizRepository.save(quiz);
    }
}