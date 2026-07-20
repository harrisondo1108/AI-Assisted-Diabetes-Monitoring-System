package com.quan.diabetes.service.medication.impl;

import com.quan.diabetes.entity.Medication;
import com.quan.diabetes.repository.MedicationRepository;
import com.quan.diabetes.service.medication.MedicationService;
import com.quan.diabetes.service.systemlog.SystemLogService;
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

    @Autowired
    private SystemLogService systemLogService;

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
    public Page<Medication> filterMedications(String keyword, String status, String form, String route, Pageable pageable) {
        String kw = (keyword == null || "all".equalsIgnoreCase(keyword.trim())) ? "" : keyword.trim();
        String st = (status == null || "all".equalsIgnoreCase(status.trim())) ? "" : status.trim();
        String fm = (form == null || "all".equalsIgnoreCase(form.trim())) ? "" : form.trim();
        String rt = (route == null || "all".equalsIgnoreCase(route.trim())) ? "" : route.trim();
        return medicationRepository.filterMedications(kw, st, fm, rt, pageable);
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

    private void validateAndTrimMedication(Medication entity) {
        if (entity == null) {
            throw new IllegalArgumentException("Dữ liệu thuốc không hợp lệ!");
        }
        if (entity.getMedicationName() == null || entity.getMedicationName().trim().isEmpty()) {
            throw new IllegalArgumentException("Tên thuốc không được để trống!");
        }
        String trimmedName = entity.getMedicationName().trim();
        if (!trimmedName.matches("^[\\p{L}0-9\\s\\-\\.\\/\\+]+$")) {
            throw new IllegalArgumentException("Tên thuốc không được chứa ký tự đặc biệt!");
        }
        if (entity.getForm() == null || entity.getForm().trim().isEmpty()) {
            throw new IllegalArgumentException("Dạng bào chế không được để trống!");
        }
        if (entity.getAdministrationRoute() == null || entity.getAdministrationRoute().trim().isEmpty()) {
            throw new IllegalArgumentException("Đường dùng không được để trống!");
        }
        entity.setMedicationName(trimmedName);
        entity.setForm(entity.getForm().trim());
        entity.setAdministrationRoute(entity.getAdministrationRoute().trim());
        if (entity.getConcentration() != null) {
            entity.setConcentration(entity.getConcentration().trim());
        }
        if (entity.getUsageInstruction() != null) {
            entity.setUsageInstruction(entity.getUsageInstruction().trim());
        }
    }

    @Override
    public Medication create(Medication entity) {
        validateAndTrimMedication(entity);
        if (existsByMedicationName(entity.getMedicationName())) {
            throw new IllegalArgumentException("Tên thuốc '" + entity.getMedicationName() + "' đã tồn tại trong hệ thống!");
        }

        String newId = generateMedicationId();
        entity.setMedicationId(newId);
        entity.setStatus("Active");
        Medication saved = medicationRepository.save(entity);

        systemLogService.saveLogWithObject(null, "CREATE", "Medicine", newId, "Thêm thuốc mới", null, saved, "SUCCESS");

        return saved;
    }

    @Override
    public Medication update(String id, Medication entity) {
        if (!medicationRepository.existsById(id)) {
            throw new EntityNotFoundException("Không tìm thấy thuốc với mã: " + id);
        }

        validateAndTrimMedication(entity);

        Medication existing = medicationRepository.findById(id).orElse(null);

        Medication oldMedication = new Medication();
        if (existing != null) {
            oldMedication.setMedicationId(existing.getMedicationId());
            oldMedication.setMedicationName(existing.getMedicationName());
            oldMedication.setForm(existing.getForm());
            oldMedication.setConcentration(existing.getConcentration());
            oldMedication.setAdministrationRoute(existing.getAdministrationRoute());
            oldMedication.setUsageInstruction(existing.getUsageInstruction());
            oldMedication.setStatus(existing.getStatus());
        }

        if (!existing.getMedicationName().equalsIgnoreCase(entity.getMedicationName()) &&
                existsByMedicationName(entity.getMedicationName())) {
            throw new IllegalArgumentException("Tên thuốc '" + entity.getMedicationName() + "' đã tồn tại trong hệ thống!");
        }

        entity.setMedicationId(id);
        if (entity.getStatus() == null) {
            entity.setStatus(existing != null ? existing.getStatus() : null);
        }
        Medication updated = medicationRepository.save(entity);

        systemLogService.saveLogWithObject(null, "UPDATE", "Medicine", id, "Cập nhật thông tin thuốc", oldMedication, updated, "SUCCESS");

        return updated;
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
        Medication existing = medicationRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Medication not found with id: " + id));
        medicationRepository.deleteById(id);

        systemLogService.saveLogWithObject(null, "DELETE", "Medicine", id, "Xóa thuốc", existing, null, "SUCCESS");
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