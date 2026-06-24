package AI_Study_Hub.dto.request;

import lombok.Data;
import java.util.Map;

@Data
public class QuizSubmitRequest {
    private Long accountId;
    private Long quizId;

    // Lưu danh sách câu trả lời theo dạng: Map<ID_Câu_Hỏi, ID_Phương_Án_Đã_Chọn>
    private Map<Long, Long> answers;
}