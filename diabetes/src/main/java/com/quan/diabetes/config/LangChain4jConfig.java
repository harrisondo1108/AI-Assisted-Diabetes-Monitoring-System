package com.quan.diabetes.config;


import com.quan.diabetes.service.AIService.GroupReminderService;
import com.quan.diabetes.service.AIService.TimingReminderConfig;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.service.AiServices;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.time.Duration;

@Configuration
public class LangChain4jConfig {

    @Bean
    public ChatLanguageModel chatLanguageModel() {
        return OllamaChatModel.builder()
                .baseUrl("http://localhost:11434") // Cổng mặc định của Ollama local
                .modelName("qwen2.5:3b")          // Tên mô hình bạn đã cài bằng lệnh
                .temperature(0.2)                  // Giữ nhiệt độ thấp để câu từ chuẩn mực, không bịa đặt
                .timeout(Duration.ofSeconds(60))   // Đặt timeout 60s phòng trường hợp CPU của ThinkPad bị quá tải xử lý chậm
                .build();
    }

    @Bean
    public GroupReminderService groupReminderService(ChatLanguageModel chatLanguageModel) {
        // LangChain4j tự tạo code ngầm (Dynamic Proxy) dựa trên Interface bạn khai báo
        return AiServices.builder(GroupReminderService.class)
                .chatLanguageModel(chatLanguageModel)
                .build();
    }

//    @Bean
//    public TimingReminderConfig timingReminderConfig(ChatLanguageModel chatLanguageModel) {
//        // LangChain4j tự tạo code ngầm (Dynamic Proxy) dựa trên Interface bạn khai báo
//        return AiServices.builder(TimingReminderConfig.class)
//                .chatLanguageModel(chatLanguageModel)
//                .build();
//    }
}
