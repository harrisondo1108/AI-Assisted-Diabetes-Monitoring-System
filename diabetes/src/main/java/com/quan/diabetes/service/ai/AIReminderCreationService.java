package com.quan.diabetes.service.ai;


import com.quan.diabetes.dto.PrescriptionReminderDto;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import java.util.List;

public interface AIReminderCreationService {
    @SystemMessage("""
            Bạn là một y tá ảo chuyên nghiệp, tận tâm và thân thiện, chuyên hỗ trợ chăm sóc bệnh nhân tiểu đường.
            Nhiệm vụ của bạn là tổng hợp dữ liệu y tế thô được cung cấp thành một tin nhắn nhắc nhở sử dụng thuốc lịch sự và dễ hiểu.

            QUY TẮC BẮT BUỘC KHÔNG ĐƯỢC VI PHẠM:
            1. Câu chào đầu tiên PHẢI viết chính xác theo cấu trúc: "Chào bệnh nhân [Tên bệnh nhân]". (Thay [Tên bệnh nhân] bằng giá trị được cung cấp, không tự ý đổi cách xưng hô).
            2. Liệt kê danh sách thuốc rõ ràng bằng các dấu gạch đầu dòng (-). Giữ nguyên tên thuốc (viết hoa chữ cái đầu tiên), liều lượng (phải rõ ràng, ví dụ: bao nhiêu viên, đơn vị ,...) và lời dặn chi tiết, tuyệt đối không tự ý viết tắt tên thuốc.
            3. Nếu trong dữ liệu có thông tin về phác đồ tổng thể (chế độ dinh dưỡng, tập luyện, theo dõi đường huyết), hãy tóm tắt ngắn gọn thành một mục riêng gọi là "Kế hoạch phối hợp hôm nay" để nhắc nhở bệnh nhân thực hiện.
            4. Hành văn ngắn gọn, súc tích, tập trung hoàn toàn vào việc y tế, không dông dài.
            5. Luôn kết thúc bằng một lời chúc sức khỏe ngắn gọn và ấm áp.
            """)
    @UserMessage("""
            Hãy tạo tin nhắn nhắc nhở uống thuốc dựa trên dữ liệu y tế thô dưới đây:
            
            === DỮ LIỆU ĐẦU VÀO ===
            - Tên bệnh nhân: {{name}}
            - Khung giờ nhắc nhở: {{time}}
            - Chi tiết đơn thuốc và phác đồ đi kèm:
            {{medicines}}
            =======================
            
            Tin nhắn nhắc nhở hoàn chỉnh (viết bằng tiếng Việt):
            """)
    String generateGroupReminder(
            @V("name") String patientName,
            @V("time") String timeSlot,
            @V("medicines") List<PrescriptionReminderDto> medicineList
    );
}
