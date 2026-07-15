package com.quan.diabetes.dto.doctor;

import jakarta.validation.constraints.Pattern;

/**
 * Form data for Tab 3: Clinical Diagnosis & Treatment Plan
 */
public class ExamStep3Form {

    private String nextAppointment;

    // Treatment plan fields
    @Pattern(regexp = "^$|.*\\S.*", message = "Mục tiêu điều trị không được chỉ chứa khoảng trắng")
    private String treatmentGoal;
    
    @Pattern(regexp = "^$|.*\\S.*", message = "Chế độ dinh dưỡng không được chỉ chứa khoảng trắng")
    private String dietPlan;
    
    @Pattern(regexp = "^$|.*\\S.*", message = "Chế độ tập luyện không được chỉ chứa khoảng trắng")
    private String exercisePlan;
    
    @Pattern(regexp = "^$|.*\\S.*", message = "Kế hoạch theo dõi đường huyết không được chỉ chứa khoảng trắng")
    private String glucoseMonitoringPlan;

    // ---- Getters & Setters ----

    public String getNextAppointment() { return nextAppointment; }
    public void setNextAppointment(String nextAppointment) { this.nextAppointment = nextAppointment; }

    public String getTreatmentGoal() { return treatmentGoal; }
    public void setTreatmentGoal(String treatmentGoal) { this.treatmentGoal = treatmentGoal; }

    public String getDietPlan() { return dietPlan; }
    public void setDietPlan(String dietPlan) { this.dietPlan = dietPlan; }

    public String getExercisePlan() { return exercisePlan; }
    public void setExercisePlan(String exercisePlan) { this.exercisePlan = exercisePlan; }

    public String getGlucoseMonitoringPlan() { return glucoseMonitoringPlan; }
    public void setGlucoseMonitoringPlan(String glucoseMonitoringPlan) { this.glucoseMonitoringPlan = glucoseMonitoringPlan; }
}
