package AI_Study_Hub.dto.response;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class QuizResponseDTO {
    private Long quizId;
    private String title;
    private Integer quantity;
    private String duration; // Ví dụ: "15 phút"
    private List<QuestionDTO> questions;

    @Data
    @Builder
    public static class QuestionDTO {
        private Long questionId;
        private String questionText;
        private List<OptionDTO> options;
    }

    @Data
    @Builder
    public static class OptionDTO {
        private Long optionId;
        private String optionText;
        // TUYỆT ĐỐI KHÔNG CÓ TRƯỜNG "isCorrect" Ở ĐÂY
    }
}