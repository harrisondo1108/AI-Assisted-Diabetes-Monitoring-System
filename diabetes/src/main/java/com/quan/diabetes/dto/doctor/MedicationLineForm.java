package com.quan.diabetes.dto.doctor;

import java.util.List;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;

/**
 * Form data for Tab 4: a single medication line to add/edit in the prescription.
 * The list of current prescription lines is stored in the HTTP session.
 */
public class MedicationLineForm {

    /** Medication ID from the catalog */
    private String medId;

    /** Dose quantity per intake */
    private Double dosagePerDose;

    /** Duration in days */
    private Integer duration;

    /** Start date (yyyy-MM-dd) */
    private String startDate;

    /** End date calculated by server; sent as hidden field for display purposes */
    private String endDate;

    /** Total quantity (calculated by server) */
    private Integer quantity;

    /** List of timing names (e.g. "Sáng", "Trưa", "Tối") */
    @NotEmpty(message = "Vui lòng chọn ít nhất một thời điểm sử dụng")
    private List<String> timing;

    /** Optional medication instructions */
    @Pattern(regexp = "^$|.*\\S.*", message = "Ghi chú cách dùng không được chỉ chứa khoảng trắng")
    private String medicationPlan;

    /**
     * Index into the session prescription list to edit, or -1 to add a new line.
     */
    private Integer editIndex;

    // ---- Getters & Setters ----

    public String getMedId() { return medId; }
    public void setMedId(String medId) { this.medId = medId; }

    public Double getDosagePerDose() { return dosagePerDose; }
    public void setDosagePerDose(Double dosagePerDose) { this.dosagePerDose = dosagePerDose; }

    public Integer getDuration() { return duration; }
    public void setDuration(Integer duration) { this.duration = duration; }

    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }

    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public List<String> getTiming() { return timing; }
    public void setTiming(List<String> timing) { this.timing = timing; }

    public String getMedicationPlan() { return medicationPlan; }
    public void setMedicationPlan(String medicationPlan) { this.medicationPlan = medicationPlan; }

    public Integer getEditIndex() { return editIndex; }
    public void setEditIndex(Integer editIndex) { this.editIndex = editIndex; }
}
