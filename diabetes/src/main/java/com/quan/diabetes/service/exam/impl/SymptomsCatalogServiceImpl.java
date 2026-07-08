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

@Service
@Transactional
public class SymptomsCatalogServiceImpl implements SymptomsCatalogService {

    private final SymptomsCatalogRepository repository;
    private final Random random = new Random();

    public SymptomsCatalogServiceImpl(SymptomsCatalogRepository repository) {
        this.repository = repository;
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

    @Override
    public SymptomsCatalog create(SymptomsCatalog entity) {
        // Tạo ID mới: SYM + 4 chữ số ngẫu nhiên
        String newId;
        do {
            int num = random.nextInt(10000);
            newId = String.format("SYM%04d", num);
        } while (repository.existsById(newId));
        entity.setSymptomId(newId);

        if (repository.existsBySymptomNameIgnoreCase(entity.getSymptomName())) {
            throw new IllegalArgumentException("Symptom name already exists: " + entity.getSymptomName());
        }
        if (entity.getStatus() == null) entity.setStatus(true);
        return repository.save(entity);
    }

    @Override
    public SymptomsCatalog update(String id, SymptomsCatalog entity) {
        SymptomsCatalog existing = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Symptom not found: " + id));
        if (!existing.getSymptomName().equalsIgnoreCase(entity.getSymptomName()) &&
                repository.existsBySymptomNameIgnoreCase(entity.getSymptomName())) {
            throw new IllegalArgumentException("Symptom name already exists: " + entity.getSymptomName());
        }
        existing.setSymptomName(entity.getSymptomName());
        return repository.save(existing);
    }

    @Override
    public void softDelete(String id) {
        SymptomsCatalog symptom = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Symptom not found: " + id));
        symptom.setStatus(false);
        repository.save(symptom);
    }

    @Override
    public void restore(String id) {
        SymptomsCatalog symptom = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Symptom not found: " + id));
        symptom.setStatus(true);
        repository.save(symptom);
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