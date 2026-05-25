package com.quan.diabetes.service.impl;

import com.quan.diabetes.entity.Prescription;
import com.quan.diabetes.repository.PrescriptionRepository;
import com.quan.diabetes.service.PrescriptionService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PrescriptionServiceImpl implements PrescriptionService {

    private final PrescriptionRepository prescriptionRepository;

    public PrescriptionServiceImpl(PrescriptionRepository prescriptionRepository) {
        this.prescriptionRepository = prescriptionRepository;
    }

    @Override
    public List<Prescription> findAll() {
        return prescriptionRepository.findAll();
    }

    @Override
    public Optional<Prescription> findById(String id) {
        return prescriptionRepository.findById(id);
    }

    @Override
    public Prescription create(Prescription entity) {
        return prescriptionRepository.save(entity);
    }

    @Override
    public Prescription update(String id, Prescription entity) {
        if (!prescriptionRepository.existsById(id)) {
            throw new EntityNotFoundException("Prescription not found with id: " + id);
        }
        return prescriptionRepository.save(entity);
    }

    @Override
    public void deleteById(String id) {
        if (!prescriptionRepository.existsById(id)) {
            throw new EntityNotFoundException("Prescription not found with id: " + id);
        }
        prescriptionRepository.deleteById(id);
    }

    @Override
    public boolean existsById(String id) {
        return prescriptionRepository.existsById(id);
    }
}
