package com.quan.diabetes.service.exam;

import com.quan.diabetes.entity.SymptomsCatalog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Map;
import java.util.Optional;

public interface SymptomsCatalogService {
    Page<SymptomsCatalog> findAll(Pageable pageable);
    Page<SymptomsCatalog> findByStatus(Boolean status, Pageable pageable);
    Page<SymptomsCatalog> searchByKeywordAndStatus(String keyword, Boolean status, Pageable pageable);
    Optional<SymptomsCatalog> findById(String id);
    SymptomsCatalog create(SymptomsCatalog entity);
    SymptomsCatalog update(String id, SymptomsCatalog entity);
    void softDelete(String id);
    void restore(String id);
    Map<String, Object> getSummaryStats();
}