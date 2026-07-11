package com.quan.diabetes.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "Reminder")
public class Reminder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ReminderID")
    private long reminderId;

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

    @ManyToOne
    @JoinColumn(name = "ClinicalExamID")
    private ClinicalExamination clinicalExamination;

    @Column(name = "LockStatus")
    private Boolean lockStatus = false;

    public Reminder() {
    }

    public long getReminderId() {
        return reminderId;
    }

    public void setReminderId(long reminderId) {
        this.reminderId = reminderId;
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

    public ClinicalExamination getClinicalExamination() {
        return clinicalExamination;
    }

    public void setClinicalExamination(ClinicalExamination clinicalExamination) {
        this.clinicalExamination = clinicalExamination;
    }

    public Boolean getLockStatus() {
        return lockStatus;
    }

    public void setLockStatus(Boolean lockStatus) {
        this.lockStatus = lockStatus;
    }
}
