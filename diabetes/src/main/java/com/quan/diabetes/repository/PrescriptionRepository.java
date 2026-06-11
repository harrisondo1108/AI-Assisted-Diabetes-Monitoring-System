package com.quan.diabetes.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.quan.diabetes.entity.Prescription;

import java.util.Optional;

public interface PrescriptionRepository extends JpaRepository<Prescription, String> {
    Optional<Prescription> findByClinicalExamination_ClinicalExamId(String clinicalExamId);
    void deleteByClinicalExamination_ClinicalExamId(String clinicalExamId);
}

