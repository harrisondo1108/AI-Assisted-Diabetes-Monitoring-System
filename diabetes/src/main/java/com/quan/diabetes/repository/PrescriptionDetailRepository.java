package com.quan.diabetes.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.quan.diabetes.entity.PrescriptionDetail;

import java.util.List;

public interface PrescriptionDetailRepository extends JpaRepository<PrescriptionDetail, String> {
    List<PrescriptionDetail> findByPrescription_PrescriptionId(String prescriptionId);
}

