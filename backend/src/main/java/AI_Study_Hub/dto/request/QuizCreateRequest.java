package AI_Study_Hub.dto.request;

import lombok.Data;
import java.util.List;

@Data
public class QuizCreateRequest {
    private String title;
    private int durationMinutes;
    private Double passScore;
    private List<Long> questionIds;

    // THÊM TRƯỜNG NÀY: Chấp nhận 2 giá trị "PUBLIC" hoặc "PRIVATE"
    private String visibility;
}