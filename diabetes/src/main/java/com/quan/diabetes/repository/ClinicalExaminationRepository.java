package com.quan.diabetes.repository;

import com.quan.diabetes.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import com.quan.diabetes.entity.ClinicalExamination;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClinicalExaminationRepository extends JpaRepository<ClinicalExamination, String> {
    List<ClinicalExamination> findByDoctor_UserIdOrderByExamDateAsc(String doctorId);
    List<ClinicalExamination> findByPatient_UserIdOrderByExamDateDesc(String patientId);
    Optional<ClinicalExamination> findFirstByPatient_UserIdOrderByExamDateDesc(String patientId);
    Optional<ClinicalExamination> findFirstByPatient_UserIdAndDoctor_UserIdAndStatusIn(String patientId, String doctorId, List<String> statuses);
    Optional<ClinicalExamination> findFirstByDoctor_UserIdAndStatus(String doctorId, String status);
    @Query("SELECT p FROM ClinicalExamination ce "
            + "JOIN ce.patient p "
            + "WHERE ce.clinicalExamId = :clinicalExamId")
    Patient findPatientByClinicalExamId(@Param("clinicalExamId") String clinicalExamId);
}

