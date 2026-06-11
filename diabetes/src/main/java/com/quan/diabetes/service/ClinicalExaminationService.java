package com.quan.diabetes.service;

import com.quan.diabetes.entity.ClinicalExamination;
import java.util.List;
import java.util.Optional;

public interface ClinicalExaminationService {

    public List<ClinicalExamination> findAll();

    public Optional<ClinicalExamination> findById(String id);

    public ClinicalExamination create(ClinicalExamination entity);

    public ClinicalExamination update(String id, ClinicalExamination entity);

    public void deleteById(String id);

    public boolean existsById(String id);

    List<ClinicalExamination> findByDoctorId(String doctorId);

    List<ClinicalExamination> findByPatientId(String patientId);

    void submitExamination(String patientId, com.quan.diabetes.dto.ClinicalExamForm form, String doctorId);

    void cancelExamination(String patientId, String reason, String doctorId);

    void startExamination(String patientId, String doctorId);
}
