package com.quan.diabetes.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalTime;

@Entity
@Table(name = "PrescriptionDetail")
public class PrescriptionDetail {

    @Id
    @Column(name = "PrescriptionDetailID", length = 50)
    private String prescriptionDetailId;

    @ManyToOne
    @JoinColumn(name = "PrescriptionID")
    private Prescription prescription;

    @ManyToOne
    @JoinColumn(name = "MedicationID")
    private Medication medication;

    @Column(name = "Dosage", length = 50)
    private String dosage;

    @Column(name = "Timing")
    private LocalTime timing;

    @Column(name = "TotalQuantity")
    private int totalQuantity;

    @Column(name = "DurationDays")
    private int durationDays;

    public PrescriptionDetail() {
    }

    public String getPrescriptionDetailId() {
        return prescriptionDetailId;
    }

    public void setPrescriptionDetailId(String prescriptionDetailId) {
        this.prescriptionDetailId = prescriptionDetailId;
    }

    public Prescription getPrescription() {
        return prescription;
    }

    public void setPrescription(Prescription prescription) {
        this.prescription = prescription;
    }

    public Medication getMedication() {
        return medication;
    }

    public void setMedication(Medication medication) {
        this.medication = medication;
    }

    public String getDosage() {
        return dosage;
    }

    public void setDosage(String dosage) {
        this.dosage = dosage;
    }

    public LocalTime getTiming() {
        return timing;
    }

    public void setTiming(LocalTime timing) {
        this.timing = timing;
    }

    public int getTotalQuantity() {
        return totalQuantity;
    }

    public void setTotalQuantity(int totalQuantity) {
        this.totalQuantity = totalQuantity;
    }

    public int getDurationDays() {
        return durationDays;
    }

    public void setDurationDays(int durationDays) {
        this.durationDays = durationDays;
    }
}


