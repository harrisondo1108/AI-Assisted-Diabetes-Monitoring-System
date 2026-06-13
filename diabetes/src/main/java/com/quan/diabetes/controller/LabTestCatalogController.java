package com.quan.diabetes.controller;

import com.quan.diabetes.entity.LabTestCatalog;
import com.quan.diabetes.service.LabTestCatalogService;
import com.quan.diabetes.service.RoomService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/lab-tests")
public class LabTestCatalogController {

    private final LabTestCatalogService labTestCatalogService;
    private final RoomService roomService;

    public LabTestCatalogController(LabTestCatalogService labTestCatalogService,
                                    RoomService roomService) {
        this.labTestCatalogService = labTestCatalogService;
        this.roomService = roomService;
    }

    @GetMapping
    public String showLabTests(@RequestParam(value = "error", required = false) String error,
                               Model model) {

        model.addAttribute("testList", labTestCatalogService.findAll());
        model.addAttribute("roomList", roomService.findAll());

        if ("duplicate".equals(error)) {
            model.addAttribute("errorMessage", "Tên xét nghiệm đã tồn tại.");
        } else if ("empty".equals(error)) {
            model.addAttribute("errorMessage", "Vui lòng nhập đầy đủ dữ liệu.");
        } else if ("negative".equals(error)) {
            model.addAttribute("errorMessage", "Min/Max không được là số âm.");
        } else if ("range".equals(error)) {
            model.addAttribute("errorMessage", "Min không được lớn hơn Max.");
        } else if ("room".equals(error)) {
            model.addAttribute("errorMessage", "Vui lòng chọn phòng xét nghiệm.");
        }

        return "Admin/LabTest";
    }

    @PostMapping("/create")
    public String createLabTest(@ModelAttribute LabTestCatalog labTestCatalog) {

        String testName = labTestCatalog.getTestName();

        if (testName == null || testName.trim().isEmpty()
                || labTestCatalog.getUnit() == null
                || labTestCatalog.getUnit().trim().isEmpty()) {
            return "redirect:/admin/lab-tests?error=empty";
        }

        testName = testName.trim();
        labTestCatalog.setTestName(testName);
        labTestCatalog.setUnit(labTestCatalog.getUnit().trim());

        if (labTestCatalog.getRoomId() == null) {
            return "redirect:/admin/lab-tests?error=room";
        }

        if (labTestCatalogService.existsByTestName(testName)) {
            return "redirect:/admin/lab-tests?error=duplicate";
        }

        if (labTestCatalog.getMinValue() < 0 || labTestCatalog.getMaxValue() < 0) {
            return "redirect:/admin/lab-tests?error=negative";
        }

        if (labTestCatalog.getMinValue() > labTestCatalog.getMaxValue()) {
            return "redirect:/admin/lab-tests?error=range";
        }

        labTestCatalog.setLabTestId(labTestCatalogService.generateLabTestId());
        labTestCatalog.setStatus(true);

        labTestCatalogService.create(labTestCatalog);

        return "redirect:/admin/lab-tests";
    }

    @PostMapping("/update/{id}")
    public String updateLabTest(@PathVariable("id") String id,
                                @ModelAttribute LabTestCatalog labTestCatalog) {

        String testName = labTestCatalog.getTestName();

        if (testName == null || testName.trim().isEmpty()
                || labTestCatalog.getUnit() == null
                || labTestCatalog.getUnit().trim().isEmpty()) {
            return "redirect:/admin/lab-tests?error=empty";
        }

        testName = testName.trim();
        labTestCatalog.setTestName(testName);
        labTestCatalog.setUnit(labTestCatalog.getUnit().trim());

        if (labTestCatalog.getRoomId() == null) {
            return "redirect:/admin/lab-tests?error=room";
        }

        if (labTestCatalogService.existsByTestNameAndLabTestIdNot(testName, id)) {
            return "redirect:/admin/lab-tests?error=duplicate";
        }

        if (labTestCatalog.getMinValue() < 0 || labTestCatalog.getMaxValue() < 0) {
            return "redirect:/admin/lab-tests?error=negative";
        }

        if (labTestCatalog.getMinValue() > labTestCatalog.getMaxValue()) {
            return "redirect:/admin/lab-tests?error=range";
        }

        LabTestCatalog oldLabTest = labTestCatalogService.findById(id).orElse(null);

        if (oldLabTest != null) {
            labTestCatalog.setLabTestId(id);
            labTestCatalog.setStatus(oldLabTest.getStatus());
            labTestCatalogService.update(id, labTestCatalog);
        }

        return "redirect:/admin/lab-tests";
    }

    @GetMapping("/detail/{id}")
    @ResponseBody
    public LabTestCatalog getLabTestDetail(@PathVariable("id") String id) {
        return labTestCatalogService.findById(id).orElse(null);
    }

    @PostMapping("/toggle-status/{id}")
    public String toggleStatus(@PathVariable("id") String id) {

        LabTestCatalog labTestCatalog = labTestCatalogService.findById(id).orElse(null);

        if (labTestCatalog != null) {
            Boolean currentStatus = labTestCatalog.getStatus();

            if (currentStatus == null) {
                currentStatus = true;
            }

            labTestCatalog.setStatus(!currentStatus);
            labTestCatalogService.update(id, labTestCatalog);
        }

        return "redirect:/admin/lab-tests";
    }
}