package com.quan.diabetes.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "TreatmentPlan")
public class TreatmentPlan {

    @Id
    @Column(name = "PlanID", length = 50)
    private String planId;

    @OneToOne
    @JoinColumn(name = "ClinicalExamID", nullable = false)
    private ClinicalExamination clinicalExamination;

    @Column(name = "TreatmentGoal", length = 500, columnDefinition = "NVARCHAR(500)")
    private String treatmentGoal;

    @Column(name = "DietPlan", columnDefinition = "NVARCHAR(MAX)")
    private String dietPlan;

    @Column(name = "ExercisePlan", columnDefinition = "NVARCHAR(MAX)")
    private String exercisePlan;

    @Column(name = "GlucoseMonitoringPlan", columnDefinition = "NVARCHAR(MAX)")
    private String glucoseMonitoringPlan;

    @Column(name = "MedicationPlan", columnDefinition = "NVARCHAR(MAX)")
    private String medicationPlan;

    @Column(name = "CreatedAt")
    private LocalDateTime createdAt;

    public TreatmentPlan() {
    }

    public String getPlanId() {
        return planId;
    }

    public void setPlanId(String planId) {
        this.planId = planId;
    }

    public ClinicalExamination getClinicalExamination() {
        return clinicalExamination;
    }

    public void setClinicalExamination(ClinicalExamination clinicalExamination) {
        this.clinicalExamination = clinicalExamination;
    }

    public String getTreatmentGoal() {
        return treatmentGoal;
    }

    public void setTreatmentGoal(String treatmentGoal) {
        this.treatmentGoal = treatmentGoal;
    }

    public String getDietPlan() {
        return dietPlan;
    }

    public void setDietPlan(String dietPlan) {
        this.dietPlan = dietPlan;
    }

    public String getExercisePlan() {
        return exercisePlan;
    }

    public void setExercisePlan(String exercisePlan) {
        this.exercisePlan = exercisePlan;
    }

    public String getGlucoseMonitoringPlan() {
        return glucoseMonitoringPlan;
    }

    public void setGlucoseMonitoringPlan(String glucoseMonitoringPlan) {
        this.glucoseMonitoringPlan = glucoseMonitoringPlan;
    }

    public String getMedicationPlan() {
        return medicationPlan;
    }

    public void setMedicationPlan(String medicationPlan) {
        this.medicationPlan = medicationPlan;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
