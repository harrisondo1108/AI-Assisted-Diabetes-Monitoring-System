package com.quan.diabetes.service.exam;

import com.quan.diabetes.entity.TreatmentPlan;
import java.util.List;
import java.util.Optional;

public interface TreatmentPlanService {

    Optional<TreatmentPlan> findByClinicalExamId(String clinicalExamId);
}