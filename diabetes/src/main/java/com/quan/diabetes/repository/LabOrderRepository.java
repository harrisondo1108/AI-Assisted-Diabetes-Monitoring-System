package com.quan.diabetes.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.quan.diabetes.entity.LabOrder;

import java.util.Optional;

public interface LabOrderRepository extends JpaRepository<LabOrder, String> {
    Optional<LabOrder> findByClinicalExamination_ClinicalExamId(String clinicalExamId);
    void deleteByClinicalExamination_ClinicalExamId(String clinicalExamId);
}

