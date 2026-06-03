package com.quan.diabetes.controller.admin;

import com.quan.diabetes.entity.Medication;
import com.quan.diabetes.service.MedicationService;
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
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String route,
            @RequestParam(required = false) String form,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "medicationName") String sortField,
            @RequestParam(defaultValue = "asc") String sortDirection,
            Model model) {

        // Create sort object
        Sort.Direction direction = sortDirection.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sort = Sort.by(direction, sortField);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Medication> medicationsPage;

        if ("active".equalsIgnoreCase(status)) {
            medicationsPage = medicationService.findAllActive(pageable);
            model.addAttribute("selectedStatus", "active");
        } else if ("clocked".equalsIgnoreCase(status)) {
            medicationsPage = medicationService.findAllClocked(pageable);
            model.addAttribute("selectedStatus", "clocked");
        } else {
            medicationsPage = medicationService.findAll(pageable);
            model.addAttribute("selectedStatus", "all");
        }

        if (form != null && !form.isEmpty()) {
            medicationsPage = medicationService.findByForm(form, pageable);
            model.addAttribute("selectedForm", form);
        }

        if (route != null && !route.isEmpty()) {
            medicationsPage = medicationService.findByAdministrationRoute(route, pageable);
            model.addAttribute("selectedRoute", route);
        }

        if (keyword != null && !keyword.trim().isEmpty()) {
            medicationsPage = medicationService.searchByKeyword(keyword, pageable);
            model.addAttribute("searchKeyword", keyword);
        }

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

    @PostMapping("/add")
    public String addMedication(@ModelAttribute Medication medication) {
        try {
            medicationService.create(medication);
            return "redirect:/admin/medicines?success=Medicine \"" + medication.getMedicationName() + "\" added successfully!";
        } catch (IllegalArgumentException e) {
            return "redirect:/admin/medicines?error=" + e.getMessage();
        } catch (Exception e) {
            return "redirect:/admin/medicines?error=Error: " + e.getMessage();
        }
    }

    @PostMapping("/edit/{id}")
    public String editMedication(@PathVariable String id, @ModelAttribute Medication medication) {
        try {
            medicationService.update(id, medication);
            return "redirect:/admin/medicines?success=Medicine \"" + medication.getMedicationName() + "\" updated successfully!";
        } catch (Exception e) {
            return "redirect:/admin/medicines?error=Error: " + e.getMessage();
        }
    }

    @PostMapping("/soft-delete/{id}")
    public String softDeleteMedication(@PathVariable String id) {
        try {
            Optional<Medication> med = medicationService.findById(id);
            if (med.isPresent()) {
                medicationService.softDelete(id);
                return "redirect:/admin/medicines?success=Medicine \"" + med.get().getMedicationName() + "\" has been clocked!";
            } else {
                return "redirect:/admin/medicines?error=Medicine not found!";
            }
        } catch (Exception e) {
            return "redirect:/admin/medicines?error=Error: " + e.getMessage();
        }
    }

    @PostMapping("/restore/{id}")
    public String restoreMedication(@PathVariable String id) {
        try {
            Optional<Medication> med = medicationService.findById(id);
            if (med.isPresent()) {
                medicationService.restore(id);
                return "redirect:/admin/medicines?success=Medicine \"" + med.get().getMedicationName() + "\" has been restored!";
            } else {
                return "redirect:/admin/medicines?error=Medicine not found!";
            }
        } catch (Exception e) {
            return "redirect:/admin/medicines?error=Error: " + e.getMessage();
        }
    }

    @GetMapping("/list")
    @ResponseBody
    public java.util.Map<String, Object> listMedicinesJson(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String route,
            @RequestParam(required = false) String form,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "medicationName") String sortField,
            @RequestParam(defaultValue = "asc") String sortDirection) {

        Sort.Direction direction = sortDirection.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sort = Sort.by(direction, sortField);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Medication> medicationsPage;

        if ("active".equalsIgnoreCase(status)) {
            medicationsPage = medicationService.findAllActive(pageable);
        } else if ("clocked".equalsIgnoreCase(status)) {
            medicationsPage = medicationService.findAllClocked(pageable);
        } else {
            medicationsPage = medicationService.findAll(pageable);
        }

        if (form != null && !form.isEmpty()) {
            medicationsPage = medicationService.findByForm(form, pageable);
        }

        if (route != null && !route.isEmpty()) {
            medicationsPage = medicationService.findByAdministrationRoute(route, pageable);
        }

        if (keyword != null && !keyword.trim().isEmpty()) {
            medicationsPage = medicationService.searchByKeyword(keyword, pageable);
        }

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
    public ResponseEntity<?> getMedicationById(@PathVariable String id) {
        Optional<Medication> medication = medicationService.findById(id);
        if (medication.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("success", false, "message", "Medication not found"));
        }
        return ResponseEntity.ok(Map.of("success", true, "data", medication.get()));
    }
}