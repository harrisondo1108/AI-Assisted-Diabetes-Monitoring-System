package com.quan.diabetes.repository;

import com.quan.diabetes.entity.TreatmentPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TreatmentPlanRepository extends JpaRepository<TreatmentPlan, String> {
    Optional<TreatmentPlan> findByClinicalExamination_ClinicalExamId(String clinicalExamId);
}
