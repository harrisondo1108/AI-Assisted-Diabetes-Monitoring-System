package com.quan.diabetes.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "ClinicalExamination")
public class ClinicalExamination {

    @Id
    @Column(name = "ClinicalExamID", length = 50)
    private String clinicalExamId;

    @Column(name = "ExamDate")
    private LocalDateTime examDate;

    @Column(name = "MedicalHistory", columnDefinition = "NVARCHAR(MAX)")
    private String medicalHistory;

    @Column(name = "DiagnosisNote", columnDefinition = "NVARCHAR(MAX)")
    private String diagnosisNote;

    @Column(name = "NextAppointment")
    private LocalDateTime nextAppointment;

    @Column(name = "Status", length = 20, columnDefinition = "NVARCHAR(20)")
    private String status;

    @ManyToOne
    @JoinColumn(name = "PatientID", nullable = false)
    private Patient patient;

    @ManyToOne
    @JoinColumn(name = "DoctorID", nullable = false)
    private User doctor;

    @OneToOne(mappedBy = "clinicalExam", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private TreatmentPlan treatmentPlan;

    public ClinicalExamination() {
    }

    public String getClinicalExamId() {
        return clinicalExamId;
    }

    public void setClinicalExamId(String clinicalExamId) {
        this.clinicalExamId = clinicalExamId;
    }

    public LocalDateTime getExamDate() {
        return examDate;
    }

    public void setExamDate(LocalDateTime examDate) {
        this.examDate = examDate;
    }

    public String getMedicalHistory() {
        return medicalHistory;
    }

    public void setMedicalHistory(String medicalHistory) {
        this.medicalHistory = medicalHistory;
    }

    public String getDiagnosisNote() {
        return diagnosisNote;
    }

    public void setDiagnosisNote(String diagnosisNote) {
        this.diagnosisNote = diagnosisNote;
    }

    public LocalDateTime getNextAppointment() {
        return nextAppointment;
    }

    public void setNextAppointment(LocalDateTime nextAppointment) {
        this.nextAppointment = nextAppointment;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public User getDoctor() {
        return doctor;
    }

    public void setDoctor(User doctor) {
        this.doctor = doctor;
    }

    public TreatmentPlan getTreatmentPlan() {
        return treatmentPlan;
    }

    public void setTreatmentPlan(TreatmentPlan treatmentPlan) {
        this.treatmentPlan = treatmentPlan;
    }
}


