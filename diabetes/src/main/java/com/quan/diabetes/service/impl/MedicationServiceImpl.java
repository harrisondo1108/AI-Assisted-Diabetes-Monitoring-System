package com.quan.diabetes.service.impl;

import com.quan.diabetes.entity.Medication;
import com.quan.diabetes.repository.MedicationRepository;
import com.quan.diabetes.service.MedicationService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MedicationServiceImpl implements MedicationService {

    private final MedicationRepository medicationRepository;

    public MedicationServiceImpl(MedicationRepository medicationRepository) {
        this.medicationRepository = medicationRepository;
    }

    @Override
    public List<Medication> findAll() {
        return medicationRepository.findAll();
    }

    @Override
    public Optional<Medication> findById(String id) {
        return medicationRepository.findById(id);
    }

    @Override
    public Medication create(Medication entity) {
        return medicationRepository.save(entity);
    }

    @Override
    public Medication update(String id, Medication entity) {
        if (!medicationRepository.existsById(id)) {
            throw new EntityNotFoundException("Medication not found with id: " + id);
        }
        return medicationRepository.save(entity);
    }

    @Override
    public void deleteById(String id) {
        if (!medicationRepository.existsById(id)) {
            throw new EntityNotFoundException("Medication not found with id: " + id);
        }
        medicationRepository.deleteById(id);
    }

    @Override
    public boolean existsById(String id) {
        return medicationRepository.existsById(id);
    }
}
