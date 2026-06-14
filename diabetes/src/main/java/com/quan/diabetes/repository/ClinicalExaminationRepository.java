package com.quan.diabetes.repository;

import com.quan.diabetes.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import com.quan.diabetes.entity.ClinicalExamination;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ClinicalExaminationRepository extends JpaRepository<ClinicalExamination, String> {
    @Query("SELECT p FROM ClinicalExamination ce "
            + "JOIN ce.patient p "
            + "WHERE ce.clinicalExamId = :clinicalExamId")
    Patient findPatientByClinicalExamId(@Param("clinicalExamId") String clinicalExamId);
}

