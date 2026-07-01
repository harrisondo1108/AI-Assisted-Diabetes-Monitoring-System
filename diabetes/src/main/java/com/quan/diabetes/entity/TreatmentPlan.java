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

    @Column(name = "TreatmentGoal",columnDefinition = "NVARCHAR(MAX)")
    private String treatmentGoal;

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

    public String getTreatmentGoal() {
        return treatmentGoal;
    }

    public void setTreatmentGoal(String treatmentGoal) {
        this.treatmentGoal = treatmentGoal;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        // 1. Chế độ ăn uống
        if (dietPlan != null && !dietPlan.isBlank()) {
            sb.append("\n       + Chế độ dinh dưỡng: ").append(dietPlan.trim());
        }

        // 2. Chế độ tập luyện
        if (exercisePlan != null && !exercisePlan.isBlank()) {
            sb.append("\n       + Chế độ tập luyện: ").append(exercisePlan.trim());
        }

        // 3. Kế hoạch theo dõi đường huyết (Cực kỳ quan trọng với bệnh nhân tiểu đường)
        if (glucoseMonitoringPlan != null && !glucoseMonitoringPlan.isBlank()) {
            sb.append("\n       + Kế hoạch theo dõi đường huyết: ").append(glucoseMonitoringPlan.trim());
        }

        return sb.toString();
    }
}
