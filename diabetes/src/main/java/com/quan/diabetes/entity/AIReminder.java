package com.quan.diabetes.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "AI_Reminder")
public class AIReminder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "AIReminderID")
    private long aiReminderId;

    @Column(name = "Title", length = 50, columnDefinition = "NVARCHAR(50)")
    private String title;

    @Column(name = "Message", columnDefinition = "NVARCHAR(MAX)")
    private String message;

    @Column(name = "ScheduledTime")
    private LocalDateTime scheduledTime;

    @Column(name = "IsRead")
    private Boolean isRead;

    @Column(name = "IsSent")
    private Boolean isSent = false;

    @ManyToOne
    @JoinColumn(name = "AIAssistantID")
    private AIAssistant aiAssistant;

    @ManyToOne
    @JoinColumn(name = "PatientID")
    private Patient patient;

    @ManyToOne
    @JoinColumn(name = "TimingID")
    private MedicationTiming timing;

    public AIReminder() {
    }

    public long getAiReminderId() {
        return aiReminderId;
    }

    public void setAiReminderId(long aiReminderId) {
        this.aiReminderId = aiReminderId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getScheduledTime() {
        return scheduledTime;
    }

    public void setScheduledTime(LocalDateTime scheduledTime) {
        this.scheduledTime = scheduledTime;
    }

    public Boolean getIsRead() {
        return isRead;
    }

    public void setIsRead(Boolean isRead) {
        this.isRead = isRead;
    }

    public Boolean getIsSent() {
        return isSent;
    }

    public void setIsSent(Boolean isSent) {
        this.isSent = isSent;
    }

    public AIAssistant getAiAssistant() {
        return aiAssistant;
    }

    public void setAiAssistant(AIAssistant aiAssistant) {
        this.aiAssistant = aiAssistant;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public MedicationTiming getTiming() {
        return timing;
    }

    public void setTiming(MedicationTiming timing) {
        this.timing = timing;
    }
}



