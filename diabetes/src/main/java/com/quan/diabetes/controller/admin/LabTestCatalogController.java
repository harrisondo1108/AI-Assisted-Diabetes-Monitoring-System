package com.quan.diabetes.controller.admin;

import com.quan.diabetes.entity.LabTestCatalog;
import com.quan.diabetes.entity.PatientType;
import com.quan.diabetes.entity.IndicatorThreshold;
import com.quan.diabetes.repository.PatientTypeRepository;
import com.quan.diabetes.repository.IndicatorThresholdRepository;
import com.quan.diabetes.service.lab.LabTestCatalogService;
import com.quan.diabetes.service.masterdata.RoomService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;
import java.util.Optional;
import java.util.List;

@Controller
@RequestMapping("/admin/lab-tests")
public class LabTestCatalogController {

    private final LabTestCatalogService labTestCatalogService;
    private final RoomService roomService;
    private final PatientTypeRepository patientTypeRepository;
    private final IndicatorThresholdRepository indicatorThresholdRepository;

    public LabTestCatalogController(LabTestCatalogService labTestCatalogService,
                                    RoomService roomService,
                                    PatientTypeRepository patientTypeRepository,
                                    IndicatorThresholdRepository indicatorThresholdRepository) {
        this.labTestCatalogService = labTestCatalogService;
        this.roomService = roomService;
        this.patientTypeRepository = patientTypeRepository;
        this.indicatorThresholdRepository = indicatorThresholdRepository;
    }

    /* ── Danh sách ── */
    @GetMapping
    public String showLabTests(@RequestParam(value = "keyword", defaultValue = "") String keyword,
                               @RequestParam(value = "status",  defaultValue = "all") String status,
                               @RequestParam(value = "error",   required = false) String error,
                               Model model) {
        Boolean statusFilter = null;
        if ("active".equals(status)) {
            statusFilter = Boolean.TRUE;
        } else if ("inactive".equals(status)) {
            statusFilter = Boolean.FALSE;
        }
        
        String kw = keyword.trim();
        java.util.List<LabTestCatalog> testList = labTestCatalogService.searchByKeywordAndStatus(kw, statusFilter);

        model.addAttribute("testList", testList);
        model.addAttribute("roomList", roomService.findAll());
        model.addAttribute("keyword",  kw);
        model.addAttribute("status",   status);

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
    @org.springframework.transaction.annotation.Transactional
    public String createLabTest(
            @ModelAttribute LabTestCatalog labTestCatalog,
            @RequestParam("youngMin") BigDecimal youngMin,
            @RequestParam("youngMax") BigDecimal youngMax,
            @RequestParam("middleMin") BigDecimal middleMin,
            @RequestParam("middleMax") BigDecimal middleMax,
            @RequestParam("elderMin") BigDecimal elderMin,
            @RequestParam("elderMax") BigDecimal elderMax,
            @RequestParam("pregnantMin") BigDecimal pregnantMin,
            @RequestParam("pregnantMax") BigDecimal pregnantMax,
            @RequestParam("childrenMin") BigDecimal childrenMin,
            @RequestParam("childrenMax") BigDecimal childrenMax) {
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

        // Save thresholds
        saveOrUpdateThreshold(labTestCatalog, "Adult", youngMin, youngMax);
        saveOrUpdateThreshold(labTestCatalog, "Middle-aged", middleMin, middleMax);
        saveOrUpdateThreshold(labTestCatalog, "Elderly", elderMin, elderMax);
        saveOrUpdateThreshold(labTestCatalog, "Pregnant", pregnantMin, pregnantMax);
        saveOrUpdateThreshold(labTestCatalog, "Children", childrenMin, childrenMax);

        return "redirect:/admin/lab-tests";
    }

    /* ── Cập nhật ── */
    @PostMapping("/update/{id}")
    @org.springframework.transaction.annotation.Transactional
    public String updateLabTest(
            @PathVariable("id") String id,
            @ModelAttribute LabTestCatalog labTestCatalog,
            @RequestParam("youngMin") BigDecimal youngMin,
            @RequestParam("youngMax") BigDecimal youngMax,
            @RequestParam("middleMin") BigDecimal middleMin,
            @RequestParam("middleMax") BigDecimal middleMax,
            @RequestParam("elderMin") BigDecimal elderMin,
            @RequestParam("elderMax") BigDecimal elderMax,
            @RequestParam("pregnantMin") BigDecimal pregnantMin,
            @RequestParam("pregnantMax") BigDecimal pregnantMax,
            @RequestParam("childrenMin") BigDecimal childrenMin,
            @RequestParam("childrenMax") BigDecimal childrenMax) {
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

            // Update thresholds
            saveOrUpdateThreshold(labTestCatalog, "Adult", youngMin, youngMax);
            saveOrUpdateThreshold(labTestCatalog, "Middle-aged", middleMin, middleMax);
            saveOrUpdateThreshold(labTestCatalog, "Elderly", elderMin, elderMax);
            saveOrUpdateThreshold(labTestCatalog, "Pregnant", pregnantMin, pregnantMax);
            saveOrUpdateThreshold(labTestCatalog, "Children", childrenMin, childrenMax);
        }

        return "redirect:/admin/lab-tests";
    }

    /* ── Xóa ── */
    @PostMapping("/delete/{id}")
    @org.springframework.transaction.annotation.Transactional
    public String deleteLabTest(@PathVariable("id") String id) {
        List<IndicatorThreshold> thresholds = indicatorThresholdRepository.findByLabTest_LabTestId(id);
        indicatorThresholdRepository.deleteAll(thresholds);
        labTestCatalogService.deleteById(id);
        return "redirect:/admin/lab-tests";
    }

    /* ── Chi tiết (JSON cho JS) ── */
    @GetMapping("/detail/{id}")
    @ResponseBody
    public Map<String, Object> getLabTestDetail(@PathVariable("id") String id) {
        LabTestCatalog test = labTestCatalogService.findById(id).orElse(null);
        if (test == null) {
            return Collections.emptyMap();
        }

        Map<String, Object> detail = new HashMap<>();
        detail.put("labTestId", test.getLabTestId());
        detail.put("testName", test.getTestName());
        detail.put("unit", test.getUnit());
        detail.put("roomId", test.getRoomId());
        detail.put("description", test.getDescription());
        detail.put("status", test.getStatus());

        // Load thresholds
        List<IndicatorThreshold> thresholds = indicatorThresholdRepository.findByLabTest_LabTestId(id);
        for (IndicatorThreshold t : thresholds) {
            String type = t.getPatientType().getTypeName().toLowerCase();
            String keyPrefix = type;
            if ("adult".equals(type)) {
                keyPrefix = "young";
            } else if ("middle-aged".equals(type)) {
                keyPrefix = "middle";
            } else if ("elderly".equals(type)) {
                keyPrefix = "elder";
            } else if ("children".equals(type)) {
                keyPrefix = "children";
            }
            detail.put(keyPrefix + "Min", t.getMinValue());
            detail.put(keyPrefix + "Max", t.getMaxValue());
        }
        return detail;
    }

    private void saveOrUpdateThreshold(LabTestCatalog test, String typeName, BigDecimal min, BigDecimal max) {
        PatientType patientType = patientTypeRepository.findAll().stream()
                .filter(t -> t.getTypeName().equalsIgnoreCase(typeName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Patient type not found: " + typeName));

        Optional<IndicatorThreshold> existingOpt = indicatorThresholdRepository
                .findByLabTest_LabTestIdAndPatientType_PatientTypeId(test.getLabTestId(), patientType.getPatientTypeId());

        IndicatorThreshold threshold = existingOpt.orElseGet(() -> {
            IndicatorThreshold t = new IndicatorThreshold();
            t.setLabTest(test);
            t.setPatientType(patientType);
            t.setCreatedAt(java.time.LocalDateTime.now());
            return t;
        });

        threshold.setMinValue(min);
        threshold.setMaxValue(max);
        indicatorThresholdRepository.save(threshold);
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