package com.quan.diabetes.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.quan.diabetes.entity.Prescription;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PrescriptionRepository extends JpaRepository<Prescription, String> {
    Optional<Prescription> findByClinicalExamination_ClinicalExamId(String clinicalExamId);
    void deleteByClinicalExamination_ClinicalExamId(String clinicalExamId);
}

