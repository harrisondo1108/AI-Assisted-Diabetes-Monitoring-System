package com.quan.diabetes.repository;

import com.quan.diabetes.entity.PrescriptionTiming;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PrescriptionTimingRepository
        extends JpaRepository<PrescriptionTiming, Long> {
    List<PrescriptionTiming>
    findByPrescriptionDetail_PrescriptionDetailId(
            String prescriptionDetailID
    );

    void deleteByPrescriptionDetail_PrescriptionDetailId(
            String prescriptionDetailID
    );

    boolean existsByPrescriptionDetail_PrescriptionDetailIdAndTiming_TimingID(
            String prescriptionDetailID,
            Integer timingID
    );
}
