package com.quan.diabetes.service.ai;

public interface AiTool {
    String getGeneralRecord(String patientId);
    String getClinicalExamination(String patientId);
    String getTreatmentPlan(String patientId);
    String getLabResults(String patientId);
    String getPrescriptions(String patientId);
}
