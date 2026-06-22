package com.quan.diabetes.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.quan.diabetes.entity.TreatmentPlan;
import java.util.Optional;

public interface TreatmentPlanRepository extends JpaRepository<TreatmentPlan, Integer> {
    Optional<TreatmentPlan> findByClinicalExamination_ClinicalExamId(String clinicalExamId);
}
