package com.quan.diabetes.service;

import com.quan.diabetes.entity.TreatmentPlan;
import java.util.List;
import java.util.Optional;

public interface TreatmentPlanService {

    List<TreatmentPlan> findAll();

    Optional<TreatmentPlan> findById(Integer id);

    Optional<TreatmentPlan> findByClinicalExamId(String clinicalExamId);

    TreatmentPlan create(TreatmentPlan entity);

    TreatmentPlan update(Integer id, TreatmentPlan entity);

    void deleteById(Integer id);

    boolean existsById(Integer id);
}
