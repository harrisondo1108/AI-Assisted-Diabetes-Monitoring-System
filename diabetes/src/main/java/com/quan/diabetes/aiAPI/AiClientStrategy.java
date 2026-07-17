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
     * @param modelName Tên model (ví dụ: "diabetes-ai", "gemini-1.5-pro")
     * @return true nếu hỗ trợ, ngược lại false
     */
    boolean supportsModel(String modelName);
}
