package com.quan.diabetes.service.medication;

import com.quan.diabetes.entity.MedicationTiming;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
@Service
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

    List<MedicationTiming> searchByKeyword(String keyword);
}
