package com.quan.diabetes.controller.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quan.diabetes.entity.SystemLog;
import com.quan.diabetes.service.systemlog.SystemLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/admin/system-log")
public class SystemLogController {

    @Autowired
    private SystemLogService systemLogService;

    @Autowired
    private ObjectMapper objectMapper;

    // Các tham số lọc, ánh xạ tên hiển thị
    private static final Map<String, String> ROLE_MAP = new HashMap<>();
    private static final Map<String, String> ACTION_MAP = new HashMap<>();
    private static final Map<String, String> STATUS_MAP = new HashMap<>();

    static {
        ROLE_MAP.put("AD", "Quản trị viên");
        ROLE_MAP.put("DOC", "Bác sĩ");
        ROLE_MAP.put("PAT", "Bệnh nhân");

        ACTION_MAP.put("LOGIN", "Đăng nhập");
        ACTION_MAP.put("REGISTER", "Đăng ký");
        ACTION_MAP.put("CREATE", "Thêm mới");
        ACTION_MAP.put("UPDATE", "Cập nhật");
        ACTION_MAP.put("DELETE", "Xóa");
        ACTION_MAP.put("LOCK", "Khóa");
        ACTION_MAP.put("UNLOCK", "Mở khóa");
        ACTION_MAP.put("RESET_PASSWORD", "Đặt lại mật khẩu");
        ACTION_MAP.put("CREATE_MEDICAL_REQUEST", "Tạo yêu cầu khám");
        ACTION_MAP.put("APPROVE_MEDICAL_REQUEST", "Duyệt yêu cầu khám");
        ACTION_MAP.put("REJECT_MEDICAL_REQUEST", "Từ chối yêu cầu khám");
        ACTION_MAP.put("APPROVE_MEDICAL_RECORD", "Tiếp nhận bệnh án");
        ACTION_MAP.put("REJECT_MEDICAL_RECORD", "Từ chối bệnh án");
        ACTION_MAP.put("UPDATE_MEDICAL_RECORD", "Cập nhật bệnh án");
        ACTION_MAP.put("COMPLETE_MEDICAL_RECORD", "Hoàn thành bệnh án");
        ACTION_MAP.put("UPDATE_PROFILE", "Cập nhật hồ sơ");
        ACTION_MAP.put("UPDATE_ROUTINE", "Cập nhật thói quen");
        ACTION_MAP.put("DELETE_ROUTINE", "Xóa thói quen");

        STATUS_MAP.put("SUCCESS", "Thành công");
        STATUS_MAP.put("FAILED", "Thất bại");
    }

    @GetMapping
    public String listSystemLogs(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) LocalDate fromDate,
            @RequestParam(required = false) LocalDate toDate,
            @RequestParam(defaultValue = "1") int page,
            Model model) {

        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
            model.addAttribute("error", "Từ ngày không được lớn hơn Đến ngày!");
            model.addAttribute("logPage", Page.empty());
            model.addAttribute("keyword", keyword);
            model.addAttribute("role", role);
            model.addAttribute("action", action);
            model.addAttribute("fromDate", null);
            model.addAttribute("toDate", null);
            model.addAttribute("roleMap", ROLE_MAP);
            model.addAttribute("actionMap", ACTION_MAP);
            model.addAttribute("statusMap", STATUS_MAP);
            return "admin/system_log_list";
        }

        try {
            int pageSize = 7;
            Pageable pageable = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));
            Page<SystemLog> logPage = systemLogService.getLogs(keyword, role, action, fromDate, toDate, pageable);

            model.addAttribute("logPage", logPage);
            model.addAttribute("keyword", keyword);
            model.addAttribute("role", role);
            model.addAttribute("action", action);
            model.addAttribute("fromDate", fromDate);
            model.addAttribute("toDate", toDate);

            // Gửi bộ từ điển để hiển thị tiếng Việt trên giao diện
            model.addAttribute("roleMap", ROLE_MAP);
            model.addAttribute("actionMap", ACTION_MAP);
            model.addAttribute("statusMap", STATUS_MAP);

            return "admin/system_log_list";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            // Trả về dữ liệu rỗng để không bị sập trang và reset ngày không hợp lệ
            model.addAttribute("logPage", Page.empty());
            model.addAttribute("keyword", keyword);
            model.addAttribute("role", role);
            model.addAttribute("action", action);
            model.addAttribute("fromDate", null);
            model.addAttribute("toDate", null);
            model.addAttribute("roleMap", ROLE_MAP);
            model.addAttribute("actionMap", ACTION_MAP);
            model.addAttribute("statusMap", STATUS_MAP);
            return "admin/system_log_list";
        }
    }

    @GetMapping("/detail/{id}")
    public String logDetail(@PathVariable Integer id, Model model) {
        Optional<SystemLog> logOpt = systemLogService.findById(id);
        if (logOpt.isEmpty()) {
            return "redirect:/admin/system-log";
        }
        SystemLog log = logOpt.get();
        model.addAttribute("log", log);

        // Format JSON OldValue, NewValue
        String formattedOldValue = formatJson(log.getOldValue());
        String formattedNewValue = formatJson(log.getNewValue());

        model.addAttribute("formattedOldValue", formattedOldValue);
        model.addAttribute("formattedNewValue", formattedNewValue);

        // Map
        model.addAttribute("roleMap", ROLE_MAP);
        model.addAttribute("actionMap", ACTION_MAP);
        model.addAttribute("statusMap", STATUS_MAP);

        return "admin/system_log_detail";
    }

    private String formatJson(String jsonString) {
        if (jsonString == null || jsonString.isEmpty() || jsonString.equals("null")) {
            return "Không có dữ liệu";
        }
        try {
            Object jsonObject = objectMapper.readValue(jsonString, Object.class);
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonObject);
        } catch (Exception e) {
            return jsonString; // Nếu không phải JSON hợp lệ thì in thẳng ra
        }
    }
}
