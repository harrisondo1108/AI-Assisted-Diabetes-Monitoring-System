package com.quan.diabetes.controller.admin;

import com.quan.diabetes.entity.SymptomsCatalog;
import com.quan.diabetes.service.exam.SymptomsCatalogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/admin/symptoms")
public class SymptomController {

    @Autowired
    private SymptomsCatalogService symptomService;

    @GetMapping
    public String symptomCatalogPage(
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "7") int size,
            @RequestParam(name = "sortField", defaultValue = "symptomName") String sortField,
            @RequestParam(name = "sortDirection", defaultValue = "asc") String sortDirection,
            Model model) {

        Sort.Direction direction = Sort.Direction.fromString(sortDirection);
        Sort sort = Sort.by(direction, sortField);
        Pageable pageable = PageRequest.of(page, size, sort);

        Boolean statusBool = null;
        if ("active".equalsIgnoreCase(status)) statusBool = true;
        else if ("clocked".equalsIgnoreCase(status)) statusBool = false;

        Page<SymptomsCatalog> symptomsPage;
        if (keyword != null && !keyword.trim().isEmpty()) {
            symptomsPage = symptomService.searchByKeywordAndStatus(keyword, statusBool, pageable);
            model.addAttribute("searchKeyword", keyword);
        } else {
            symptomsPage = symptomService.findByStatus(statusBool, pageable);
        }

        // Thống kê
        var stats = symptomService.getSummaryStats();

        model.addAttribute("symptoms", symptomsPage.getContent());
        model.addAttribute("currentPage", symptomsPage.getNumber());
        model.addAttribute("totalPages", symptomsPage.getTotalPages());
        model.addAttribute("totalItems", symptomsPage.getTotalElements());
        model.addAttribute("pageSize", size);
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDirection", sortDirection);
        model.addAttribute("selectedStatus", status != null ? status : "");
        model.addAttribute("totalSymptoms", stats.get("totalSymptoms"));
        model.addAttribute("activeSymptoms", stats.get("activeSymptoms"));
        model.addAttribute("clockedSymptoms", stats.get("clockedSymptoms"));

        return "admin/symptoms";
    }
    // API JSON cho danh sách (dùng cho real-time search)
    @GetMapping("/list")
    @ResponseBody
    public Map<String, Object> listSymptoms(
            @RequestParam(name = "keyword", required = false, defaultValue = "") String keyword,
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "7") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("symptomName").ascending());
        Boolean statusBool = null;
        if ("active".equalsIgnoreCase(status)) statusBool = true;
        else if ("clocked".equalsIgnoreCase(status)) statusBool = false;

        Page<SymptomsCatalog> symptomsPage;
        if (keyword != null && !keyword.trim().isEmpty()) {
            symptomsPage = symptomService.searchByKeywordAndStatus(keyword.trim(), statusBool, pageable);
        } else {
            symptomsPage = symptomService.findByStatus(statusBool, pageable);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("content", symptomsPage.getContent());
        response.put("currentPage", symptomsPage.getNumber());
        response.put("totalPages", symptomsPage.getTotalPages());
        response.put("totalElements", symptomsPage.getTotalElements());
        response.put("pageSize", symptomsPage.getSize());
        return response;
    }

    // API JSON cho thống kê (để cập nhật số lượng khi tìm kiếm)
    @GetMapping("/stats")
    @ResponseBody
    public Map<String, Object> getStats() {
        return symptomService.getSummaryStats();
    }
    @GetMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getSymptomById(@PathVariable String id) {
        Optional<SymptomsCatalog> symptom = symptomService.findById(id);
        if (symptom.isEmpty()) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(Map.of("success", true, "data", symptom.get()));
    }

    @PostMapping("/add")
    public String addSymptom(@ModelAttribute SymptomsCatalog symptom) {
        try {
            symptomService.create(symptom);
            String message = "Symptom \"" + symptom.getSymptomName() + "\" added successfully!";
            String encoded = URLEncoder.encode(message, StandardCharsets.UTF_8);
            return "redirect:/admin/symptoms?success=" + encoded;
        } catch (IllegalArgumentException e) {
            String encoded = URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8);
            return "redirect:/admin/symptoms?error=" + encoded;
        } catch (Exception e) {
            String encoded = URLEncoder.encode("Error: " + e.getMessage(), StandardCharsets.UTF_8);
            return "redirect:/admin/symptoms?error=" + encoded;
        }
    }

    @PostMapping("/edit/{id}")
    public String editSymptom(@PathVariable("id") String id, @ModelAttribute SymptomsCatalog symptom) {
        try {
            symptomService.update(id, symptom);
            String message = "Symptom updated successfully!";
            String encoded = URLEncoder.encode(message, StandardCharsets.UTF_8);
            return "redirect:/admin/symptoms?success=" + encoded;
        } catch (Exception e) {
            String encoded = URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8);
            return "redirect:/admin/symptoms?error=" + encoded;
        }
    }

    @PostMapping("/soft-delete/{id}")
    public String softDeleteSymptom(@PathVariable("id") String id) {
        try {
            var opt = symptomService.findById(id);
            if (opt.isPresent()) {
                symptomService.softDelete(id);
                String message = "Symptom \"" + opt.get().getSymptomName() + "\" has been clocked!";
                String encoded = URLEncoder.encode(message, StandardCharsets.UTF_8);
                return "redirect:/admin/symptoms?success=" + encoded;
            }
            String encoded = URLEncoder.encode("Symptom not found!", StandardCharsets.UTF_8);
            return "redirect:/admin/symptoms?error=" + encoded;
        } catch (Exception e) {
            String encoded = URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8);
            return "redirect:/admin/symptoms?error=" + encoded;
        }
    }

    @PostMapping("/restore/{id}")
    public String restoreSymptom(@PathVariable("id") String id) {
        try {
            var opt = symptomService.findById(id);
            if (opt.isPresent()) {
                symptomService.restore(id);
                String message = "Symptom \"" + opt.get().getSymptomName() + "\" has been restored!";
                String encoded = URLEncoder.encode(message, StandardCharsets.UTF_8);
                return "redirect:/admin/symptoms?success=" + encoded;
            }
            String encoded = URLEncoder.encode("Symptom not found!", StandardCharsets.UTF_8);
            return "redirect:/admin/symptoms?error=" + encoded;
        } catch (Exception e) {
            String encoded = URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8);
            return "redirect:/admin/symptoms?error=" + encoded;
        }
    }
}