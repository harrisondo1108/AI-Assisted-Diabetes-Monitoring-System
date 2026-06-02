package com.quan.diabetes.service.impl;

import com.quan.diabetes.entity.IndicatorThreshold;
import com.quan.diabetes.repository.IndicatorThresholdRepository;
import com.quan.diabetes.service.IndicatorThresholdService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class IndicatorThresholdServiceImpl implements IndicatorThresholdService {

    @Autowired
    private IndicatorThresholdRepository indicatorThresholdRepository;

    @Override
    public List<IndicatorThreshold> findAll() {
        return indicatorThresholdRepository.findAll();
    }

    @Override
    public Optional<IndicatorThreshold> findById(Integer id) {
        return indicatorThresholdRepository.findById(id);
    }

    @Override
    public IndicatorThreshold create(IndicatorThreshold entity) {
        return indicatorThresholdRepository.save(entity);
    }

    @Override
    public IndicatorThreshold update(Integer id, IndicatorThreshold entity) {
        if (!indicatorThresholdRepository.existsById(id)) {
            throw new RuntimeException("IndicatorThreshold not found with id: " + id);
        }
        entity.setThresholdId(id);
        return indicatorThresholdRepository.save(entity);
    }

    @Override
    public void deleteById(Integer id) {
        indicatorThresholdRepository.deleteById(id);
    }

    @Override
    public boolean existsById(Integer id) {
        return indicatorThresholdRepository.existsById(id);
    }
}