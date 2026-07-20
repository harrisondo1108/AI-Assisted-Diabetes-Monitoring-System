package com.quan.diabetes.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.quan.diabetes.entity.LabResult;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LabResultRepository extends JpaRepository<LabResult, String> {
    List<LabResult> findByLabOrder_ClinicalExamination_Patient_UserIdAndLabTest_TestNameContainingIgnoreCaseOrderByLabOrder_ClinicalExamination_ExamDateAsc(String patientId, String testName);
    List<LabResult> findByLabOrder_ClinicalExamination_ClinicalExamId(String clinicalExamId);
    long countByLabOrder_ClinicalExamination_Doctor_UserIdAndFlag(String doctorId, String flag);

    @org.springframework.data.jpa.repository.Query("SELECT DISTINCT lr FROM LabResult lr " +
           "JOIN FETCH lr.labTest lt " +
           "LEFT JOIN FETCH lr.labOrder lo " +
           "LEFT JOIN FETCH lo.clinicalExamination ce " +
           "LEFT JOIN FETCH ce.patient pat " +
           "WHERE pat.userId = :patientId ORDER BY ce.examDate DESC")
    List<LabResult> findByPatientIdWithDetails(@org.springframework.data.repository.query.Param("patientId") String patientId);
}

