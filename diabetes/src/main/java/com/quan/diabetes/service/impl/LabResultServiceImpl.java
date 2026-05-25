package com.quan.diabetes.service.impl;

import com.quan.diabetes.entity.LabResult;
import com.quan.diabetes.repository.LabResultRepository;
import com.quan.diabetes.service.LabResultService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class LabResultServiceImpl implements LabResultService {

    private final LabResultRepository labResultRepository;

    public LabResultServiceImpl(LabResultRepository labResultRepository) {
        this.labResultRepository = labResultRepository;
    }

    @Override
    public List<LabResult> findAll() {
        return labResultRepository.findAll();
    }

    @Override
    public Optional<LabResult> findById(String id) {
        return labResultRepository.findById(id);
    }

    @Override
    public LabResult create(LabResult entity) {
        return labResultRepository.save(entity);
    }

    @Override
    public LabResult update(String id, LabResult entity) {
        if (!labResultRepository.existsById(id)) {
            throw new EntityNotFoundException("LabResult not found with id: " + id);
        }
        return labResultRepository.save(entity);
    }

    @Override
    public void deleteById(String id) {
        if (!labResultRepository.existsById(id)) {
            throw new EntityNotFoundException("LabResult not found with id: " + id);
        }
        labResultRepository.deleteById(id);
    }

    @Override
    public boolean existsById(String id) {
        return labResultRepository.existsById(id);
    }
}
