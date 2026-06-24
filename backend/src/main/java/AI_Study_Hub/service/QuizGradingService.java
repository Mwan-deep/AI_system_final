package AI_Study_Hub.service;

import AI_Study_Hub.dto.request.QuizSubmitRequest;
import AI_Study_Hub.entity.Account;
import AI_Study_Hub.entity.QuestionOption;
import AI_Study_Hub.entity.Quiz;
import AI_Study_Hub.entity.QuizAttempt;
import AI_Study_Hub.repository.AccountRepository;
import AI_Study_Hub.repository.QuestionOptionRepository;
import AI_Study_Hub.repository.QuizAttemptRepository;
import AI_Study_Hub.repository.QuizRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class QuizGradingService {

    private final QuizRepository quizRepository;
    private final AccountRepository accountRepository;
    private final QuestionOptionRepository optionRepository;
    private final QuizAttemptRepository attemptRepository;

    @Transactional
    public QuizAttempt gradeQuiz(QuizSubmitRequest request) {
        Account account = accountRepository.findById(request.getAccountId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản"));
        Quiz quiz = quizRepository.findById(request.getQuizId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bài thi"));

        int totalQuestions = quiz.getQuestions().size();
        if (totalQuestions == 0) {
            throw new RuntimeException("Bài thi này chưa có câu hỏi nào!");
        }

        int correctCount = 0;

        // Chấm điểm: Duyệt qua từng câu trả lời của sinh viên
        for (Map.Entry<Long, Long> entry : request.getAnswers().entrySet()) {
            Long selectedOptionId = entry.getValue();

            // Tìm phương án sinh viên đã chọn trong DB
            QuestionOption selectedOption = optionRepository.findById(selectedOptionId).orElse(null);

            // Nếu phương án tồn tại và có cờ isCorrect = true -> Cộng điểm
            if (selectedOption != null && selectedOption.getIsCorrect()) {
                correctCount++;
            }
        }

        int wrongCount = totalQuestions - correctCount;

        // Tính điểm trên thang 10 (Làm tròn 2 chữ số thập phân)
        double score = (double) correctCount / totalQuestions * 10;
        score = Math.round(score * 100.0) / 100.0;

        // Lưu kết quả làm bài vào Database
        QuizAttempt attempt = QuizAttempt.builder()
                .quiz(quiz)
                .account(account)
                .score(score)
                .totalQuestionFalse(wrongCount)
                .build();

        return attemptRepository.save(attempt);
    }
}