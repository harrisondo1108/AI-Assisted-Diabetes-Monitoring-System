package com.quan.diabetes.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "TreatmentPlan")
public class TreatmentPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PlanID")
    private Integer planId;

    @ManyToOne
    @JoinColumn(name = "ClinicalExamID", nullable = false)
    private ClinicalExamination clinicalExamination;

    @Column(name = "TreatmentGoal", columnDefinition = "NVARCHAR(MAX)")
    private String treatmentGoal;

    @Column(name = "DietPlan", columnDefinition = "NVARCHAR(MAX)")
    private String dietPlan;

    @Column(name = "ExercisePlan", columnDefinition = "NVARCHAR(MAX)")
    private String exercisePlan;

    @Column(name = "GlucoseMonitoringPlan", columnDefinition = "NVARCHAR(MAX)")
    private String glucoseMonitoringPlan;

    @Column(name = "CreatedAt", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    public TreatmentPlan() {
    }

    public Integer getPlanId() {
        return planId;
    }

    public void setPlanId(Integer planId) {
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
