package com.quan.diabetes.dto.reminder;

import com.quan.diabetes.entity.TreatmentPlan;

import java.time.LocalDate;

/**
 * DTO used to describe medication reminders for a clinical examination (session).
 */
public class PrescriptionReminderDto {
    @com.fasterxml.jackson.annotation.JsonIgnore
    private String patientId;
    @com.fasterxml.jackson.annotation.JsonIgnore
    private String clinicalExamId;
    @com.fasterxml.jackson.annotation.JsonProperty("Tên thuốc")
    private String medicationName;
    @com.fasterxml.jackson.annotation.JsonProperty("Liều lượng")
    private String dosage;
    @com.fasterxml.jackson.annotation.JsonProperty("Ngày bắt đầu uống")
    private LocalDate startDate;
    @com.fasterxml.jackson.annotation.JsonProperty("Ngày kết thúc uống")
    private LocalDate endDate;
    @com.fasterxml.jackson.annotation.JsonProperty("Dạng thuốc")
    private String form;
    @com.fasterxml.jackson.annotation.JsonProperty("Đường dùng (cách dùng)")
    private String administrationRoute;
    @com.fasterxml.jackson.annotation.JsonProperty("Hướng dẫn sử dụng")
    private String usageInstruction;
    @com.fasterxml.jackson.annotation.JsonProperty("Thời điểm uống trong ngày")
    private String timingName;
    @com.fasterxml.jackson.annotation.JsonProperty("Kế hoạch dùng thuốc")
    private String medicationPlan;
    @com.fasterxml.jackson.annotation.JsonIgnore
    private TreatmentPlan treatmentPlan;

    // Constructor used by JPQL new expression


    public PrescriptionReminderDto(String patientId, String clinicalExamId, String medicationName, String dosage, LocalDate startDate, LocalDate endDate, String form, String administrationRoute, String usageInstruction, String timingName, String medicationPlan, TreatmentPlan treatmentPlan) {
        this.patientId = patientId;
        this.clinicalExamId = clinicalExamId;
        this.medicationName = medicationName;
        this.dosage = dosage;
        this.startDate = startDate;
        this.endDate = endDate;
        this.form = form;
        this.administrationRoute = administrationRoute;
        this.usageInstruction = usageInstruction;
        this.timingName = timingName;
        this.medicationPlan = medicationPlan;
        this.treatmentPlan = treatmentPlan;
    }

    // Getters and setters
    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }

    public String getClinicalExamId() { return clinicalExamId; }
    public void setClinicalExamId(String clinicalExamId) { this.clinicalExamId = clinicalExamId; }

    public String getMedicationName() { return medicationName; }
    public void setMedicationName(String medicationName) { this.medicationName = medicationName; }

    public String getDosage() { return dosage; }
    public void setDosage(String dosage) { this.dosage = dosage; }

    public String getForm() { return form; }
    public void setForm(String form) { this.form = form; }

    public String getAdministrationRoute() { return administrationRoute; }
    public void setAdministrationRoute(String administrationRoute) { this.administrationRoute = administrationRoute; }

    public String getUsageInstruction() { return usageInstruction; }
    public void setUsageInstruction(String usageInstruction) { this.usageInstruction = usageInstruction; }

    public String getTimingName() { return timingName; }
    public void setTimingName(String timingName) { this.timingName = timingName; }

    public String getMedicationPlan() { return medicationPlan; }
    public void setMedicationPlan(String medicationPlan) { this.medicationPlan = medicationPlan; }

    public TreatmentPlan getTreatmentPlan() { return treatmentPlan; }
    public void setTreatmentPlan(TreatmentPlan treatmentPlan) { this.treatmentPlan = treatmentPlan; }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        // 2. Thông tin chính về thuốc
        sb.append("  + Thuốc: ").append(medicationName != null && !medicationName.isBlank() ? medicationName : "Không rõ tên thuốc");
        sb.append(" | Liều lượng: ").append(dosage != null && !dosage.isBlank() ? dosage : "Theo chỉ định");

        // 3. Dạng bào chế và Đường dùng
        boolean hasForm = (form != null && !form.isBlank());
        boolean hasRoute = (administrationRoute != null && !administrationRoute.isBlank());
        if (hasForm || hasRoute) {
            sb.append(" (");
            if (hasForm) sb.append("Dạng thuốc: ").append(form);
            if (hasForm && hasRoute) sb.append(" - ");
            if (hasRoute) sb.append("Cách dùng: ").append(administrationRoute);
            sb.append(")");
        }

        // 4. Khung giờ và Lời dặn từ bác sĩ
        if (timingName != null && !timingName.isBlank()) {
            sb.append("\n    * Thời điểm dùng thuốc trong ngày: ").append(timingName);
        }
        if (usageInstruction != null && !usageInstruction.isBlank()) {
            sb.append("\n    * Cách sử dụng mặc định của thuốc: ").append(usageInstruction);
        }

        // 6. Kế hoạch và Phác đồ điều trị tổng thể
        if (medicationPlan != null && !medicationPlan.isBlank()) {
            sb.append("\n    * Kế hoạch sử dụng: ").append(medicationPlan);
        }
        if (treatmentPlan != null) {
            // AI sẽ đọc được thông tin tổng thể của phác đồ từ Object này (LangChain4j sẽ gọi toString của TreatmentPlan)
            sb.append("\n    * Thuộc phác đồ tổng thể: \n").append(treatmentPlan.toString());
        }

        return sb.toString();
    }
}


