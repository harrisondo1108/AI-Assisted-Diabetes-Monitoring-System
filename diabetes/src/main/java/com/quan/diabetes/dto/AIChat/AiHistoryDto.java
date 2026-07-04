package com.quan.diabetes.dto.AIChat;

public class AiHistoryDto {
    private String role; // "user" or "ai"
    private String message;

    public AiHistoryDto() {
    }

    public AiHistoryDto(String role, String message) {
        this.role = role;
        this.message = message;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "AiHistoryDto{" +
                "role='" + role + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}
