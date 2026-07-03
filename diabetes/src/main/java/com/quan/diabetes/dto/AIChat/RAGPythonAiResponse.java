package com.quan.diabetes.dto.AIChat;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RAGPythonAiResponse {
    private String status;
    private String content;

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

    @Override
    public String toString() {
        return "RAGPythonAiResponse{" +
                "status='" + status + '\'' +
                ", content='" + content + '\'' +
                '}';
    }
}
