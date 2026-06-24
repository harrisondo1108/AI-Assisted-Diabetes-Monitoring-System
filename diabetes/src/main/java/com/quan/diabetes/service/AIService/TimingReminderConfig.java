package com.quan.diabetes.service.AIService;

import com.quan.diabetes.entity.PatientRoutine;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface TimingReminderConfig {
    @SystemMessage("""
            Bạn là một hệ thống đề xuất thời điểm gửi nhắc nhở uống thuốc cho bệnh nhân.
            """)
    @UserMessage("""
            Input:
            - {{time}}: mô tả thời điểm dùng thuốc (ví dụ: "08:00" hoặc "trước ăn sáng", "sau trưa", "trước ăn tối", "sáng", ...).
            - {{routine}}: đối tượng PatientRoutine có các trường thời gian: {{routine}}.breakfastTime, {{routine}}.lunchTime, {{routine}}.dinnerTime, {{routine}}.wakeUpTime, {{routine}}.sleepTime (tất cả ở định dạng "HH:mm").

            QUY TẮC RÕ RÀNG (BẮT BUỘC THỰC HIỆN):
            1) Mapping từ khóa sang trường trong {{routine}} (ưu tiên):
               - "sáng"/"ăn sáng"/"breakfast"  -> {{routine}}.breakfastTime
               - "trưa"/"ăn trưa"/"lunch"      -> {{routine}}.lunchTime
               - "tối"/"ăn tối"/"dinner"       -> {{routine}}.dinnerTime
               - "thức"/"wake"/"wakeup"        -> {{routine}}.wakeUpTime
               - "ngủ"/"sleep"                  -> {{routine}}.sleepTime

            2) Nếu {{time}} chứa từ chỉ "trước" (ví dụ "trước ăn sáng") -> lấy trường mapped từ (1) và TRỪ 30 phút.
            3) Nếu {{time}} chứa từ chỉ "sau" (ví dụ "sau trưa")   -> lấy trường mapped từ (1) và CỘNG 30 phút.
            4) Nếu {{time}} là một chuỗi chính xác ở định dạng "HH:mm" -> TRẢ VỀ CHÍNH XÁC CHUỖI ĐÓ (sau khi kiểm tra hợp lệ).

            QUY ĐỊNH QUAN TRỌNG VỀ KẾT QUẢ:
            - CHỈ trả về một chuỗi duy nhất ở định dạng 24 giờ "HH:mm" (ví dụ "07:30"). KHÔNG được thêm văn bản, ký tự, chú thích hay giải thích nào khác.
            - Nếu không thể xác định trường mapped từ (ví dụ {{time}} không chứa từ khóa nào) và {{time}} không phải "HH:mm", hãy cố gắng phân tích các trường trong {{routine}} ngay cả khi chúng là đối tượng LocalTime (ví dụ có các thuộc tính hour, minute) hoặc là chuỗi. Nếu vẫn không thể, TRẢ VỀ {{routine}}.breakfastTime (định dạng "HH:mm").

            VÍ DỤ (Input -> Output):
            - {{time}}="trước ăn sáng", {{routine}}.breakfastTime="07:00" -> "06:30"
            - {{time}}="sau trưa",    {{routine}}.lunchTime="11:00"     -> "11:30"
            - {{time}}="08:00",        {{routine}}.any                     -> "08:00"
            - {{time}}="khi thức dậy", {{routine}}.wakeUpTime="06:00"  -> "06:00" (nếu không có "trước" hoặc "sau", trả về trường mapped không thay đổi)

            KẾT LUẬN: TUÂN THỦ NGHIÊM NGẶT CÁC QUY TẮC TRÊN. CHỈ TRẢ VỀ MỘT CHUỖI HH:mm.
            """)
    String generateReminderTime(
            @V("time") String timeSlot,
            @V("routine") PatientRoutine patientRoutine
    );
}
