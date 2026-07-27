package com.quan.diabetes.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;

@Entity
@Table(name = "ExamSymptom")
public class ExamSymptom {

    @EmbeddedId
    private ExamSymptomId id;

    @ManyToOne
    @MapsId("clinicalExamId")
    @JoinColumn(name = "ClinicalExamID")
    private ClinicalExamination clinicalExamination;

    @ManyToOne
    @MapsId("symptomId")
    @JoinColumn(name = "SymptomID")
    private SymptomsCatalog symptom;

    @Column(name = "Note", columnDefinition = "Nvarchar(255)")
    private String note;

    public ExamSymptom() {
    }

    public ExamSymptomId getId() {
        return id;
    }

    public void setId(ExamSymptomId id) {
        this.id = id;
    }

    public ClinicalExamination getClinicalExamination() {
        return clinicalExamination;
    }

    public void setClinicalExamination(ClinicalExamination clinicalExamination) {
        this.clinicalExamination = clinicalExamination;
    }

    public SymptomsCatalog getSymptom() {
        return symptom;
    }

    public void setSymptom(SymptomsCatalog symptom) {
        this.symptom = symptom;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}

