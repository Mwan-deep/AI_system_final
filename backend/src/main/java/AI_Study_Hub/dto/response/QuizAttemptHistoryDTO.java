package AI_Study_Hub.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class QuizAttemptHistoryDTO {
    private Long attemptId;
    private Long quizId;
    private String quizTitle;      // Tên bài thi đã làm
    private Double score;          // Bao nhiêu điểm
    private Integer totalWrong;    // Bao nhiêu câu sai
    private LocalDateTime attemptedAt; // Làm khi nào
    private String timeTaken;      // Thời gian làm là bao lâu
}