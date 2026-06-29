package com.quan.diabetes;
import com.quan.diabetes.dto.AiChatRequest;
import com.quan.diabetes.dto.AiChatResponse;
import com.quan.diabetes.service.AiService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.Scanner;

@SpringBootApplication
public class RAGAiChatTest {
    public static void main(String[] args) {
        SpringApplication.run(RAGAiChatTest.class, args);
    }

    // Bean này sẽ tự động chạy ngay sau khi Spring Boot khởi động xong
    @Bean
    public CommandLineRunner testChatbot(AiService aiService) {
        return args -> {
            Scanner scanner = new Scanner(System.in);

            System.out.println("\n=========================================================");
            System.out.println("🤖 HỆ THỐNG BÁC SĨ AI (DIABETES) ĐÃ KHỞI ĐỘNG THÀNH CÔNG!");
            System.out.println("💡 Gõ câu hỏi của bạn và ấn Enter (Gõ 'thoat' để dừng)");
            System.out.println("=========================================================\n");

            // Giả lập ID bệnh nhân tiểu đường trong DB
            String mockPatientId = "P607261";

            while (true) {
                System.out.print("👨‍⚕️ Bệnh nhân: ");
                String userInput = scanner.nextLine();

                if (userInput.trim().equalsIgnoreCase("thoat")) {
                    System.out.println("Tắt hệ thống AI. Tạm biệt!");
                    break;
                }

                if (userInput.trim().isEmpty()) {
                    continue;
                }

                System.out.println("⏳ Bác sĩ đang suy nghĩ và tra cứu dữ liệu...");

                // 1. Đóng gói request chuẩn theo DTO của bạn
                AiChatRequest request = new AiChatRequest();
                request.setPatientId(mockPatientId);
                request.setMessage(userInput);

                // 2. Chạy qua logic RAG trong AiService
                AiChatResponse response = aiService.processChat(request);

                // 3. Xử lý và hiển thị kết quả
                if ("SUCCESS".equals(response.getStatus())) {
                    // Lưu ý: Đổi .getMessage() thành .getContent() nếu trong DTO của bạn đặt tên biến là content
                    System.out.println("\n🤖 Bác sĩ AI: " + response.getContent());
                } else {
                    System.out.println("\n❌ LỖI: " + response.getContent());
                }

                System.out.println("---------------------------------------------------------");
            }

            scanner.close();
        };
    }
}
