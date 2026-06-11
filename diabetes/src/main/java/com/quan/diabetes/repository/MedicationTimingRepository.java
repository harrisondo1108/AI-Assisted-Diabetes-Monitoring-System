package com.quan.diabetes.repository;

import com.quan.diabetes.entity.MedicationTiming;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MedicationTimingRepository
        extends JpaRepository<MedicationTiming, Integer> {
    Optional<MedicationTiming> findByTimingName(String timingName);
}
