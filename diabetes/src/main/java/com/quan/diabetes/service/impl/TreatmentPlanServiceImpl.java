package com.quan.diabetes.service.impl;

import com.quan.diabetes.entity.TreatmentPlan;
import com.quan.diabetes.repository.TreatmentPlanRepository;
import com.quan.diabetes.service.TreatmentPlanService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TreatmentPlanServiceImpl implements TreatmentPlanService {

    private final TreatmentPlanRepository treatmentPlanRepository;

    public TreatmentPlanServiceImpl(TreatmentPlanRepository treatmentPlanRepository) {
        this.treatmentPlanRepository = treatmentPlanRepository;
    }

    @Override
    public List<TreatmentPlan> findAll() {
        return treatmentPlanRepository.findAll();
    }

    @Override
    public Optional<TreatmentPlan> findById(Integer id) {
        return treatmentPlanRepository.findById(id);
    }

    @Override
    public Optional<TreatmentPlan> findByClinicalExamId(String clinicalExamId) {
        return treatmentPlanRepository.findByClinicalExamination_ClinicalExamId(clinicalExamId);
    }

    @Override
    public TreatmentPlan create(TreatmentPlan entity) {
        return treatmentPlanRepository.save(entity);
    }

    @Override
    public TreatmentPlan update(Integer id, TreatmentPlan entity) {
        if (!treatmentPlanRepository.existsById(id)) {
            throw new EntityNotFoundException("TreatmentPlan not found with id: " + id);
        }
        return treatmentPlanRepository.save(entity);
    }

    @Override
    public void deleteById(Integer id) {
        if (!treatmentPlanRepository.existsById(id)) {
            throw new EntityNotFoundException("TreatmentPlan not found with id: " + id);
        }
        treatmentPlanRepository.deleteById(id);
    }

    @Override
    public boolean existsById(Integer id) {
        return treatmentPlanRepository.existsById(id);
    }
}
