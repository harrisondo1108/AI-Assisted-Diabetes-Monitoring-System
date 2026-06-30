package com.quan.diabetes.dto.AIChat;

public class RAGAiChatRequest {
    private String patientId;
    private String message;

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
