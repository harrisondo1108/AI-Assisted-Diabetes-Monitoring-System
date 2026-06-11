package com.quan.diabetes.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.quan.diabetes.entity.ClinicalExamination;

import java.util.List;
import java.util.Optional;

public interface ClinicalExaminationRepository extends JpaRepository<ClinicalExamination, String> {
    List<ClinicalExamination> findByDoctor_UserIdOrderByExamDateAsc(String doctorId);
    List<ClinicalExamination> findByPatient_UserIdOrderByExamDateDesc(String patientId);
    Optional<ClinicalExamination> findFirstByPatient_UserIdAndDoctor_UserIdAndStatusIn(String patientId, String doctorId, List<String> statuses);
}

