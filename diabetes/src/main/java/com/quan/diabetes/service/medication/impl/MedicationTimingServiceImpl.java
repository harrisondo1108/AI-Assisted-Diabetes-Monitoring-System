package com.quan.diabetes.service.medication.impl;

import com.quan.diabetes.entity.MedicationTiming;
import com.quan.diabetes.repository.MedicationTimingRepository;
import com.quan.diabetes.service.medication.MedicationTimingService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MedicationTimingServiceImpl
        implements MedicationTimingService {

    private final MedicationTimingRepository repository;

    public MedicationTimingServiceImpl(
            MedicationTimingRepository repository) {
        this.repository = repository;
    }

    @Override
    public MedicationTiming save(MedicationTiming timing) {
        return repository.save(timing);
    }

    @Override
    public MedicationTiming update(MedicationTiming timing) {
        return repository.save(timing);
    }

    @Override
    public void deleteById(Integer timingId) {
        repository.deleteById(timingId);
    }

    @Override
    public MedicationTiming findById(Integer timingId) {
        return repository.findById(timingId)
                .orElse(null);
    }

    @Override
    public List<MedicationTiming> findAll() {
        return repository.findAll();
    }
}