package com.quan.diabetes.service.impl;

import com.quan.diabetes.entity.PrescriptionDetail;
import com.quan.diabetes.repository.PrescriptionDetailRepository;
import com.quan.diabetes.service.PrescriptionDetailService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PrescriptionDetailServiceImpl implements PrescriptionDetailService {

    private final PrescriptionDetailRepository prescriptionDetailRepository;

    public PrescriptionDetailServiceImpl(PrescriptionDetailRepository prescriptionDetailRepository) {
        this.prescriptionDetailRepository = prescriptionDetailRepository;
    }

    @Override
    public List<PrescriptionDetail> findAll() {
        return prescriptionDetailRepository.findAll();
    }

    @Override
    public Optional<PrescriptionDetail> findById(String id) {
        return prescriptionDetailRepository.findById(id);
    }

    @Override
    public PrescriptionDetail create(PrescriptionDetail entity) {
        return prescriptionDetailRepository.save(entity);
    }

    @Override
    public PrescriptionDetail update(String id, PrescriptionDetail entity) {
        if (!prescriptionDetailRepository.existsById(id)) {
            throw new EntityNotFoundException("PrescriptionDetail not found with id: " + id);
        }
        return prescriptionDetailRepository.save(entity);
    }

    @Override
    public void deleteById(String id) {
        if (!prescriptionDetailRepository.existsById(id)) {
            throw new EntityNotFoundException("PrescriptionDetail not found with id: " + id);
        }
        prescriptionDetailRepository.deleteById(id);
    }

    @Override
    public boolean existsById(String id) {
        return prescriptionDetailRepository.existsById(id);
    }
}
