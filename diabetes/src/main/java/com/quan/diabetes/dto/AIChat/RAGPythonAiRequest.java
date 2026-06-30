package com.quan.diabetes.dto.AIChat;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RAGPythonAiRequest {
    @JsonProperty("patient_id")
    private String patientId;

    private String message;

    @JsonProperty("context_data")
    private String contextData;

    public RAGPythonAiRequest() {
    }

    public RAGPythonAiRequest(String patientId, String message, String contextData) {
        this.patientId = patientId;
        this.message = message;
        this.contextData = contextData;
    }

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

    public String getContextData() {
        return contextData;
    }

    public void setContextData(String contextData) {
        this.contextData = contextData;
    }
}
