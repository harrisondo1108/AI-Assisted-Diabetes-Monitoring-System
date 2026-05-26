package com.quan.diabetes.controller;

import com.quan.diabetes.entity.Medication;
import com.quan.diabetes.service.MedicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/admin/medicines")
public class MedicationController {

    @Autowired
    private MedicationService medicationService;

    // ========== VIEW ==========
    @GetMapping
    public String medicineManagementPage() {
        return "admin/medicine_management";
    }

    // ========== REST API ==========

    // Lấy tất cả thuốc
    @GetMapping("/api")
    @ResponseBody
    public ResponseEntity<?> getAllMedications() {
        try {
            List<Medication> medications = medicationService.findAll();
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", medications);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    // Lấy chi tiết 1 thuốc 
    @GetMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<?> getMedicationById(@PathVariable String id) {
        try {
            Optional<Medication> medication = medicationService.findById(id);
            if (medication.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("success", false, "message", "Medication not found"));
            }
            return ResponseEntity.ok(Map.of("success", true, "data", medication.get()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PostMapping("/api")
    @ResponseBody
    public ResponseEntity<?> createMedication(@RequestBody Medication medication) {
        try {
            // Không cần tạo ID ở đây nữa, Service sẽ tự tạo
            Medication created = medicationService.create(medication);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("success", true, "message", "Medication created successfully!", "data", created));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Error: " + e.getMessage()));
        }
    }

    // Cập nhật thuốc (CHO EDIT)
    @PutMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<?> updateMedication(@PathVariable String id, @RequestBody Medication medication) {
        try {
            Optional<Medication> existingOpt = medicationService.findById(id);
            if (existingOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("success", false, "message", "Medication not found"));
            }

            medication.setMedicationId(id);
            Medication updated = medicationService.update(id, medication);
            return ResponseEntity.ok(Map.of("success", true, "message", "Medication updated successfully!", "data", updated));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Error: " + e.getMessage()));
        }
    }

    // Xóa thuốc (CHO DELETE)
    @DeleteMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<?> deleteMedication(@PathVariable String id) {
        try {
            if (!medicationService.existsById(id)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("success", false, "message", "Medication not found"));
            }
            medicationService.deleteById(id);
            return ResponseEntity.ok(Map.of("success", true, "message", "Medication deleted successfully!"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Error: " + e.getMessage()));
        }
    }

    // Thống kê
    @GetMapping("/api/summary")
    @ResponseBody
    public ResponseEntity<?> getSummary() {
        try {
            List<Medication> medications = medicationService.findAll();
            long total = medications.size();
            long oral = medications.stream()
                    .filter(m -> m.getForm() != null && ("tablet".equalsIgnoreCase(m.getForm()) || "capsule".equalsIgnoreCase(m.getForm())))
                    .count();
            long injectable = medications.stream()
                    .filter(m -> m.getForm() != null && "injection".equalsIgnoreCase(m.getForm()))
                    .count();

            Map<String, Object> summary = new HashMap<>();
            summary.put("totalMedications", total);
            summary.put("oralFormulations", oral);
            summary.put("injectableFormulations", injectable);
            summary.put("uniqueRoutes", medicationService.findAllDistinctRoutes().size());

            return ResponseEntity.ok(Map.of("success", true, "data", summary));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    // Lấy danh sách routes
    @GetMapping("/api/routes")
    @ResponseBody
    public ResponseEntity<?> getAllRoutes() {
        try {
            List<String> routes = medicationService.findAllDistinctRoutes();
            return ResponseEntity.ok(Map.of("success", true, "data", routes));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }
}
