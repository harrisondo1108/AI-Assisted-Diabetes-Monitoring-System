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

    /* ── Danh sách ── */
    @GetMapping
    public String showLabTests(@RequestParam(value = "error", required = false) String error,
                               Model model) {
        model.addAttribute("testList", labTestCatalogService.findAll());
        model.addAttribute("roomList", roomService.findAll());

        if ("duplicate".equals(error))
            model.addAttribute("errorMessage", "Tên xét nghiệm đã tồn tại.");
        else if ("empty".equals(error))
            model.addAttribute("errorMessage", "Vui lòng nhập đầy đủ dữ liệu.");
        else if ("room".equals(error))
            model.addAttribute("errorMessage", "Vui lòng chọn phòng xét nghiệm.");

        return "Admin/LabTest";
    }

    /* ── Tạo mới ── */
    @PostMapping("/create")
    public String createLabTest(@ModelAttribute LabTestCatalog labTestCatalog) {
        String testName = labTestCatalog.getTestName();

        if (testName == null || testName.trim().isEmpty()
                || labTestCatalog.getUnit() == null
                || labTestCatalog.getUnit().trim().isEmpty()) {
            return "redirect:/admin/lab-tests?error=empty";
        }

        if (labTestCatalog.getRoomId() == null) {
            return "redirect:/admin/lab-tests?error=room";
        }

        labTestCatalog.setTestName(testName.trim());
        labTestCatalog.setUnit(labTestCatalog.getUnit().trim());

        if (labTestCatalogService.existsByTestName(testName.trim())) {
            return "redirect:/admin/lab-tests?error=duplicate";
        }

        labTestCatalog.setLabTestId(labTestCatalogService.generateLabTestId());
        labTestCatalog.setStatus(true);
        labTestCatalogService.create(labTestCatalog);

        return "redirect:/admin/lab-tests";
    }

    /* ── Cập nhật ── */
    @PostMapping("/update/{id}")
    public String updateLabTest(@PathVariable("id") String id,
                                @ModelAttribute LabTestCatalog labTestCatalog) {
        String testName = labTestCatalog.getTestName();

        if (testName == null || testName.trim().isEmpty()
                || labTestCatalog.getUnit() == null
                || labTestCatalog.getUnit().trim().isEmpty()) {
            return "redirect:/admin/lab-tests?error=empty";
        }

        if (labTestCatalog.getRoomId() == null) {
            return "redirect:/admin/lab-tests?error=room";
        }

        labTestCatalog.setTestName(testName.trim());
        labTestCatalog.setUnit(labTestCatalog.getUnit().trim());

        if (labTestCatalogService.existsByTestNameAndLabTestIdNot(testName.trim(), id)) {
            return "redirect:/admin/lab-tests?error=duplicate";
        }

        LabTestCatalog existing = labTestCatalogService.findById(id).orElse(null);
        if (existing != null) {
            labTestCatalog.setLabTestId(id);
            labTestCatalog.setStatus(existing.getStatus());
            labTestCatalogService.update(id, labTestCatalog);
        }

        return "redirect:/admin/lab-tests";
    }

    /* ── Xóa ── */
    @PostMapping("/delete/{id}")
    public String deleteLabTest(@PathVariable("id") String id) {
        labTestCatalogService.deleteById(id);
        return "redirect:/admin/lab-tests";
    }

    /* ── Chi tiết (JSON cho JS) ── */
    @GetMapping("/detail/{id}")
    @ResponseBody
    public LabTestCatalog getLabTestDetail(@PathVariable("id") String id) {
        return labTestCatalogService.findById(id).orElse(null);
    }

    /* ── Toggle Active / Inactive ── */
    @PostMapping("/toggle-status/{id}")
    public String toggleStatus(@PathVariable("id") String id) {
        LabTestCatalog test = labTestCatalogService.findById(id).orElse(null);
        if (test != null) {
            Boolean current = test.getStatus();
            test.setStatus(current == null || !current);
            labTestCatalogService.update(id, test);
        }
        return "redirect:/admin/lab-tests";
    }
}