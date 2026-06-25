package com.quan.diabetes.service.lab;

import com.quan.diabetes.entity.IndicatorThreshold;
import java.util.List;
import java.util.Optional;

public interface IndicatorThresholdService {
    List<IndicatorThreshold> findAll();
    Optional<IndicatorThreshold> findById(Integer id);
    IndicatorThreshold create(IndicatorThreshold entity);
    IndicatorThreshold update(Integer id, IndicatorThreshold entity);
    void deleteById(Integer id);
    boolean existsById(Integer id);
}