package com.quan.diabetes.service.exam.impl;

import com.quan.diabetes.entity.SymptomsCatalog;
import com.quan.diabetes.repository.SymptomsCatalogRepository;
import com.quan.diabetes.service.exam.SymptomsCatalogService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

import com.quan.diabetes.service.systemlog.SystemLogService;

@Service
@Transactional
public class SymptomsCatalogServiceImpl implements SymptomsCatalogService {

    private final SymptomsCatalogRepository repository;
    private final SystemLogService systemLogService;
    private final Random random = new Random();

    public SymptomsCatalogServiceImpl(SymptomsCatalogRepository repository, SystemLogService systemLogService) {
        this.repository = repository;
        this.systemLogService = systemLogService;
    }

    @Override
    public Page<SymptomsCatalog> findAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    @Override
    public Page<SymptomsCatalog> findByStatus(Boolean status, Pageable pageable) {
        if (status == null) return findAll(pageable);
        return repository.findByStatus(status, pageable);
    }

    @Override
    public Page<SymptomsCatalog> searchByKeywordAndStatus(String keyword, Boolean status, Pageable pageable) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return findByStatus(status, pageable);
        }
        java.util.List<SymptomsCatalog> filtered = repository.findAll().stream()
                .filter(s -> (status == null || status.equals(s.getStatus())))
                .filter(s -> com.quan.diabetes.util.SearchUtil.matches(s.getSymptomId(), keyword)
                        || com.quan.diabetes.util.SearchUtil.matches(s.getSymptomName(), keyword))
                .collect(java.util.stream.Collectors.toList());

        int total = filtered.size();
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), total);
        java.util.List<SymptomsCatalog> pageContent = new java.util.ArrayList<>();
        if (start < total) {
            pageContent = filtered.subList(start, end);
        }
        return new org.springframework.data.domain.PageImpl<>(pageContent, pageable, total);
    }

    @Override
    public Optional<SymptomsCatalog> findById(String id) {
        return repository.findById(id);
    }

    private void validateSymptomName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Tên triệu chứng không được để trống!");
        }
        // Cho phép chữ cái (Unicode), số, khoảng trắng, dấu ngoặc đơn, dấu phẩy, dấu chấm, dấu gạch ngang
        String pattern = "^[\\p{L}\\p{N} (),.\\-]+$";
        if (!name.matches(pattern)) {
            throw new IllegalArgumentException("Tên triệu chứng không được chứa ký tự đặc biệt!");
        }
    }

    @Override
    public SymptomsCatalog create(SymptomsCatalog entity) {
        validateSymptomName(entity.getSymptomName());
        // Tạo ID mới: SYM + 4 chữ số ngẫu nhiên
        String newId;
        do {
            int num = random.nextInt(10000);
            newId = String.format("SYM%04d", num);
        } while (repository.existsById(newId));
        entity.setSymptomId(newId);

        if (repository.existsBySymptomNameIgnoreCase(entity.getSymptomName())) {
            throw new IllegalArgumentException("Tên triệu chứng đã tồn tại: " + entity.getSymptomName());
        }
        if (entity.getStatus() == null) entity.setStatus(true);
        SymptomsCatalog saved = repository.save(entity);
        systemLogService.saveLogWithObject(null, "CREATE", "SymptomsCatalog", saved.getSymptomId(), "Thêm triệu chứng mới", null, saved, "SUCCESS");
        return saved;
    }

    @Override
    public SymptomsCatalog update(String id, SymptomsCatalog entity) {
        validateSymptomName(entity.getSymptomName());
        SymptomsCatalog existing = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy triệu chứng: " + id));
        if (!existing.getSymptomName().equalsIgnoreCase(entity.getSymptomName()) &&
                repository.existsBySymptomNameIgnoreCase(entity.getSymptomName())) {
            throw new IllegalArgumentException("Tên triệu chứng đã tồn tại: " + entity.getSymptomName());
        }
        
        SymptomsCatalog oldSymptom = new SymptomsCatalog();
        oldSymptom.setSymptomId(existing.getSymptomId());
        oldSymptom.setSymptomName(existing.getSymptomName());
        oldSymptom.setStatus(existing.getStatus());
        
        existing.setSymptomName(entity.getSymptomName());
        SymptomsCatalog updated = repository.save(existing);
        systemLogService.saveLogWithObject(null, "UPDATE", "SymptomsCatalog", id, "Cập nhật triệu chứng", oldSymptom, updated, "SUCCESS");
        return updated;
    }

    @Override
    public void softDelete(String id) {
        SymptomsCatalog symptom = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy triệu chứng: " + id));
                
        SymptomsCatalog oldSymptom = new SymptomsCatalog();
        oldSymptom.setSymptomId(symptom.getSymptomId());
        oldSymptom.setSymptomName(symptom.getSymptomName());
        oldSymptom.setStatus(symptom.getStatus());
        
        symptom.setStatus(false);
        SymptomsCatalog updated = repository.save(symptom);
        systemLogService.saveLogWithObject(null, "LOCK", "SymptomsCatalog", id, "Khóa triệu chứng", oldSymptom, updated, "SUCCESS");
    }

    @Override
    public void restore(String id) {
        SymptomsCatalog symptom = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy triệu chứng: " + id));
                
        SymptomsCatalog oldSymptom = new SymptomsCatalog();
        oldSymptom.setSymptomId(symptom.getSymptomId());
        oldSymptom.setSymptomName(symptom.getSymptomName());
        oldSymptom.setStatus(symptom.getStatus());
        
        symptom.setStatus(true);
        SymptomsCatalog updated = repository.save(symptom);
        systemLogService.saveLogWithObject(null, "UNLOCK", "SymptomsCatalog", id, "Mở khóa triệu chứng", oldSymptom, updated, "SUCCESS");
    }

    @Override
    public void delete(String id) {
        SymptomsCatalog symptom = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy triệu chứng: " + id));
        repository.delete(symptom);
        systemLogService.saveLogWithObject(null, "DELETE", "SymptomsCatalog", id, "Xóa triệu chứng", symptom, null, "SUCCESS");
    }

    @Override
    public Map<String, Object> getSummaryStats() {
        long total = repository.count();
        long active = repository.findByStatus(true, Pageable.unpaged()).getTotalElements();
        long clocked = repository.findByStatus(false, Pageable.unpaged()).getTotalElements();
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalSymptoms", total);
        stats.put("activeSymptoms", active);
        stats.put("clockedSymptoms", clocked);
        stats.put("totalCategories", 0); // có thể thêm sau
        return stats;
    }
}