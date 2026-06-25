package com.quan.diabetes.service.medication;

import com.quan.diabetes.entity.MedicationTiming;

import java.util.List;

public interface MedicationTimingService {

    MedicationTiming save(MedicationTiming timing);

    MedicationTiming update(MedicationTiming timing);

    void deleteById(Integer timingId);

    MedicationTiming findById(Integer timingId);

    List<MedicationTiming> findAll();
}
