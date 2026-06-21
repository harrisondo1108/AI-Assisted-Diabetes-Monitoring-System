package com.quan.diabetes.service.notification;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class SmsServiceTest {
    @Autowired
    private SmsService smsService; // Spring Boot tự động inject và nạp đủ cấu hình cho bạn
    @Test
    void sendSms() {
        System.out.println("--- BẮT ĐẦU CHẠY INTEGRATION TEST CHO SMS SERVICE ---");

        String phoneToReceive = "0328938692";
        String content = "hello từ class SmsService chạy bằng Spring Boot Test!";

        // Gọi trực tiếp hàm gửi tin nhắn của Service để kiểm tra
        boolean isSuccess = smsService.sendSms(phoneToReceive, content);

        if (isSuccess) {
            System.out.println("🎉 KẾT QUẢ TEST: Class SmsService của bạn đã chạy thành công!");
        } else {
            System.out.println("❌ KẾT QUẢ TEST: Thất bại. Vui lòng kiểm tra lại log lỗi.");
        }
    }
}