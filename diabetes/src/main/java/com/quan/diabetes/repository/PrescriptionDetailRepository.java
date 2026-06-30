package com.quan.diabetes.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.quan.diabetes.entity.PrescriptionDetail;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PrescriptionDetailRepository extends JpaRepository<PrescriptionDetail, String> {
    List<PrescriptionDetail> findByPrescription_PrescriptionId(String prescriptionId);
}

