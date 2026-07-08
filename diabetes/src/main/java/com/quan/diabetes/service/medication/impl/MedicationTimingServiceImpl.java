package com.quan.diabetes.service.medication.impl;

import com.quan.diabetes.entity.MedicationTiming;
import com.quan.diabetes.repository.MedicationTimingRepository;
import jakarta.persistence.EntityNotFoundException;
import com.quan.diabetes.service.medication.MedicationTimingService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

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
    public MedicationTiming create(MedicationTiming timing) {
        return repository.save(timing);
    }

    @Override
    public MedicationTiming update(MedicationTiming timing) {
        return repository.save(timing);
    }

    @Override
    public MedicationTiming update(Integer id, MedicationTiming timing) {
        if (!repository.existsById(id)) {
            throw new EntityNotFoundException("MedicationTiming not found with id: " + id);
        }
        return repository.save(timing);
    }

    @Override
    public void deleteById(Integer timingId) {
        if (!repository.existsById(timingId)) {
            throw new EntityNotFoundException("MedicationTiming not found with id: " + timingId);
        }
        repository.deleteById(timingId);
    }

    @Override
    public Optional<MedicationTiming> findById(Integer timingId) {
        return repository.findById(timingId);
    }

    @Override
    public List<MedicationTiming> findAll() {
        return repository.findAll();
    }

    @Override
    public boolean existsByTimingName(String timingName) {
        return repository.existsByTimingName(timingName);
    }

    @Override
    public boolean existsByTimingNameAndTimingIdNot(String timingName, Integer timingId) {
        return repository.existsByTimingNameAndTimingIDNot(timingName, timingId);
    }

    @Override
    public List<MedicationTiming> searchByKeyword(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return repository.findAll();
        }
        return repository.findAll().stream()
                .filter(t -> com.quan.diabetes.util.SearchUtil.matches(t.getTimingName(), keyword))
                .collect(java.util.stream.Collectors.toList());
    }
}