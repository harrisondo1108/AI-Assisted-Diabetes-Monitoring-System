package com.quan.diabetes.controller;

import com.quan.diabetes.entity.Medication;
import com.quan.diabetes.service.MedicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;
import java.util.Optional;

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
            Model model) {

        List<Medication> medications = medicationService.findAll();

        if (form != null && !form.isEmpty()) {
            medications = medications.stream().filter(m -> form.equals(m.getForm())).toList();
            model.addAttribute("selectedForm", form);
        }
        if (route != null && !route.isEmpty()) {
            medications = medications.stream().filter(m -> route.equals(m.getAdministrationRoute())).toList();
            model.addAttribute("selectedRoute", route);
        }
        if (keyword != null && !keyword.trim().isEmpty()) {
            medications = medicationService.searchByKeyword(keyword);
            model.addAttribute("searchKeyword", keyword);
        }

        long total = medications.size();
        long oral = medications.stream().filter(m -> m.getForm() != null && ("tablet".equalsIgnoreCase(m.getForm()) || "capsule".equalsIgnoreCase(m.getForm()))).count();
        long injectable = medications.stream().filter(m -> m.getForm() != null && "injection".equalsIgnoreCase(m.getForm())).count();

        model.addAttribute("medications", medications);
        model.addAttribute("totalMedications", total);
        model.addAttribute("oralFormulations", oral);
        model.addAttribute("injectableFormulations", injectable);
        model.addAttribute("routes", medicationService.findAllDistinctRoutes());

        return "admin/medicine_management";
    }

    @PostMapping("/add")
    public String addMedication(@ModelAttribute Medication medication, RedirectAttributes redirectAttributes) {
        try {
            medicationService.create(medication);
            redirectAttributes.addFlashAttribute("success", "Medicine \"" + medication.getMedicationName() + "\" added successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error: " + e.getMessage());
        }
        return "redirect:/admin/medicines";
    }

    @PostMapping("/edit/{id}")
    public String editMedication(@PathVariable String id, @ModelAttribute Medication medication, RedirectAttributes redirectAttributes) {
        try {
            medicationService.update(id, medication);
            redirectAttributes.addFlashAttribute("success", "Medicine \"" + medication.getMedicationName() + "\" updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error: " + e.getMessage());
        }
        return "redirect:/admin/medicines";
    }

    @GetMapping("/delete/{id}")
    public String deleteMedication(@PathVariable String id, RedirectAttributes redirectAttributes) {
        try {
            Optional<Medication> med = medicationService.findById(id);
            if (med.isPresent()) {
                String name = med.get().getMedicationName();
                medicationService.deleteById(id);
                redirectAttributes.addFlashAttribute("success", "Medicine \"" + name + "\" deleted successfully!");
            } else {
                redirectAttributes.addFlashAttribute("error", "Medicine not found!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error: " + e.getMessage());
        }
        return "redirect:/admin/medicines";
    }

    @GetMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<?> getMedicationById(@PathVariable String id) {
        Optional<Medication> medication = medicationService.findById(id);
        if (medication.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("success", false, "message", "Medication not found"));
        }
        return ResponseEntity.ok(Map.of("success", true, "data", medication.get()));
    }
}