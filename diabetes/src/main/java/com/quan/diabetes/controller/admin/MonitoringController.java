package com.quan.diabetes.controller.admin;

import com.quan.diabetes.monitoring.dto.AiPatientAccessLogDto;
import com.quan.diabetes.monitoring.service.AiMonitoringService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.quan.diabetes.entity.AIAssistant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

@Controller
@RequestMapping("/admin/monitoring")
public class MonitoringController {

    @Autowired
    private AiMonitoringService aiMonitoringService;

    @Value("${ollama.model:diabetes}")
    private String activeModelName;

    @GetMapping
    public String monitoringPage(
            @RequestParam(defaultValue = "access") String activeTab,
            @RequestParam(defaultValue = "0") int accessPage,
            @RequestParam(defaultValue = "7") int accessSize,
            @RequestParam(required = false) String accessPatientId,
            @RequestParam(required = false) String dataType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate accessFromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate accessToDate,
            Model model
    ) {
        LocalDateTime accessFromDateTime = accessFromDate != null ? accessFromDate.atStartOfDay() : null;
        LocalDateTime accessToDateTime = accessToDate != null ? accessToDate.atTime(23, 59, 59) : null;

        Pageable accessPageable = PageRequest.of(accessPage, accessSize, Sort.by(Sort.Direction.DESC, "accessedAt"));
        Page<AiPatientAccessLogDto> accessLogs = aiMonitoringService.getPatientAccessLogs(accessPatientId, dataType, accessFromDateTime, accessToDateTime, accessPageable);

        Double avgLatencyMs = aiMonitoringService.getAverageLatencyMs();
        Long todayUsersCount = aiMonitoringService.countDistinctPatientsToday();

        AIAssistant activeAssistant = aiMonitoringService.getActiveAssistant();
        List<AIAssistant> allAssistants = aiMonitoringService.getAllAssistants();
        String displayModel = (activeAssistant != null && activeAssistant.getAiName() != null)
                ? activeAssistant.getAiName() + " (" + activeAssistant.getModelName() + ")"
                : activeModelName;

        model.addAttribute("avgLatencyMs", avgLatencyMs);
        model.addAttribute("todayUsersCount", todayUsersCount);
        model.addAttribute("activeModelName", displayModel);
        model.addAttribute("activeAssistant", activeAssistant);
        model.addAttribute("allAssistants", allAssistants);
        model.addAttribute("aiEnabled", aiMonitoringService.isAiEnabled());

        model.addAttribute("activeTab", activeTab);
        model.addAttribute("accessLogs", accessLogs);

        model.addAttribute("accessPatientId", accessPatientId);
        model.addAttribute("dataType", dataType);
        model.addAttribute("accessFromDate", accessFromDate);
        model.addAttribute("accessToDate", accessToDate);

        return "Admin/monitoring";
    }

    @PostMapping("/api/toggle-ai")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> toggleAiApi(@RequestParam boolean enabled) {
        aiMonitoringService.setAiEnabled(enabled);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("aiEnabled", enabled);
        response.put("message", enabled ? "Đã bật Trợ lý AI!" : "Đã tạm tắt Trợ lý AI!");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/api/switch-model")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> switchModelApi(@RequestParam Integer assistantId) {
        try {
            AIAssistant active = aiMonitoringService.switchActiveAssistant(assistantId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("activeAssistant", active);
            response.put("message", "Đã chuyển đổi sang Model AI: " + active.getAiName() + " (" + active.getModelName() + ")");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Lỗi khi chuyển đổi Model AI: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
