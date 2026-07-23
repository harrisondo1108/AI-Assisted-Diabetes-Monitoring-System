package com.quan.diabetes.service.exam.impl;

import com.quan.diabetes.entity.TreatmentPlan;
import com.quan.diabetes.repository.TreatmentPlanRepository;
import com.quan.diabetes.service.exam.TreatmentPlanService;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class TreatmentPlanServiceImpl implements TreatmentPlanService {

    private final TreatmentPlanRepository treatmentPlanRepository;

    public TreatmentPlanServiceImpl(TreatmentPlanRepository treatmentPlanRepository) {
        this.treatmentPlanRepository = treatmentPlanRepository;
    }

    @Override
    public Optional<TreatmentPlan> findByClinicalExamId(String clinicalExamId) {
        return treatmentPlanRepository.findByClinicalExam_ClinicalExamId(clinicalExamId);
    }
}