package com.quan.diabetes.service;

import com.quan.diabetes.entity.MedicationTiming;

import java.util.List;
import java.util.Optional;

public interface MedicationTimingService {

    MedicationTiming save(MedicationTiming timing);

    MedicationTiming create(MedicationTiming timing);

    MedicationTiming update(MedicationTiming timing);

    MedicationTiming update(Integer id, MedicationTiming timing);

    void deleteById(Integer timingId);

    Optional<MedicationTiming> findById(Integer timingId);

    List<MedicationTiming> findAll();

    boolean existsByTimingName(String timingName);

    boolean existsByTimingNameAndTimingIdNot(String timingName, Integer timingId);
}
