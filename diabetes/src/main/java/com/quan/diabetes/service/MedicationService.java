package com.quan.diabetes.service;

import com.quan.diabetes.entity.Medication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface MedicationService {

    // Paginated methods
    public Page<Medication> findAll(Pageable pageable);
    public Page<Medication> findAllActive(Pageable pageable);
    public Page<Medication> findAllClocked(Pageable pageable);
    public Page<Medication> findByForm(String form, Pageable pageable);
    public Page<Medication> findByAdministrationRoute(String route, Pageable pageable);
    public Page<Medication> searchByKeyword(String keyword, Pageable pageable);

    // Non-paginated methods (for dropdowns, stats, etc.)
    public List<Medication> findAllList();
    public List<Medication> findAllActiveList();
    public List<Medication> findAllClockedList();
    public Optional<Medication> findById(String id);
    public Medication create(Medication entity);
    public Medication update(String id, Medication entity);
    public void updateStatus(String id, String status);
    public void softDelete(String id);
    public void restore(String id);
    public void deleteById(String id);
    public boolean existsById(String id);
    public boolean existsByMedicationName(String medicationName);
    public List<String> findAllDistinctRoutes();
    public Map<String, Object> getSummary();
}