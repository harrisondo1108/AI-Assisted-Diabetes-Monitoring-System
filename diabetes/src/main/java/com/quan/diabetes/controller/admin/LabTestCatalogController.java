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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
    @GetMapping({"", "/"})
    public String showLabTests(@RequestParam(value = "keyword", defaultValue = "") String keyword,
                               @RequestParam(value = "status",  defaultValue = "all") String status,
                               @RequestParam(value = "error",   required = false) String error,
                               @RequestParam(value = "success", required = false) String success,
                               Model model) {
        Boolean statusFilter = null;
        if ("active".equals(status)) {
            statusFilter = Boolean.TRUE;
        } else if ("inactive".equals(status)) {
            statusFilter = Boolean.FALSE;
        }
        
        String kw = keyword.trim();
        java.util.List<LabTestCatalog> testList = labTestCatalogService.searchByKeywordAndStatus(kw, statusFilter);
        java.util.List<LabTestCatalog> allTests = labTestCatalogService.findAll();

        long statTotal = allTests.size();
        long statActive = allTests.stream().filter(t -> Boolean.TRUE.equals(t.getStatus())).count();
        long statInactive = allTests.stream().filter(t -> Boolean.FALSE.equals(t.getStatus())).count();

        model.addAttribute("testList", testList);
        model.addAttribute("roomList", roomService.findAll());
        model.addAttribute("keyword",  kw);
        model.addAttribute("status",   status);
        model.addAttribute("statTotal", statTotal);
        model.addAttribute("statActive", statActive);
        model.addAttribute("statInactive", statInactive);

        if (error != null && !error.isEmpty()) {
            if ("duplicate".equals(error))
                model.addAttribute("errorMessage", "Tên xét nghiệm đã tồn tại.");
            else if ("empty".equals(error))
                model.addAttribute("errorMessage", "Vui lòng nhập đầy đủ dữ liệu.");
            else if ("room".equals(error))
                model.addAttribute("errorMessage", "Vui lòng chọn phòng xét nghiệm.");
            else
                model.addAttribute("errorMessage", error);
        }

        if (success != null && !success.isEmpty()) {
            model.addAttribute("successMessage", success);
        }

        return "Admin/LabTest";
    }

    private boolean containsInvalidChars(String str) {
        if (str == null) return false;
        return str.matches(".*[<>;'\"\\\\`\\$^\\{\\}~\\|\\[\\]].*");
    }

    private String validateLabTestForm(
            LabTestCatalog labTestCatalog,
            String currentId,
            BigDecimal youngMin, BigDecimal youngMax,
            BigDecimal middleMin, BigDecimal middleMax,
            BigDecimal elderMin, BigDecimal elderMax,
            BigDecimal pregnantMin, BigDecimal pregnantMax,
            BigDecimal childrenMin, BigDecimal childrenMax) {

        String testName = labTestCatalog.getTestName();
        if (testName == null || testName.trim().isEmpty()) {
            return "Vui lòng nhập Tên xét nghiệm!";
        }
        testName = testName.trim();
        if (testName.length() > 100) {
            return "Tên xét nghiệm không được vượt quá 100 ký tự!";
        }
        if (containsInvalidChars(testName)) {
            return "Tên xét nghiệm không được chứa các ký tự đặc biệt nguy hiểm (< > ; ' \" \\ `)!";
        }

        // Kiểm tra trùng tên
        if (currentId == null) {
            if (labTestCatalogService.existsByTestName(testName)) {
                return "Tên xét nghiệm \"" + testName + "\" đã tồn tại trong hệ thống!";
            }
        } else {
            if (labTestCatalogService.existsByTestNameAndLabTestIdNot(testName, currentId)) {
                return "Tên xét nghiệm \"" + testName + "\" đã bị trùng với xét nghiệm khác!";
            }
        }

        // Kiểm tra đơn vị
        String unit = labTestCatalog.getUnit();
        if (unit == null || unit.trim().isEmpty()) {
            return "Vui lòng nhập Đơn vị xét nghiệm!";
        }
        unit = unit.trim();
        if (unit.length() > 20) {
            return "Đơn vị xét nghiệm không được vượt quá 20 ký tự!";
        }
        if (containsInvalidChars(unit)) {
            return "Đơn vị không được chứa các ký tự đặc biệt nguy hiểm!";
        }

        // Kiểm tra phòng xét nghiệm
        if (labTestCatalog.getRoomId() == null) {
            return "Vui lòng chọn Phòng xét nghiệm!";
        }

        // Kiểm tra mô tả
        String desc = labTestCatalog.getDescription();
        if (desc != null && !desc.trim().isEmpty()) {
            if (desc.trim().length() > 255) {
                return "Mô tả không được vượt quá 255 ký tự!";
            }
            if (containsInvalidChars(desc)) {
                return "Mô tả không được chứa các ký tự đặc biệt nguy hiểm!";
            }
        }

        // Kiểm tra ngưỡng Min < Max cho từng nhóm đối tượng
        return validateThresholds(
                youngMin, youngMax, "Người trẻ",
                middleMin, middleMax, "Trung niên",
                elderMin, elderMax, "Cao tuổi",
                pregnantMin, pregnantMax, "Người có bầu",
                childrenMin, childrenMax, "Trẻ em"
        );
    }

    private String validateThresholds(Object... pairs) {
        for (int i = 0; i < pairs.length; i += 3) {
            BigDecimal min = (BigDecimal) pairs[i];
            BigDecimal max = (BigDecimal) pairs[i + 1];
            String groupName = (String) pairs[i + 2];

            if (min == null || max == null) {
                return "Vui lòng nhập đầy đủ giá trị Min và Max cho nhóm [" + groupName + "]!";
            }
            if (min.compareTo(BigDecimal.ZERO) < 0 || max.compareTo(BigDecimal.ZERO) < 0) {
                return "Ngưỡng Min/Max của nhóm [" + groupName + "] không được nhỏ hơn 0!";
            }
            if (min.compareTo(max) >= 0) {
                return "Lỗi ngưỡng chỉ số [" + groupName + "]: Giá trị Min (" + min + ") phải nhỏ hơn Max (" + max + ")!";
            }
        }
        return null;
    }

    /* ── Tạo mới ── */
    @PostMapping("/create")
    @org.springframework.transaction.annotation.Transactional
    public String createLabTest(
            @ModelAttribute LabTestCatalog labTestCatalog,
            @RequestParam(value = "youngMin", required = false) BigDecimal youngMin,
            @RequestParam(value = "youngMax", required = false) BigDecimal youngMax,
            @RequestParam(value = "middleMin", required = false) BigDecimal middleMin,
            @RequestParam(value = "middleMax", required = false) BigDecimal middleMax,
            @RequestParam(value = "elderMin", required = false) BigDecimal elderMin,
            @RequestParam(value = "elderMax", required = false) BigDecimal elderMax,
            @RequestParam(value = "pregnantMin", required = false) BigDecimal pregnantMin,
            @RequestParam(value = "pregnantMax", required = false) BigDecimal pregnantMax,
            @RequestParam(value = "childrenMin", required = false) BigDecimal childrenMin,
            @RequestParam(value = "childrenMax", required = false) BigDecimal childrenMax,
            RedirectAttributes redirectAttributes) {

        String errorMsg = validateLabTestForm(labTestCatalog, null,
                youngMin, youngMax, middleMin, middleMax, elderMin, elderMax, pregnantMin, pregnantMax, childrenMin, childrenMax);

        if (errorMsg != null) {
            redirectAttributes.addAttribute("error", errorMsg);
            return "redirect:/admin/lab-tests";
        }

        labTestCatalog.setTestName(labTestCatalog.getTestName().trim());
        labTestCatalog.setUnit(labTestCatalog.getUnit().trim());
        if (labTestCatalog.getDescription() != null) {
            labTestCatalog.setDescription(labTestCatalog.getDescription().trim());
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

        redirectAttributes.addAttribute("success", "Định nghĩa xét nghiệm \"" + labTestCatalog.getTestName() + "\" thành công!");
        return "redirect:/admin/lab-tests";
    }

    /* ── Cập nhật ── */
    @PostMapping("/update/{id}")
    @org.springframework.transaction.annotation.Transactional
    public String updateLabTest(
            @PathVariable("id") String id,
            @ModelAttribute LabTestCatalog labTestCatalog,
            @RequestParam(value = "youngMin", required = false) BigDecimal youngMin,
            @RequestParam(value = "youngMax", required = false) BigDecimal youngMax,
            @RequestParam(value = "middleMin", required = false) BigDecimal middleMin,
            @RequestParam(value = "middleMax", required = false) BigDecimal middleMax,
            @RequestParam(value = "elderMin", required = false) BigDecimal elderMin,
            @RequestParam(value = "elderMax", required = false) BigDecimal elderMax,
            @RequestParam(value = "pregnantMin", required = false) BigDecimal pregnantMin,
            @RequestParam(value = "pregnantMax", required = false) BigDecimal pregnantMax,
            @RequestParam(value = "childrenMin", required = false) BigDecimal childrenMin,
            @RequestParam(value = "childrenMax", required = false) BigDecimal childrenMax,
            RedirectAttributes redirectAttributes) {

        LabTestCatalog existing = labTestCatalogService.findById(id).orElse(null);
        if (existing == null) {
            redirectAttributes.addAttribute("error", "Không tìm thấy xét nghiệm cần cập nhật!");
            return "redirect:/admin/lab-tests";
        }

        String errorMsg = validateLabTestForm(labTestCatalog, id,
                youngMin, youngMax, middleMin, middleMax, elderMin, elderMax, pregnantMin, pregnantMax, childrenMin, childrenMax);

        if (errorMsg != null) {
            redirectAttributes.addAttribute("error", errorMsg);
            return "redirect:/admin/lab-tests";
        }

        labTestCatalog.setLabTestId(id);
        labTestCatalog.setTestName(labTestCatalog.getTestName().trim());
        labTestCatalog.setUnit(labTestCatalog.getUnit().trim());
        if (labTestCatalog.getDescription() != null) {
            labTestCatalog.setDescription(labTestCatalog.getDescription().trim());
        }
        labTestCatalog.setStatus(existing.getStatus());
        labTestCatalogService.update(id, labTestCatalog);

        // Update thresholds
        saveOrUpdateThreshold(labTestCatalog, "Adult", youngMin, youngMax);
        saveOrUpdateThreshold(labTestCatalog, "Middle-aged", middleMin, middleMax);
        saveOrUpdateThreshold(labTestCatalog, "Elderly", elderMin, elderMax);
        saveOrUpdateThreshold(labTestCatalog, "Pregnant", pregnantMin, pregnantMax);
        saveOrUpdateThreshold(labTestCatalog, "Children", childrenMin, childrenMax);

        redirectAttributes.addAttribute("success", "Cập nhật xét nghiệm \"" + labTestCatalog.getTestName() + "\" thành công!");
        return "redirect:/admin/lab-tests";
    }

    /* ── Xóa ── */
    @PostMapping("/delete/{id}")
    @org.springframework.transaction.annotation.Transactional
    public String deleteLabTest(@PathVariable("id") String id, RedirectAttributes redirectAttributes) {
        LabTestCatalog test = labTestCatalogService.findById(id).orElse(null);
        String name = test != null ? test.getTestName() : id;
        List<IndicatorThreshold> thresholds = indicatorThresholdRepository.findByLabTest_LabTestId(id);
        indicatorThresholdRepository.deleteAll(thresholds);
        labTestCatalogService.deleteById(id);
        redirectAttributes.addAttribute("success", "Đã xóa xét nghiệm \"" + name + "\" khỏi hệ thống!");
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
                .orElseGet(() -> {
                    PatientType newPt = new PatientType();
                    newPt.setTypeName(typeName);
                    if ("Children".equalsIgnoreCase(typeName) || "Child".equalsIgnoreCase(typeName)) {
                        newPt.setMinAge(0);
                        newPt.setMaxAge(17);
                    }
                    return patientTypeRepository.save(newPt);
                });

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
    public String toggleStatus(@PathVariable("id") String id, RedirectAttributes redirectAttributes) {
        LabTestCatalog test = labTestCatalogService.findById(id).orElse(null);
        if (test != null) {
            Boolean current = test.getStatus();
            boolean newStatus = (current == null || !current);
            test.setStatus(newStatus);
            labTestCatalogService.update(id, test);
            String statusText = newStatus ? "Kích hoạt" : "Vô hiệu hóa";
            redirectAttributes.addAttribute("success", statusText + " xét nghiệm \"" + test.getTestName() + "\" thành công!");
        } else {
            redirectAttributes.addAttribute("error", "Không tìm thấy xét nghiệm!");
        }
        return "redirect:/admin/lab-tests";
    }
}