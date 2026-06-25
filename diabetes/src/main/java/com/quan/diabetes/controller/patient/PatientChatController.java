package com.quan.diabetes.controller.patient;

import com.quan.diabetes.entity.*;
import com.quan.diabetes.dto.MedicationReminderView;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Controller
public class PatientChatController extends BasePatientController {

    @GetMapping("/patient/chat")
    public String chat(Model model, HttpSession session) {
        Patient patient = addCommonData(model, session, "chat");

        List<AIConversation> conversations = findConversationsByPatient(patient);
        
        if (conversations.isEmpty() && patient != null) {
            AIConversation newConv = new AIConversation();
            newConv.setAiConversationId(UUID.randomUUID().toString());
            newConv.setPatient(patient);
            newConv.setTopic("Hỗ trợ chăm sóc tiểu đường");
            newConv.setCreatedAt(LocalDateTime.now());
            aiConversationService.create(newConv);
            conversations = List.of(newConv);
        }

        List<AIMessage> messages = conversations.isEmpty()
                ? List.of()
                : findMessagesByConversation(conversations.get(0));

        model.addAttribute("conversations", conversations);
        model.addAttribute("messages", messages);

        return "patient/chat";
    }

    @PostMapping("/patient/chat/send")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> sendMessage(
            @RequestParam("message") String message,
            HttpSession session) {
        
        Map<String, Object> response = new HashMap<>();
        User currentUser = getCurrentUser(session);
        Patient patient = getCurrentPatient(session);

        if (currentUser == null || patient == null) {
            response.put("success", false);
            response.put("error", "Không tìm thấy phiên làm việc của bệnh nhân.");
            return ResponseEntity.ok(response);
        }

        if (message == null || message.trim().isEmpty()) {
            response.put("success", false);
            response.put("error", "Nội dung tin nhắn không được để trống.");
            return ResponseEntity.ok(response);
        }

        List<AIConversation> conversations = findConversationsByPatient(patient);
        AIConversation conversation;
        if (conversations.isEmpty()) {
            conversation = new AIConversation();
            conversation.setAiConversationId(UUID.randomUUID().toString());
            conversation.setPatient(patient);
            conversation.setTopic("Hỗ trợ chăm sóc tiểu đường");
            conversation.setCreatedAt(LocalDateTime.now());
            aiConversationService.create(conversation);
        } else {
            conversation = conversations.get(0);
        }

        AIMessage userMsg = new AIMessage();
        userMsg.setAiConversation(conversation);
        userMsg.setContent(message.trim());
        userMsg.setSender("Patient");
        userMsg.setTime(LocalDateTime.now());
        aiMessageService.create(userMsg);

        String replyText = generateAiReply(patient, message.trim());

        AIMessage aiMsg = new AIMessage();
        aiMsg.setAiConversation(conversation);
        aiMsg.setContent(replyText);
        aiMsg.setSender("AI");
        aiMsg.setTime(LocalDateTime.now());
        aiMessageService.create(aiMsg);

        response.put("success", true);
        response.put("reply", replyText);
        return ResponseEntity.ok(response);
    }

    private String generateAiReply(Patient patient, String userText) {
        String msg = userText.toLowerCase();
        
        List<LabResult> labResults = findLabResultsByPatient(patient);
        
        if (msg.contains("phác đồ") || msg.contains("phac do") || msg.contains("kế hoạch điều trị") 
                || msg.contains("ke hoach dieu tri") || msg.contains("mục tiêu điều trị") 
                || msg.contains("muc tieu dieu tri") || msg.contains("chế độ ăn") || msg.contains("che do an") 
                || msg.contains("tập luyện") || msg.contains("tap luyen") || msg.contains("theo dõi đường huyết") 
                || msg.contains("theo doi duong huyet")) {
            
            List<ClinicalExamination> examinations = findExaminationsByPatient(patient);
            ClinicalExamination latestExam = examinations.isEmpty() ? null : examinations.get(0);
            TreatmentPlan latestTreatmentPlan = null;
            if (latestExam != null) {
                latestTreatmentPlan = treatmentPlanService.findByClinicalExamId(latestExam.getClinicalExamId()).orElse(null);
            }
            
            if (latestTreatmentPlan == null) {
                return "Hiện tại tôi chưa thấy phác đồ điều trị chi tiết nào được bác sĩ thiết lập cho bạn. Hãy tuân thủ hướng dẫn và trao đổi thêm với bác sĩ điều trị trong lần khám tới nhé.";
            }
            
            StringBuilder sb = new StringBuilder();
            sb.append("Dưới đây là Phác đồ điều trị gần nhất của bạn (ngày khám ")
              .append(latestExam.getExamDate() != null ? latestExam.getExamDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "")
              .append("):\n\n");
            
            sb.append("- **Mục tiêu điều trị**: ").append(latestTreatmentPlan.getTreatmentGoal() != null ? latestTreatmentPlan.getTreatmentGoal() : "Chưa cập nhật").append("\n");
            sb.append("- **Chế độ ăn uống**: ").append(latestTreatmentPlan.getDietPlan() != null ? latestTreatmentPlan.getDietPlan() : "Chưa cập nhật").append("\n");
            sb.append("- **Chế độ tập luyện**: ").append(latestTreatmentPlan.getExercisePlan() != null ? latestTreatmentPlan.getExercisePlan() : "Chưa cập nhật").append("\n");
            sb.append("- **Theo dõi đường huyết**: ").append(latestTreatmentPlan.getGlucoseMonitoringPlan() != null ? latestTreatmentPlan.getGlucoseMonitoringPlan() : "Chưa cập nhật").append("\n");
            
            return sb.toString();
        }

        if (msg.contains("chỉ số") || msg.contains("chiso") || msg.contains("hba1c") 
                || msg.contains("glucose") || msg.contains("đường huyết") || msg.contains("duong huyet") 
                || msg.contains("bmi") || msg.contains("cân nặng") || msg.contains("can nang") 
                || msg.contains("chiều cao") || msg.contains("chieu cao") || msg.contains("sức khỏe") 
                || msg.contains("suc khoe") || msg.contains("nguy cơ") || msg.contains("nguy co") 
                || msg.contains("risk") || msg.contains("indicator")) {
            
            String bmi = calculateBmi(patient);
            String bmiStatus = evaluateBmiStatus(patient);
            String hba1cVal = getLatestResultValue(labResults, "hba1c", "N/A");
            String hba1cStatus = evaluateHbA1cStatus(labResults);
            String glucoseVal = getLatestResultValue(labResults, "glucose", "N/A");
            String glucoseStatus = evaluateGlucoseStatus(labResults);
            int riskScore = calculateRiskScore(labResults, patient);
            String riskLevel = getRiskLevel(riskScore);
            String riskDesc = getRiskDescription(riskScore);
            String riskAdvice = getRiskAdvice(riskScore);

            StringBuilder sb = new StringBuilder();
            sb.append("Dưới đây là phân tích các chỉ số sức khỏe gần nhất của bạn:\n\n");
            sb.append("1. Chỉ số thể hình (BMI): ").append(bmi).append(" (Phân loại: ").append(bmiStatus).append(")\n");
            sb.append("2. Chỉ số HbA1c: ").append(hba1cVal);
            if (!"N/A".equals(hba1cVal)) sb.append("%");
            sb.append(" (Trạng thái: ").append(hba1cStatus).append(")\n");
            sb.append("3. Chỉ số Đường huyết đói (Glucose): ").append(glucoseVal);
            if (!"N/A".equals(glucoseVal)) sb.append(" mg/dL");
            sb.append(" (Trạng thái: ").append(glucoseStatus).append(")\n");
            sb.append("4. Đánh giá nguy cơ biến chứng tiểu đường: ").append(riskLevel).append(" (Điểm nguy cơ: ").append(riskScore).append("/100)\n\n");
            sb.append("Chi tiết đánh giá: ").append(riskDesc).append("\n\n");
            sb.append("Lời khuyên y tế dành cho bạn: ").append(riskAdvice);
            return sb.toString();
        }
        
        if (msg.contains("thuốc") || msg.contains("thuoc") || msg.contains("uống thuốc") 
                || msg.contains("uong thuoc") || msg.contains("medication") || msg.contains("medicine") 
                || msg.contains("lịch uống") || msg.contains("lich uong") || msg.contains("đơn thuốc") 
                || msg.contains("don thuoc") || msg.contains("prescription")) {
            
            List<MedicationReminderView> reminders = buildTodayMedicationReminders(patient);
            if (reminders.isEmpty()) {
                return "Hiện tại tôi chưa thấy lịch uống thuốc hay đơn thuốc nào được ghi nhận trên hệ thống của bạn. Hãy tuân thủ hướng dẫn và trao đổi thêm với bác sĩ điều trị nếu có bất kỳ đơn thuốc mới nào nhé.";
            }
            
            StringBuilder sb = new StringBuilder();
            sb.append("Lịch uống thuốc hôm nay của bạn:\n\n");
            
            Set<String> uniqueReminders = new LinkedHashSet<>();
            for (MedicationReminderView reminder : reminders) {
                String desc = "- " + reminder.getMedicationName() + " (" + reminder.getDosage() + ") uống lúc " 
                        + reminder.getEndTime().toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm"))
                        + " (" + reminder.getTimingName() + ")";
                if (reminder.getInstruction() != null && !reminder.getInstruction().isEmpty()) {
                    desc += " - Lưu ý: " + reminder.getInstruction();
                }
                if (reminder.getMedicationPlan() != null && !reminder.getMedicationPlan().isEmpty()) {
                    desc += " (Kế hoạch: " + reminder.getMedicationPlan() + ")";
                }
                uniqueReminders.add(desc);
            }
            
            for (String r : uniqueReminders) {
                sb.append(r).append("\n");
            }
            
            sb.append("\nBạn nên đặt báo thức hoặc bật thông báo trên trình duyệt để không quên giờ uống thuốc nhé!");
            return sb.toString();
        }
        
        if (msg.contains("thói quen") || msg.contains("routine") || msg.contains("sinh hoạt") 
                || msg.contains("giờ giấc") || msg.contains("gio giac") || msg.contains("ăn uống") 
                || msg.contains("an uong") || msg.contains("ngủ") || msg.contains("ngu") 
                || msg.contains("bữa ăn") || msg.contains("bua an")) {
            
            PatientRoutine routine = findRoutineByPatient(patient);
            if (routine == null) {
                return "Hiện tại bạn chưa cập nhật lịch sinh hoạt hàng ngày trên hệ thống. Bạn vui lòng vào mục 'Hồ sơ cá nhân' để cập nhật giờ giấc sinh hoạt nhé.";
            }
            
            StringBuilder sb = new StringBuilder();
            sb.append("Lịch sinh hoạt hàng ngày hiện tại của bạn:\n");
            sb.append("- Thức dậy: ").append(routine.getWakeUpTime()).append("\n");
            sb.append("- Ăn sáng: ").append(routine.getBreakfastTime()).append("\n");
            sb.append("- Ăn trưa: ").append(routine.getLunchTime()).append("\n");
            sb.append("- Ăn tối: ").append(routine.getDinnerTime()).append("\n");
            sb.append("- Đi ngủ: ").append(routine.getSleepTime()).append("\n\n");
            sb.append("Việc duy trì giờ ăn và ngủ đều đặn rất quan trọng để giúp cơ thể ổn định lượng đường huyết. Hãy cố gắng tuân thủ khung giờ này nhé!");
            return sb.toString();
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("Xin chào! Tôi là Trợ lý AI Chăm sóc Tiểu đường của bạn.\n\n");
        sb.append("Tôi có thể giúp bạn kiểm tra thông tin sức khỏe cá nhân bất cứ lúc nào. Bạn có thể hỏi tôi các câu hỏi như:\n");
        sb.append("- \"Chỉ số sức khỏe của tôi thế nào?\" để kiểm tra BMI, HbA1c, Glucose và đánh giá mức độ nguy cơ tiểu đường.\n");
        sb.append("- \"Lịch uống thuốc hôm nay của tôi\" để xem các loại thuốc cần uống và thời gian uống.\n");
        sb.append("- \"Lịch sinh hoạt của tôi\" để xem lại các khung giờ ăn ngủ đã thiết lập.\n");
        sb.append("- \"Phác đồ điều trị của tôi như thế nào?\" để xem chi tiết chế độ dinh dưỡng, luyện tập và mục tiêu điều trị của bác sĩ.\n\n");
        sb.append("Ngoài ra, hãy luôn nhớ các nguyên tắc tự chăm sóc tiểu đường cơ bản:\n");
        sb.append("- Hạn chế tinh bột tinh chế, đồ ngọt, nước có ga.\n");
        sb.append("- Ăn nhiều rau xanh, chất xơ và bổ sung protein vừa phải.\n");
        sb.append("- Duy trì vận động tối thiểu 30 phút mỗi ngày.\n");
        sb.append("- Uống đủ nước và tránh thức khuya.\n\n");
        sb.append("Hôm nay bạn cần tôi hỗ trợ thông tin gì không?");
        return sb.toString();
    }
}
