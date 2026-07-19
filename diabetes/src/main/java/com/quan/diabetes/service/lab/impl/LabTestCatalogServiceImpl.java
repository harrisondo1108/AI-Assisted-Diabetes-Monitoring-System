package com.quan.diabetes.service.lab.impl;

import com.quan.diabetes.entity.LabTestCatalog;
import com.quan.diabetes.repository.LabTestCatalogRepository;
import com.quan.diabetes.service.lab.LabTestCatalogService;
import com.quan.diabetes.service.systemlog.SystemLogService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class LabTestCatalogServiceImpl implements LabTestCatalogService {

    private final LabTestCatalogRepository labTestCatalogRepository;
    private final SystemLogService systemLogService;

    public LabTestCatalogServiceImpl(LabTestCatalogRepository labTestCatalogRepository, SystemLogService systemLogService) {
        this.labTestCatalogRepository = labTestCatalogRepository;
        this.systemLogService = systemLogService;
    }

    @Override
    public List<LabTestCatalog> findAll() {
        return labTestCatalogRepository.findAll();
    }

    @Override
    public Optional<LabTestCatalog> findById(String id) {
        return labTestCatalogRepository.findById(id);
    }

    @Override
    public LabTestCatalog create(LabTestCatalog entity) {
        LabTestCatalog saved = labTestCatalogRepository.save(entity);
        systemLogService.saveLogWithObject(null, "CREATE", "LaboratoryTest", saved.getLabTestId(), "Thêm xét nghiệm mới", null, saved, "SUCCESS");
        return saved;
    }

    @Override
    public LabTestCatalog update(String id, LabTestCatalog entity) {
        LabTestCatalog existing = labTestCatalogRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("LabTestCatalog not found with id: " + id));
        
        LabTestCatalog oldLabTest = new LabTestCatalog();
        oldLabTest.setLabTestId(existing.getLabTestId());
        oldLabTest.setTestName(existing.getTestName());
        oldLabTest.setUnit(existing.getUnit());
        oldLabTest.setDescription(existing.getDescription());
        oldLabTest.setRoomId(existing.getRoomId());
        oldLabTest.setStatus(existing.getStatus());

        LabTestCatalog updated = labTestCatalogRepository.save(entity);
        
        systemLogService.saveLogWithObject(null, "UPDATE", "LaboratoryTest", id, "Cập nhật xét nghiệm", oldLabTest, updated, "SUCCESS");
        return updated;
    }

    @Override
    public void deleteById(String id) {
        LabTestCatalog existing = labTestCatalogRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("LabTestCatalog not found with id: " + id));
        labTestCatalogRepository.deleteById(id);
        systemLogService.saveLogWithObject(null, "DELETE", "LaboratoryTest", id, "Xóa xét nghiệm", existing, null, "SUCCESS");
    }

    @Override
    public boolean existsById(String id) {
        return labTestCatalogRepository.existsById(id);
    }

    @Override
    public boolean existsByTestName(String testName) {
        return labTestCatalogRepository.existsByTestName(testName);
    }

    @Override
    public boolean existsByTestNameAndLabTestIdNot(String testName, String labTestId) {
        return labTestCatalogRepository.existsByTestNameAndLabTestIdNot(testName, labTestId);
    }

    @Override
    public String generateLabTestId() {
        return "LT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    @Override
    public List<LabTestCatalog> searchByKeywordAndStatus(String keyword, Boolean status) {
        return labTestCatalogRepository.findAll().stream()
                .filter(t -> (status == null || status.equals(t.getStatus())))
                .filter(t -> com.quan.diabetes.util.SearchUtil.matches(t.getTestName(), keyword)
                          || com.quan.diabetes.util.SearchUtil.matches(t.getUnit(), keyword))
                .collect(java.util.stream.Collectors.toList());
    }
}
