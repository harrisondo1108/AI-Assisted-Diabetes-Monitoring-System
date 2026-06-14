package com.quan.diabetes.service.AIService;


import com.quan.diabetes.dto.PrescriptionReminderDto;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import java.util.List;

public interface GroupReminderService {
    @SystemMessage("""
            Bạn là một y tá ảo hỗ trợ nhắc nhở bệnh nhân tiểu đường sử dụng thuốc.
            """)
    @UserMessage("""
            Bạn là một y tá ảo có nhiệm vụ nhắc lịch sử dụng thuốc cho bệnh nhân tên là {{name}} vào lúc {{time}}.
            
            Dưới đây là danh sách các loại thuốc họ cần uống:
            {{medicines}}
            
            Nhiệm vụ của bạn:
            1. Tổng hợp thông tin trên thành một tin nhắn nhắc nhở duy nhất, hành văn cực kỳ lịch sự, thân thiện và chuẩn mực y tế.
            2. Nên chào nên ngắn gọn lại tập trung vào việc nhắc nhở bệnh nhân uống thuốc và các plan(nếu có), và khi chào thì câu chào nên là "chào bệnh nhân " + tên của họ để tạo cảm giác gần gũi, thân thiện.
            3. Các loại thuốc phải được liệt kê rõ ràng dưới dạng danh sách gạch đầu dòng kèm theo liều lượng và lời dặn tương ứng. Không tự ý viết tắt.
            4. Luôn kết thúc bằng một lời chúc sức khỏe ngắn gọn, tốt đẹp.
            """)
    String generateGroupReminder(
            @V("name") String patientName,
            @V("time") String timeSlot,
            @V("medicines") List<PrescriptionReminderDto> medicineList
    );
}
