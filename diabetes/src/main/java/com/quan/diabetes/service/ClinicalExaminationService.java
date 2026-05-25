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
}
