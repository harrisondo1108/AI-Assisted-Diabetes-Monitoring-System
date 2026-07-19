package com.quan.diabetes.service.lab.impl;

import com.quan.diabetes.entity.IndicatorThreshold;
import com.quan.diabetes.repository.IndicatorThresholdRepository;
import com.quan.diabetes.service.lab.IndicatorThresholdService;
import com.quan.diabetes.service.systemlog.SystemLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class IndicatorThresholdServiceImpl implements IndicatorThresholdService {

    @Autowired
    private IndicatorThresholdRepository indicatorThresholdRepository;

    @Autowired
    private SystemLogService systemLogService;

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
        IndicatorThreshold saved = indicatorThresholdRepository.save(entity);
        systemLogService.saveLogWithObject(null, "CREATE", "Threshold", String.valueOf(saved.getThresholdId()), "Thêm ngưỡng chỉ số mới", null, saved, "SUCCESS");
        return saved;
    }

    @Override
    public IndicatorThreshold update(Integer id, IndicatorThreshold entity) {
        if (!indicatorThresholdRepository.existsById(id)) {
            throw new RuntimeException("IndicatorThreshold not found with id: " + id);
        }
        IndicatorThreshold existing = indicatorThresholdRepository.findById(id).orElse(null);
        
        IndicatorThreshold oldThreshold = new IndicatorThreshold();
        if (existing != null) {
            oldThreshold.setThresholdId(existing.getThresholdId());
            oldThreshold.setLabTest(existing.getLabTest());
            oldThreshold.setPatientType(existing.getPatientType());
            oldThreshold.setMinValue(existing.getMinValue());
            oldThreshold.setMaxValue(existing.getMaxValue());
            oldThreshold.setCreatedAt(existing.getCreatedAt());
        }

        entity.setThresholdId(id);
        IndicatorThreshold updated = indicatorThresholdRepository.save(entity);
        
        systemLogService.saveLogWithObject(null, "UPDATE", "Threshold", String.valueOf(id), "Cập nhật ngưỡng chỉ số", oldThreshold, updated, "SUCCESS");
        return updated;
    }

    @Override
    public void deleteById(Integer id) {
        IndicatorThreshold existing = indicatorThresholdRepository.findById(id).orElse(null);
        indicatorThresholdRepository.deleteById(id);
        systemLogService.saveLogWithObject(null, "DELETE", "Threshold", String.valueOf(id), "Xóa ngưỡng chỉ số", existing, null, "SUCCESS");
    }

    @Override
    public boolean existsById(Integer id) {
        return indicatorThresholdRepository.existsById(id);
    }
}