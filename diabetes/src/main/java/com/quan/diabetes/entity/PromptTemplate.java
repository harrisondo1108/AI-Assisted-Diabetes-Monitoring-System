package com.quan.diabetes.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "PromptTemplate")
public class PromptTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "TemplateID")
    private Integer templateId;

    @Column(name = "TemplateName", nullable = false, length = 100, columnDefinition = "NVARCHAR(100)")
    private String templateName;

    @Column(name = "SystemPrompt", columnDefinition = "NVARCHAR(MAX)")
    private String systemPrompt;

    @Column(name = "IsActive")
    private Boolean isActive = true;   // DEFAULT 1

    @Column(name = "CreatedAt")
    private LocalDateTime createdAt;

    public PromptTemplate() {}

    // Getters and Setters
    public Integer getTemplateId() {
        return templateId;
    }

    public void setTemplateId(Integer templateId) {
        this.templateId = templateId;
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public String getSystemPrompt() {
        return systemPrompt;
    }

    public void setSystemPrompt(String systemPrompt) {
        this.systemPrompt = systemPrompt;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}