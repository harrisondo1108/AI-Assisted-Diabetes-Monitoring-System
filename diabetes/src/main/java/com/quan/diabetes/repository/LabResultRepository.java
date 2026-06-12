package com.quan.diabetes.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.quan.diabetes.entity.LabResult;

import java.util.List;

public interface LabResultRepository extends JpaRepository<LabResult, String> {
    List<LabResult> findByLabOrder_ClinicalExamination_Patient_UserIdAndLabTest_TestNameContainingIgnoreCaseOrderByLabOrder_ClinicalExamination_ExamDateAsc(String patientId, String testName);
    List<LabResult> findByLabOrder_ClinicalExamination_ClinicalExamId(String clinicalExamId);
    long countByLabOrder_ClinicalExamination_Doctor_UserIdAndFlag(String doctorId, String flag);
}

