package com.quan.diabetes.service.medication;

import com.quan.diabetes.entity.PrescriptionTiming;

import java.util.List;

public interface PrescriptionTimingService {

    PrescriptionTiming save(PrescriptionTiming timing);

    PrescriptionTiming update(PrescriptionTiming timing);

    void deleteById(Long id);

    PrescriptionTiming findById(Long id);

    List<PrescriptionTiming> findAll();

    List<PrescriptionTiming>
    findByPrescriptionDetailId(String prescriptionDetailId);

    boolean exists(String prescriptionDetailId,
                   Integer timingId);
}
