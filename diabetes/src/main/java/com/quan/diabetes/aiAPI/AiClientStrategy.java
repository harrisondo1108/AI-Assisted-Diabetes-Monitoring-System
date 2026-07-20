package com.quan.diabetes.aiAPI;

import com.quan.diabetes.aiAPI.dto.AiGenerateOptions;

/**
 * Strategy interface chuẩn hoá việc kết nối và sinh văn bản cho các nhà cung cấp AI (Ollama, Gemini, v.v.).
 */
public interface AiClientStrategy {

    /**
     * Thực hiện gửi yêu cầu sinh văn bản đến AI Service.
     *
     * @param prompt       Chuỗi câu hỏi hoặc nội dung prompt (đã bao gồm RAG data, lịch sử)
     * @param systemPrompt Chuỗi hướng dẫn hệ thống (system prompt)
     * @param options      Các tham số cấu hình sinh (nếu có, ví dụ model cụ thể, temperature, maxTokens)
     * @return Chuỗi phản hồi từ AI
     */
    String generate(String prompt, String systemPrompt, AiGenerateOptions options);

    /**
     * Thực hiện gửi yêu cầu sinh văn bản theo luồng (streaming) đến AI Service.
     *
     * @param prompt       Chuỗi câu hỏi hoặc nội dung prompt
     * @param systemPrompt Chuỗi hướng dẫn hệ thống
     * @param options      Các tham số cấu hình sinh
     * @param onChunk      Callback khi nhận được từng mảnh token/chunk
     * @param onComplete   Callback khi hoàn tất stream
     * @param onError      Callback khi có lỗi xảy ra
     */
    default void generateStream(String prompt, String systemPrompt, AiGenerateOptions options,
                                java.util.function.Consumer<String> onChunk,
                                Runnable onComplete,
                                java.util.function.Consumer<Throwable> onError) {
        try {
            String res = generate(prompt, systemPrompt, options);
            if (res != null && onChunk != null) {
                onChunk.accept(res);
            }
            if (onComplete != null) {
                onComplete.run();
            }
        } catch (Throwable t) {
            if (onError != null) {
                onError.accept(t);
            }
        }
    }

    /**
     * Mã định danh của nhà cung cấp (Ví dụ: "LOCAL_OLLAMA", "GEMINI").
     */
    String getProviderId();

    /**
     * Tên hiển thị thân thiện của nhà cung cấp.
     */
    String getDisplayName();

    /**
     * Kiểm tra client này có hỗ trợ xử lý model được yêu cầu hay không.
     *
     * @param modelName Tên model (ví dụ: "diabetes", "gemini-2.5-flash")
     * @return true nếu hỗ trợ, ngược lại false
     */
    boolean supportsModel(String modelName);
}
