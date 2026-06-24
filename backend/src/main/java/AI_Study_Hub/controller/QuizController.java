package AI_Study_Hub.controller;

import AI_Study_Hub.dto.response.QuizAttemptHistoryDTO;
import AI_Study_Hub.dto.response.QuizResponseDTO;
import AI_Study_Hub.dto.request.QuizSubmitRequest;
import AI_Study_Hub.dto.request.QuizCreateRequest;
import AI_Study_Hub.entity.Account;
import AI_Study_Hub.entity.Question;
import AI_Study_Hub.entity.Quiz;
import AI_Study_Hub.entity.QuizAttempt;
import AI_Study_Hub.repository.AccountRepository;
import AI_Study_Hub.service.QuizGeneratorService;
import AI_Study_Hub.service.QuizGradingService;
import AI_Study_Hub.service.QuizQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/quizzes")
@RequiredArgsConstructor
public class QuizController {

    private final QuizGeneratorService quizGeneratorService;
    private final QuizGradingService quizGradingService;
    private final QuizQueryService quizQueryService;
    private final AccountRepository accountRepository;

    // 1. API Nhờ AI sinh câu hỏi
    @PostMapping("/generate-questions")
    public ResponseEntity<?> generateQuestions(
            @RequestParam("materialId") Long materialId,
            @RequestParam(value = "quantity", defaultValue = "5") int quantity) {
        try {
            List<Question> generatedQuestions = quizGeneratorService.generateQuestionsOnly(materialId, quantity);
            return ResponseEntity.ok(generatedQuestions);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Lỗi sinh câu hỏi: " + e.getMessage());
        }
    }

    // 2. API Người dùng chốt cấu hình và tạo đề thi (Có thiết lập Public/Private)
    @PostMapping("/create")
    public ResponseEntity<?> createQuiz(@RequestBody QuizCreateRequest request) {
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            Account currentUser = accountRepository.findByUserName(username)
                    .orElseThrow(() -> new RuntimeException("Lỗi định danh"));

            Quiz quiz = quizGeneratorService.createCustomQuiz(currentUser.getAccountId(), request);

            Map<String, Object> response = new HashMap<>();
            response.put("quizId", quiz.getQuizId());
            response.put("title", quiz.getTitle());
            response.put("totalQuestions", quiz.getQuantity());
            response.put("visibility", quiz.getVisibility());
            response.put("message", "Đã tạo đề thi thành công!");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Lỗi tạo đề thi: " + e.getMessage());
        }
    }

    // 3. API Nộp bài và Chấm điểm
    @PostMapping("/submit")
    public ResponseEntity<?> submitAndGradeQuiz(@RequestBody QuizSubmitRequest request) {
        try {
            QuizAttempt attempt = quizGradingService.gradeQuiz(request);

            Map<String, Object> response = new HashMap<>();
            response.put("attemptId", attempt.getAttemptId());
            response.put("score", attempt.getScore());
            response.put("totalWrong", attempt.getTotalQuestionFalse());
            response.put("message", "Nộp bài và chấm điểm thành công!");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Lỗi chấm điểm: " + e.getMessage());
        }
    }

    // 4. API Frontend gọi để lấy đề thi về làm
    @GetMapping("/{quizId}/take")
    public ResponseEntity<?> getQuizForTaking(@PathVariable("quizId") Long quizId) {
        try {
            QuizResponseDTO quizData = quizQueryService.getQuizForTaking(quizId);
            return ResponseEntity.ok(quizData);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi truy xuất bài thi: " + e.getMessage());
        }
    }

    // 5. API Lấy danh sách bài thi của cá nhân (Thấy cả Public và Private)
    @GetMapping("/my-quizzes")
    public ResponseEntity<?> getMyQuizzes() {
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            Account currentUser = accountRepository.findByUserName(username)
                    .orElseThrow(() -> new RuntimeException("Lỗi định danh"));

            List<Quiz> quizzes = quizQueryService.getUserQuizzes(currentUser.getAccountId());
            return ResponseEntity.ok(quizzes);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Lỗi lấy danh sách bài thi cá nhân: " + e.getMessage());
        }
    }

    // 6. API Lấy toàn bộ bài thi PUBLIC trên hệ thống (Thư viện chung)
    @GetMapping("/all")
    public ResponseEntity<?> getPublicQuizzes() {
        try {
            List<Quiz> quizzes = quizQueryService.getPublicSystemQuizzes();
            return ResponseEntity.ok(quizzes);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Lỗi lấy danh sách bài thi hệ thống: " + e.getMessage());
        }
    }

    // 7. API Lấy lịch sử làm bài thi (Làm khi nào, bao nhiêu điểm...)
    @GetMapping("/history")
    public ResponseEntity<?> getMyQuizHistory() {
        try {
            // Định danh an toàn qua JWT Token
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            Account currentUser = accountRepository.findByUserName(username)
                    .orElseThrow(() -> new RuntimeException("Lỗi định danh"));

            // Gọi hàm truy xuất lịch sử
            List<QuizAttemptHistoryDTO> history = quizQueryService.getUserQuizHistory(currentUser.getAccountId());

            return ResponseEntity.ok(history);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Lỗi lấy lịch sử làm bài: " + e.getMessage());
        }
    }

    // 8. API Cập nhật trạng thái Công khai/Riêng tư của bài thi
    @PatchMapping("/{quizId}/visibility")
    public ResponseEntity<?> updateVisibility(
            @PathVariable("quizId") Long quizId,
            @RequestParam("status") String status) {
        try {
            // Định danh an toàn qua JWT Token
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            Account currentUser = accountRepository.findByUserName(username)
                    .orElseThrow(() -> new RuntimeException("Lỗi định danh"));

            // Gọi Service để xử lý cập nhật
            Quiz updatedQuiz = quizGeneratorService.updateQuizVisibility(quizId, currentUser.getAccountId(), status);

            Map<String, Object> response = new HashMap<>();
            response.put("quizId", updatedQuiz.getQuizId());
            response.put("title", updatedQuiz.getTitle());
            response.put("newVisibility", updatedQuiz.getVisibility());
            response.put("message", "Đã cập nhật trạng thái bài thi thành công!");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Lỗi cập nhật trạng thái: " + e.getMessage());
        }
    }
}