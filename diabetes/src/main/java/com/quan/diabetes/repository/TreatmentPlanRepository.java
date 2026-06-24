package com.quan.diabetes.repository;

import com.quan.diabetes.entity.TreatmentPlan;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TreatmentPlanRepository extends JpaRepository<TreatmentPlan, Integer> {
}
