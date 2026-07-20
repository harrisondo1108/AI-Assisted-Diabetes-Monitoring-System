package com.quan.diabetes.controller.admin;

import com.quan.diabetes.entity.Medication;
import com.quan.diabetes.service.medication.MedicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;

@Controller
@RequestMapping("/admin/medicines")
public class MedicationController {

    @Autowired
    private MedicationService medicationService;

    @GetMapping
    public String medicineManagementPage(
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "route", required = false) String route,
            @RequestParam(name = "form", required = false) String form,
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "7") int size,
            @RequestParam(name = "sortField", defaultValue = "medicationId") String sortField,
            @RequestParam(name = "sortDirection", defaultValue = "desc") String sortDirection,
            Model model) {

        // Create sort object
        Sort.Direction direction = sortDirection.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sort = Sort.by(direction, sortField);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Medication> medicationsPage = medicationService.filterMedications(keyword, status, form, route, pageable);

        model.addAttribute("selectedStatus", (status == null || "all".equalsIgnoreCase(status)) ? "" : status);
        model.addAttribute("selectedForm", (form == null || "all".equalsIgnoreCase(form)) ? "" : form);
        model.addAttribute("selectedRoute", (route == null || "all".equalsIgnoreCase(route)) ? "" : route);
        model.addAttribute("searchKeyword", keyword == null ? "" : keyword);

        Map<String, Object> stats = medicationService.getSummary();

        model.addAttribute("medicationsPage", medicationsPage);
        model.addAttribute("medications", medicationsPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", medicationsPage.getTotalPages());
        model.addAttribute("totalItems", medicationsPage.getTotalElements());
        model.addAttribute("pageSize", size);
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDirection", sortDirection);
        model.addAttribute("totalMedications", stats.get("totalMedications"));
        model.addAttribute("activeMedications", stats.get("activeMedications"));
        model.addAttribute("clockedMedications", stats.get("clockedMedications"));
        model.addAttribute("oralFormulations", stats.get("oralFormulations"));
        model.addAttribute("injectableFormulations", stats.get("injectableFormulations"));
        model.addAttribute("routes", medicationService.findAllDistinctRoutes());

        return "admin/medicine_management";
    }

    @GetMapping("/add")
    public String redirectAddGet() {
        return "redirect:/admin/medicines";
    }

    @PostMapping("/add")
    public String addMedication(@ModelAttribute Medication medication, RedirectAttributes redirectAttributes) {
        try {
            medicationService.create(medication);
            redirectAttributes.addAttribute("success", "Thêm mới thuốc \"" + medication.getMedicationName() + "\" thành công!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addAttribute("error", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addAttribute("error", "Lỗi hệ thống: " + e.getMessage());
        }
        return "redirect:/admin/medicines";
    }

    @PostMapping("/edit/{id}")
    public String editMedication(@PathVariable("id") String id, @ModelAttribute Medication medication, RedirectAttributes redirectAttributes) {
        try {
            medicationService.update(id, medication);
            redirectAttributes.addAttribute("success", "Cập nhật thuốc \"" + medication.getMedicationName() + "\" thành công!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addAttribute("error", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addAttribute("error", "Lỗi hệ thống: " + e.getMessage());
        }
        return "redirect:/admin/medicines";
    }

    @PostMapping("/soft-delete/{id}")
    public String softDeleteMedication(@PathVariable("id") String id, RedirectAttributes redirectAttributes) {
        try {
            Optional<Medication> med = medicationService.findById(id);
            if (med.isPresent()) {
                medicationService.softDelete(id);
                redirectAttributes.addAttribute("success", "Đã tạm khóa thuốc \"" + med.get().getMedicationName() + "\"!");
            } else {
                redirectAttributes.addAttribute("error", "Không tìm thấy thuốc!");
            }
        } catch (Exception e) {
            redirectAttributes.addAttribute("error", "Lỗi hệ thống: " + e.getMessage());
        }
        return "redirect:/admin/medicines";
    }

    @PostMapping("/restore/{id}")
    public String restoreMedication(@PathVariable("id") String id, RedirectAttributes redirectAttributes) {
        try {
            Optional<Medication> med = medicationService.findById(id);
            if (med.isPresent()) {
                medicationService.restore(id);
                redirectAttributes.addAttribute("success", "Đã khôi phục thuốc \"" + med.get().getMedicationName() + "\"!");
            } else {
                redirectAttributes.addAttribute("error", "Không tìm thấy thuốc!");
            }
        } catch (Exception e) {
            redirectAttributes.addAttribute("error", "Lỗi hệ thống: " + e.getMessage());
        }
        return "redirect:/admin/medicines";
    }

    @GetMapping("/list")
    @ResponseBody
    public java.util.Map<String, Object> listMedicinesJson(
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "route", required = false) String route,
            @RequestParam(name = "form", required = false) String form,
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "7") int size,
            @RequestParam(name = "sortField", defaultValue = "medicationId") String sortField,
            @RequestParam(name = "sortDirection", defaultValue = "desc") String sortDirection) {

        Sort.Direction direction = sortDirection.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sort = Sort.by(direction, sortField);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Medication> medicationsPage = medicationService.filterMedications(keyword, status, form, route, pageable);

        java.util.Map<String, Object> response = new java.util.HashMap<>();
        response.put("content", medicationsPage.getContent());
        response.put("currentPage", medicationsPage.getNumber());
        response.put("totalPages", medicationsPage.getTotalPages());
        response.put("totalElements", medicationsPage.getTotalElements());
        response.put("pageSize", medicationsPage.getSize());
        return response;
    }

    @GetMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<?> getMedicationById(@PathVariable("id") String id) {
        Optional<Medication> medication = medicationService.findById(id);
        if (medication.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("success", false, "message", "Medication not found"));
        }
        return ResponseEntity.ok(Map.of("success", true, "data", medication.get()));
    }
}