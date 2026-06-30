package com.quan.diabetes.dto.AIChat;

public class RAGAiChatResponse {
    private String status;
    private String content;

    public RAGAiChatResponse() {}

    public RAGAiChatResponse(String status, String content) {
        this.status = status;
        this.content = content;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
