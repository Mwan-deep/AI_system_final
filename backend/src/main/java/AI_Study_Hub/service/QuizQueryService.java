package AI_Study_Hub.service;

import AI_Study_Hub.dto.response.QuizResponseDTO;
import AI_Study_Hub.entity.Question;
import AI_Study_Hub.entity.QuestionOption;
import AI_Study_Hub.entity.Quiz;
import AI_Study_Hub.repository.QuizRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import AI_Study_Hub.dto.response.QuizAttemptHistoryDTO; // <--- Import DTO mới
import AI_Study_Hub.entity.QuizAttempt; // <--- Import Entity
import AI_Study_Hub.repository.QuizAttemptRepository; // <--- Import Repo

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class QuizQueryService {

    private final QuizRepository quizRepository;
    private final QuizAttemptRepository quizAttemptRepository; // <--- THÊM DÒNG NÀY

    public QuizResponseDTO getQuizForTaking(Long quizId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bài thi"));

        List<QuizResponseDTO.QuestionDTO> questionDTOs = new ArrayList<>();

        for (Question question : quiz.getQuestions()) {

            List<QuizResponseDTO.OptionDTO> optionDTOs = new ArrayList<>();
            for (QuestionOption option : question.getQuestionOptions()) {
                optionDTOs.add(QuizResponseDTO.OptionDTO.builder()
                        .optionId(option.getOptionId())
                        .optionText(option.getOptionText())
                        .build());
            }

            questionDTOs.add(QuizResponseDTO.QuestionDTO.builder()
                    .questionId(question.getQuestionId())
                    .questionText(question.getQuestionText())
                    .options(optionDTOs)
                    .build());
        }

        return QuizResponseDTO.builder()
                .quizId(quiz.getQuizId())
                .title(quiz.getTitle())
                .quantity(quiz.getQuantity())
                .duration(quiz.getDuration() != null ? quiz.getDuration().getMinute() + " phút" : "Không giới hạn")
                .questions(questionDTOs)
                .build();
    }

    // HÀM MỚI: Lấy danh sách bài thi của riêng 1 user (Lấy cả Public và Private của họ)
    public List<Quiz> getUserQuizzes(Long accountId) {
        return quizRepository.findByAccount_AccountIdOrderByCreatedAtDesc(accountId);
    }

    // HÀM MỚI: Lấy toàn bộ bài thi PUBLIC trên hệ thống (Thư viện chung)
    public List<Quiz> getPublicSystemQuizzes() {
        return quizRepository.findByVisibilityOrderByCreatedAtDesc("PUBLIC");
    }

    // HÀM MỚI: Lấy lịch sử làm bài thi
    public List<QuizAttemptHistoryDTO> getUserQuizHistory(Long accountId) {
        // Lấy danh sách lịch sử từ DB
        List<QuizAttempt> attempts = quizAttemptRepository.findByAccount_AccountIdOrderByCreatedAtDesc(accountId);

        List<QuizAttemptHistoryDTO> historyList = new ArrayList<>();

        for (QuizAttempt attempt : attempts) {
            historyList.add(QuizAttemptHistoryDTO.builder()
                    .attemptId(attempt.getAttemptId())
                    .quizId(attempt.getQuiz().getQuizId())
                    .quizTitle(attempt.getQuiz().getTitle())
                    .score(attempt.getScore())
                    .totalWrong(attempt.getTotalQuestionFalse())
                    .attemptedAt(attempt.getCompletedAt())
                    // Nếu Entity của bạn có thuộc tính lưu thời gian làm, hãy thay bằng attempt.getTimeTaken()
                    .timeTaken("N/A")
                    .build());
        }

        return historyList;
    }
}