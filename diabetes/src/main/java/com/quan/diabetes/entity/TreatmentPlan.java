package com.quan.diabetes.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "TreatmentPlan", schema = "dbo")
public class TreatmentPlan {

    @Id
    @Column(name = "PlanID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int planId;

    @Column(name = "CreatedAt", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "DietPlan", columnDefinition = "NVARCHAR(MAX)")
    private String dietPlan;

    @Column(name = "ExercisePlan", columnDefinition = "NVARCHAR(MAX)")
    private String exercisePlan;

    @Column(name = "GlucoseMonitoringPlan", columnDefinition = "NVARCHAR(MAX)")
    private String glucoseMonitoringPlan;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ClinicalExamID", referencedColumnName = "ClinicalExamID", nullable = false)
    private ClinicalExamination clinicalExam;

    /**
     * Hàm này sẽ tự động chạy ngay trước khi đối tượng được lưu vào database lần đầu tiên.
     */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // --- Constructors ---
    public TreatmentPlan() {
    }

    // --- Getters and Setters ---


    public int getPlanId() {
        return planId;
    }

    public void setPlanId(int planId) {
        this.planId = planId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    // Bạn có thể xóa hẳn hàm setCreatedAt nếu không muốn code bên ngoài thay đổi thời gian tạo lịch sử
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
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

    public ClinicalExamination getClinicalExam() {
        return clinicalExam;
    }

    public void setClinicalExam(ClinicalExamination clinicalExam) {
        this.clinicalExam = clinicalExam;
    }

    @Override
    public String toString() {
        return "TreatmentPlan{" +
                "planId=" + planId +
                ", createdAt=" + createdAt +
                ", dietPlan='" + dietPlan + '\'' +
                ", exercisePlan='" + exercisePlan + '\'' +
                ", glucoseMonitoringPlan='" + glucoseMonitoringPlan +
                '}';
    }
}
