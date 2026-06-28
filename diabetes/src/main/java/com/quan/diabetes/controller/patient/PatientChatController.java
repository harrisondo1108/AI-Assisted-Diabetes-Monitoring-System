package com.quan.diabetes.controller.patient;

import com.quan.diabetes.entity.*;
import com.quan.diabetes.dto.MedicationReminderView;
import com.quan.diabetes.service.ai.AIChatService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.beans.factory.annotation.Autowired;

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

    @Autowired
    private AIChatService aiChatService;

    // ===== API =====
    @PostMapping("/patient/chat/api/send")
    @ResponseBody
    public com.quan.diabetes.dto.AIChat.ChatResponseDto sendMessage(@RequestBody com.quan.diabetes.dto.AIChat.ChatRequestDto request) {
        return aiChatService.sendMessage(request);
    }

    @PostMapping("/patient/chat/api/send/assistant/{assistantId}")
    @ResponseBody
    public com.quan.diabetes.dto.AIChat.ChatResponseDto sendMessageWithAssistant(
            @RequestBody com.quan.diabetes.dto.AIChat.ChatRequestDto request,
            @PathVariable Integer assistantId) {
        return aiChatService.sendMessageWithAssistant(request, assistantId);
    }

    @GetMapping("/patient/chat/api/history/{conversationId}")
    @ResponseBody
    public com.quan.diabetes.dto.AIChat.ConversationHistoryDto getConversationHistory(@PathVariable String conversationId) {
        return aiChatService.getConversationHistory(conversationId);
    }

    @GetMapping("/patient/chat/api/patient/{patientId}/conversations")
    @ResponseBody
    public List<com.quan.diabetes.dto.AIChat.ConversationHistoryDto> getPatientConversations(@PathVariable String patientId) {
        return aiChatService.getPatientConversations(patientId);
    }

    @GetMapping("/patient/chat/api/patient/{patientId}/conversations/assistant/{assistantId}")
    @ResponseBody
    public List<com.quan.diabetes.dto.AIChat.ConversationHistoryDto> getPatientConversationsWithAssistant(
            @PathVariable String patientId,
            @PathVariable Integer assistantId) {
        return aiChatService.getPatientConversationsWithAssistant(patientId, assistantId);
    }

    @DeleteMapping("/patient/chat/api/conversation/{conversationId}")
    @ResponseBody
    public void deleteConversation(@PathVariable String conversationId) {
        aiChatService.deleteConversation(conversationId);
    }

    @GetMapping("/patient/chat/api/assistants")
    @ResponseBody
    public List<com.quan.diabetes.dto.AIChat.AIAssistantDto> getAvailableAssistants() {
        return aiChatService.getAvailableAssistants();
    }
}
