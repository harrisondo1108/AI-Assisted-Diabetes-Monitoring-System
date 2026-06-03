package com.quan.diabetes.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "Medication")
public class Medication {

    @Id
    @Column(name = "MedicationID", length = 50)
    private String medicationId;

    @Column(name = "MedicationName", length = 100, unique = true, columnDefinition = "NVARCHAR(100)")
    private String medicationName;

    @Column(name = "Form", length = 50, columnDefinition = "NVARCHAR(50)")
    private String form;

    @Column(name = "Concentration", length = 50, columnDefinition = "NVARCHAR(50)")
    private String concentration;

    @Column(name = "AdministrationRoute", length = 50, columnDefinition = "NVARCHAR(50)")
    private String administrationRoute;

    @Column(name = "UsageInstruction", columnDefinition = "NVARCHAR(MAX)")
    private String usageInstruction;

    @Column(name = "Status", length = 20, columnDefinition = "VARCHAR(20) DEFAULT 'Active'")
    private String status = "Active";
    // active, locked

    public Medication() {
    }

    public String getMedicationId() {
        return medicationId;
    }

    public void setMedicationId(String medicationId) {
        this.medicationId = medicationId;
    }

    public String getMedicationName() {
        return medicationName;
    }

    public void setMedicationName(String medicationName) {
        this.medicationName = medicationName;
    }

    public String getForm() {
        return form;
    }

    public void setForm(String form) {
        this.form = form;
    }

    public String getConcentration() {
        return concentration;
    }

    public void setConcentration(String concentration) {
        this.concentration = concentration;
    }

    public String getAdministrationRoute() {
        return administrationRoute;
    }

    public void setAdministrationRoute(String administrationRoute) {
        this.administrationRoute = administrationRoute;
    }

    public String getUsageInstruction() {
        return usageInstruction;
    }

    public void setUsageInstruction(String usageInstruction) {
        this.usageInstruction = usageInstruction;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}