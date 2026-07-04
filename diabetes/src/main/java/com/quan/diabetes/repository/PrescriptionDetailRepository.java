package com.quan.diabetes.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.quan.diabetes.entity.PrescriptionDetail;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PrescriptionDetailRepository extends JpaRepository<PrescriptionDetail, String> {
    List<PrescriptionDetail> findByPrescription_PrescriptionId(String prescriptionId);

    @org.springframework.data.jpa.repository.Query("SELECT pd FROM PrescriptionDetail pd " +
           "JOIN FETCH pd.prescription p " +
           "JOIN FETCH p.clinicalExamination ce " +
           "JOIN FETCH pd.medication m " +
           "LEFT JOIN FETCH ce.treatmentPlan tp " +
           "LEFT JOIN FETCH pd.prescriptionTimings pt " +
           "LEFT JOIN FETCH pt.timing " +
           "WHERE ce.clinicalExamId = :clinicalExamId")
    List<PrescriptionDetail> findByClinicalExamIdWithDetails(@org.springframework.data.repository.query.Param("clinicalExamId") String clinicalExamId);
}

