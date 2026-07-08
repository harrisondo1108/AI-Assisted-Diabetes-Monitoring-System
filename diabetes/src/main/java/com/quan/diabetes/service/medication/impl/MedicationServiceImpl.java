package com.quan.diabetes.service.medication.impl;

import com.quan.diabetes.entity.Medication;
import com.quan.diabetes.repository.MedicationRepository;
import com.quan.diabetes.service.medication.MedicationService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class MedicationServiceImpl implements MedicationService {

    @Autowired
    private MedicationRepository medicationRepository;

    private String generateMedicationId() {
        List<Medication> allMedications = medicationRepository.findAll();
        int maxNumber = 0;
        for (Medication med : allMedications) {
            String id = med.getMedicationId();
            if (id != null && id.startsWith("MED-")) {
                try {
                    int number = Integer.parseInt(id.substring(4));
                    if (number > maxNumber) maxNumber = number;
                } catch (NumberFormatException e) {}
            }
        }
        return String.format("MED-%02d", maxNumber + 1);
    }

    @Override
    public Page<Medication> findAll(Pageable pageable) {
        return medicationRepository.findAll(pageable);
    }

    @Override
    public Page<Medication> findAllActive(Pageable pageable) {
        return medicationRepository.findAllActive(pageable);
    }

    @Override
    public Page<Medication> findAllClocked(Pageable pageable) {
        return medicationRepository.findAllClocked(pageable);
    }

    @Override
    public Page<Medication> findByForm(String form, Pageable pageable) {
        return medicationRepository.findByForm(form, pageable);
    }

    @Override
    public Page<Medication> findByAdministrationRoute(String route, Pageable pageable) {
        return medicationRepository.findByAdministrationRoute(route, pageable);
    }

    @Override
    public Page<Medication> searchByKeyword(String keyword, Pageable pageable) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return findAll(pageable);
        }
        List<Medication> filtered = medicationRepository.findAll().stream()
                .filter(m -> com.quan.diabetes.util.SearchUtil.matches(m.getMedicationName(), keyword)
                        || com.quan.diabetes.util.SearchUtil.matches(m.getAdministrationRoute(), keyword))
                .collect(java.util.stream.Collectors.toList());

        int total = filtered.size();
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), total);
        List<Medication> pageContent = new java.util.ArrayList<>();
        if (start < total) {
            pageContent = filtered.subList(start, end);
        }
        return new org.springframework.data.domain.PageImpl<>(pageContent, pageable, total);
    }

    @Override
    public List<Medication> findAllList() {
        return medicationRepository.findAll();
    }

    @Override
    public List<Medication> findAllActiveList() {
        return medicationRepository.findAllActiveList();
    }

    @Override
    public List<Medication> findAllClockedList() {
        return medicationRepository.findAllClockedList();
    }

    @Override
    public Optional<Medication> findById(String id) {
        return medicationRepository.findById(id);
    }

    @Override
    public Medication create(Medication entity) {
        // Kiểm tra tên thuốc đã tồn tại
        if (existsByMedicationName(entity.getMedicationName())) {
            throw new IllegalArgumentException("Medicine " + entity.getMedicationName() + " already exists!");
        }

        String newId = generateMedicationId();
        entity.setMedicationId(newId);
        entity.setStatus("Active");
        return medicationRepository.save(entity);
    }

    @Override
    public Medication update(String id, Medication entity) {
        if (!medicationRepository.existsById(id)) {
            throw new EntityNotFoundException("Medication not found with id: " + id);
        }

        Medication existing = medicationRepository.findById(id).get();

        // Kiểm tra trùng tên (bỏ qua chính nó)
        if (!existing.getMedicationName().equalsIgnoreCase(entity.getMedicationName()) &&
                existsByMedicationName(entity.getMedicationName())) {
            throw new RuntimeException("Medicine " + entity.getMedicationName() + " already exists!");
        }

        entity.setMedicationId(id);
        if (entity.getStatus() == null) {
            entity.setStatus(existing.getStatus());
        }
        return medicationRepository.save(entity);
    }

    @Override
    public void updateStatus(String id, String status) {
        if (!medicationRepository.existsById(id)) {
            throw new EntityNotFoundException("Medication not found with id: " + id);
        }
        medicationRepository.updateStatus(id, status);
    }

    @Override
    public void softDelete(String id) {
        updateStatus(id, "Clocked");
    }

    @Override
    public void restore(String id) {
        updateStatus(id, "Active");
    }

    @Override
    public void deleteById(String id) {
        if (!medicationRepository.existsById(id)) {
            throw new EntityNotFoundException("Medication not found with id: " + id);
        }
        medicationRepository.deleteById(id);
    }

    @Override
    public boolean existsById(String id) {
        return medicationRepository.existsById(id);
    }

    @Override
    public boolean existsByMedicationName(String medicationName) {
        return medicationRepository.existsByMedicationNameIgnoreCase(medicationName);
    }

    @Override
    public List<Medication> searchByKeywordList(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return findAllList();
        }
        return medicationRepository.findAll().stream()
                .filter(m -> com.quan.diabetes.util.SearchUtil.matches(m.getMedicationName(), keyword)
                        || com.quan.diabetes.util.SearchUtil.matches(m.getConcentration(), keyword)
                        || com.quan.diabetes.util.SearchUtil.matches(m.getAdministrationRoute(), keyword))
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public List<String> findAllDistinctRoutes() {
        return medicationRepository.findAllDistinctRoutes();
    }

    @Override
    public Map<String, Object> getSummary() {
        Map<String, Object> summary = new HashMap<>();
        summary.put("totalMedications", medicationRepository.countTotalMedications());
        summary.put("activeMedications", medicationRepository.countActiveMedications());
        summary.put("clockedMedications", medicationRepository.countClockedMedications());

        List<Medication> all = findAllList();
        long oral = all.stream()
                .filter(m -> m.getForm() != null && ("tablet".equalsIgnoreCase(m.getForm()) || "capsule".equalsIgnoreCase(m.getForm())))
                .count();
        long injectable = all.stream()
                .filter(m -> m.getForm() != null && "injection".equalsIgnoreCase(m.getForm()))
                .count();

        summary.put("oralFormulations", oral);
        summary.put("injectableFormulations", injectable);
        summary.put("uniqueRoutes", findAllDistinctRoutes().size());

        return summary;
    }
}