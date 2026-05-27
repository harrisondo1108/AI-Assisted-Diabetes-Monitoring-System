package com.quan.diabetes.controller;

import com.quan.diabetes.entity.LabTestCatalog;
import com.quan.diabetes.service.LabTestCatalogService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/lab-tests")
public class LabTestCatalogController {

    private final LabTestCatalogService labTestCatalogService;

    public LabTestCatalogController(LabTestCatalogService labTestCatalogService) {
        this.labTestCatalogService = labTestCatalogService;
    }

    @GetMapping
    public String showLabTests(Model model) {
        model.addAttribute("testList", labTestCatalogService.findAll());
        return "Admin/LabTest";
    }

    @PostMapping("/create")
    public String createLabTest(@ModelAttribute LabTestCatalog labTestCatalog) {
        labTestCatalogService.create(labTestCatalog);
        return "redirect:/admin/lab-tests";
    }

    @PostMapping("/update/{id}")
    public String updateLabTest(@PathVariable("id") String id,
                                @ModelAttribute LabTestCatalog labTestCatalog) {
        labTestCatalog.setLabTestId(id);
        labTestCatalogService.update(id, labTestCatalog);
        return "redirect:/admin/lab-tests";
    }

    @PostMapping("/delete/{id}")
    public String deleteLabTest(@PathVariable("id") String id) {
        labTestCatalogService.deleteById(id);
        return "redirect:/admin/lab-tests";
    }

    @GetMapping("/detail/{id}")
    @ResponseBody
    public LabTestCatalog getLabTestDetail(@PathVariable("id") String id) {
        return labTestCatalogService.findById(id).orElse(null);
    }
}