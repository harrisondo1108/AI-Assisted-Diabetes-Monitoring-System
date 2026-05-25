package com.quan.diabetes.service.impl;

import com.quan.diabetes.entity.LabOrder;
import com.quan.diabetes.repository.LabOrderRepository;
import com.quan.diabetes.service.LabOrderService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class LabOrderServiceImpl implements LabOrderService {

    private final LabOrderRepository labOrderRepository;

    public LabOrderServiceImpl(LabOrderRepository labOrderRepository) {
        this.labOrderRepository = labOrderRepository;
    }

    @Override
    public List<LabOrder> findAll() {
        return labOrderRepository.findAll();
    }

    @Override
    public Optional<LabOrder> findById(String id) {
        return labOrderRepository.findById(id);
    }

    @Override
    public LabOrder create(LabOrder entity) {
        return labOrderRepository.save(entity);
    }

    @Override
    public LabOrder update(String id, LabOrder entity) {
        if (!labOrderRepository.existsById(id)) {
            throw new EntityNotFoundException("LabOrder not found with id: " + id);
        }
        return labOrderRepository.save(entity);
    }

    @Override
    public void deleteById(String id) {
        if (!labOrderRepository.existsById(id)) {
            throw new EntityNotFoundException("LabOrder not found with id: " + id);
        }
        labOrderRepository.deleteById(id);
    }

    @Override
    public boolean existsById(String id) {
        return labOrderRepository.existsById(id);
    }
}
