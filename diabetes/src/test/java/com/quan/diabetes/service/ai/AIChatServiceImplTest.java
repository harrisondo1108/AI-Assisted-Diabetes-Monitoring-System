package com.quan.diabetes.service.ai;

import com.quan.diabetes.dto.AIChat.AIAssistantDto;
import com.quan.diabetes.dto.AIChat.AiChatRequestDto;
import com.quan.diabetes.dto.AIChat.ChatResponseDto;
import com.quan.diabetes.dto.AIChat.ConversationHistoryDto;
import com.quan.diabetes.entity.AIAssistant;
import com.quan.diabetes.entity.AIConversation;
import com.quan.diabetes.entity.AIMessage;
import com.quan.diabetes.entity.Patient;
import com.quan.diabetes.service.ai.AIAssistantService;
import com.quan.diabetes.service.ai.AIConversationService;
import com.quan.diabetes.service.ai.AIMessageService;
import com.quan.diabetes.service.ai.AiTool;
import com.quan.diabetes.service.ai.impl.AIChatServiceImpl;
import com.quan.diabetes.service.user.PatientService;
import com.quan.diabetes.monitoring.service.AiMonitoringService;
import com.quan.diabetes.aiAPI.manager.AiProviderManager;
import com.quan.diabetes.aiAPI.dto.AiGenerateOptions;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AIChatServiceImplTest {

    @Mock private AIMessageService aiMessageService;
    @Mock private AIConversationService aiConversationService;
    @Mock private AIAssistantService aiAssistantService;
    @Mock private PatientService patientService;
    @Mock private AiTool aiTool;
    @Mock private AiMonitoringService aiMonitoringService;
    @Mock private AiProviderManager aiProviderManager;

    @InjectMocks
    private AIChatServiceImpl service;

    private Patient patient;
    private AIAssistant assistant;
    private AIConversation conversation;
    private AiChatRequestDto requestDto;

    @BeforeEach
    void setUp() {
        patient = new Patient();
        patient.setUserId("PAT-01");

        assistant = new AIAssistant();
        assistant.setAiAssistantId(1);
        assistant.setAiName("Specialist");
        assistant.setModelName("diabetes");
        assistant.setStatus("Active");

        conversation = new AIConversation();
        conversation.setAiConversationId("CONV-01");
        conversation.setPatient(patient);
        conversation.setAiAssistant(assistant);

        requestDto = new AiChatRequestDto("hello", "CONV-01", "PAT-01");

        ReflectionTestUtils.setField(service, "ollamaDefaultModel", "diabetes");
    }

    @Test
    void testSendMessage_AiDisabled() {
        when(aiMonitoringService.isAiEnabled()).thenReturn(false);
        ChatResponseDto res = service.sendMessage(requestDto);
        assertNotNull(res);
        assertTrue(res.message().contains("tạm tắt"));
    }

    @Test
    void testSendMessage_PatientNotFound() {
        when(aiMonitoringService.isAiEnabled()).thenReturn(true);
        when(patientService.findById("PAT-01")).thenReturn(Optional.empty());

        ChatResponseDto res = service.sendMessage(requestDto);
        assertNotNull(res);
        assertTrue(res.error().contains("Failed to process message"));
    }

    @Test
    void testSendMessage_SuccessGreeting() {
        when(aiMonitoringService.isAiEnabled()).thenReturn(true);
        when(patientService.findById("PAT-01")).thenReturn(Optional.of(patient));
        when(aiConversationService.findById("CONV-01")).thenReturn(Optional.of(conversation));
        when(aiAssistantService.getOrCreateDefaultAssistant()).thenReturn(assistant);

        when(aiProviderManager.generateWithModel(anyString(), anyString(), anyString(), any()))
                .thenReturn("Xin chào bạn!");

        ChatResponseDto res = service.sendMessage(requestDto);
        assertNotNull(res);
        assertEquals("Xin chào bạn!", res.message());
        verify(aiMessageService, times(2)).create(any()); // User message + AI message
    }

    @Test
    void testSendMessage_SuccessRAGPrescription_NoData() {
        requestDto = new AiChatRequestDto("Xem đơn thuốc của tôi", "CONV-01", "PAT-01");
        when(aiMonitoringService.isAiEnabled()).thenReturn(true);
        when(patientService.findById("PAT-01")).thenReturn(Optional.of(patient));
        when(aiConversationService.findById("CONV-01")).thenReturn(Optional.of(conversation));
        when(aiAssistantService.getOrCreateDefaultAssistant()).thenReturn(assistant);
        when(aiTool.getPrescriptions("PAT-01")).thenReturn("DANH SÁCH THUỐC TRONG ĐƠN THUỐC CỦA BỆNH NHÂN:\n(Không có dữ liệu)");

        ChatResponseDto res = service.sendMessage(requestDto);
        assertNotNull(res);
        assertTrue(res.message().contains("chưa có bản ghi"));
    }

    @Test
    void testSendMessage_SuccessRAGPrescription_WithData() {
        requestDto = new AiChatRequestDto("Xem đơn thuốc của tôi", "CONV-01", "PAT-01");
        when(aiMonitoringService.isAiEnabled()).thenReturn(true);
        when(patientService.findById("PAT-01")).thenReturn(Optional.of(patient));
        when(aiConversationService.findById("CONV-01")).thenReturn(Optional.of(conversation));
        when(aiAssistantService.getOrCreateDefaultAssistant()).thenReturn(assistant);
        when(aiTool.getPrescriptions("PAT-01")).thenReturn("DANH SÁCH THUỐC TRONG ĐƠN THUỐC CỦA BỆNH NHÂN:\n1. Metformin");
        when(aiProviderManager.generateWithModel(anyString(), anyString(), anyString(), any()))
                .thenReturn("Đây là đơn thuốc của bạn: Metformin");

        ChatResponseDto res = service.sendMessage(requestDto);
        assertNotNull(res);
        assertTrue(res.message().contains("Metformin"));
    }

    @Test
    void testSendMessage_SuccessFollowUpCheck() {
        requestDto = new AiChatRequestDto("Nó có an toàn không", "CONV-01", "PAT-01");
        when(aiMonitoringService.isAiEnabled()).thenReturn(true);
        when(patientService.findById("PAT-01")).thenReturn(Optional.of(patient));
        when(aiConversationService.findById("CONV-01")).thenReturn(Optional.of(conversation));
        when(aiAssistantService.getOrCreateDefaultAssistant()).thenReturn(assistant);
        // mock history containing "đơn"
        when(aiMessageService.getFormattedConversationHistory("CONV-01", 3))
                .thenReturn("Bệnh nhân: đơn thuốc Metformin");
        when(aiTool.getPrescriptions("PAT-01")).thenReturn("DANH SÁCH THUỐC TRONG ĐƠN THUỐC CỦA BỆNH NHÂN:\n1. Metformin");
        when(aiProviderManager.generateWithModel(anyString(), anyString(), anyString(), any()))
                .thenReturn("Hãy uống Metformin sau ăn.");

        ChatResponseDto res = service.sendMessage(requestDto);
        assertNotNull(res);
        assertTrue(res.message().contains("Metformin"));
    }

    @Test
    void testSendMessage_CreateConversation() {
        requestDto = new AiChatRequestDto("hello", null, "PAT-01"); // null conv id
        when(aiMonitoringService.isAiEnabled()).thenReturn(true);
        when(patientService.findById("PAT-01")).thenReturn(Optional.of(patient));
        when(aiAssistantService.getOrCreateDefaultAssistant()).thenReturn(assistant);
        when(aiConversationService.create(any())).thenReturn(conversation);
        when(aiProviderManager.generateWithModel(anyString(), anyString(), anyString(), any()))
                .thenReturn("Xin chào!");

        ChatResponseDto res = service.sendMessage(requestDto);
        assertNotNull(res);
        assertEquals("CONV-01", res.conversationId());
    }

    @Test
    void testSendMessageStream_AiDisabled() {
        when(aiMonitoringService.isAiEnabled()).thenReturn(false);
        SseEmitter emitter = service.sendMessageStream(requestDto, 1);
        assertNotNull(emitter);
    }

    @Test
    void testSendMessageStream_Success() throws Exception {
        when(aiMonitoringService.isAiEnabled()).thenReturn(true);
        when(patientService.findById("PAT-01")).thenReturn(Optional.of(patient));
        when(aiConversationService.findById("CONV-01")).thenReturn(Optional.of(conversation));
        when(aiAssistantService.getOrCreateDefaultAssistant()).thenReturn(assistant);

        doAnswer(invocation -> {
            java.util.function.Consumer<String> onChunk = invocation.getArgument(4);
            Runnable onComplete = invocation.getArgument(5);
            onChunk.accept("Hello chunk");
            onComplete.run();
            return null;
        }).when(aiProviderManager).generateStreamWithModel(anyString(), anyString(), anyString(), any(), any(), any(), any());

        SseEmitter emitter = service.sendMessageStream(requestDto, 1);
        assertNotNull(emitter);
        
        // Wait for async thread processing
        Thread.sleep(200);
        verify(aiMessageService, times(2)).create(any());
    }

    @Test
    void testGetConversationHistory() {
        AIMessage msg = new AIMessage();
        msg.setSender("AI");
        msg.setContent("Bác sĩ AI");
        msg.setTime(LocalDateTime.now());

        when(aiMessageService.findByConversationId("CONV-01")).thenReturn(List.of(msg));

        ConversationHistoryDto res = service.getConversationHistory("CONV-01");
        assertNotNull(res);
        assertEquals("CONV-01", res.conversationId());
        assertEquals(1, res.messages().size());
        assertEquals("AI", res.messages().get(0).sender());
    }

    @Test
    void testGetPatientConversations() {
        AIMessage msg = new AIMessage();
        msg.setSender("Patient");
        msg.setContent("Hello");
        msg.setTime(LocalDateTime.now());

        when(aiConversationService.findByPatientId("PAT-01")).thenReturn(List.of(conversation));
        when(aiMessageService.findByConversationId("CONV-01")).thenReturn(List.of(msg));

        List<ConversationHistoryDto> res = service.getPatientConversations("PAT-01");
        assertEquals(1, res.size());
        assertEquals("User", res.get(0).messages().get(0).sender());
    }

    @Test
    void testGetPatientConversationsWithAssistant() {
        AIMessage msg = new AIMessage();
        msg.setSender("AI");
        msg.setContent("Hello");
        msg.setTime(LocalDateTime.now());

        when(aiConversationService.findByPatientIdAndAssistantId("PAT-01", 1)).thenReturn(List.of(conversation));
        when(aiMessageService.findByConversationId("CONV-01")).thenReturn(List.of(msg));

        List<ConversationHistoryDto> res = service.getPatientConversationsWithAssistant("PAT-01", 1);
        assertEquals(1, res.size());
        assertEquals("AI", res.get(0).messages().get(0).sender());
    }

    @Test
    void testDeleteConversation_Success() {
        when(aiConversationService.existsById("CONV-01")).thenReturn(true);
        doNothing().when(aiMessageService).deleteByConversationId("CONV-01");
        doNothing().when(aiConversationService).deleteById("CONV-01");

        assertDoesNotThrow(() -> service.deleteConversation("CONV-01"));
        verify(aiConversationService).deleteById("CONV-01");
    }

    @Test
    void testDeleteConversation_NotFound() {
        when(aiConversationService.existsById("CONV-99")).thenReturn(false);
        assertThrows(EntityNotFoundException.class, () -> service.deleteConversation("CONV-99"));
    }

    @Test
    void testGetAvailableAssistants() {
        when(aiAssistantService.findAll()).thenReturn(List.of(assistant));
        List<AIAssistantDto> res = service.getAvailableAssistants();
        assertEquals(1, res.size());
        assertEquals("Specialist", res.get(0).name());
    }

    @Test
    void testSendMessage_TopicGenerationOnFirstMessage() {
        requestDto = new AiChatRequestDto("Hello this is first message", "CONV-01", "PAT-01");
        when(aiMonitoringService.isAiEnabled()).thenReturn(true);
        when(patientService.findById("PAT-01")).thenReturn(Optional.of(patient));
        when(aiConversationService.findById("CONV-01")).thenReturn(Optional.of(conversation));
        when(aiAssistantService.getOrCreateDefaultAssistant()).thenReturn(assistant);

        when(aiProviderManager.generateWithModel(anyString(), anyString(), anyString(), any()))
                .thenReturn("Xin chào bạn!");
        when(aiMessageService.countByConversationId("CONV-01")).thenReturn(1L);

        ChatResponseDto res = service.sendMessage(requestDto);
        assertNotNull(res);
        verify(aiConversationService).update(eq("CONV-01"), any(AIConversation.class));
    }

    @Test
    void testSendMessage_EmptyAiResponse() {
        when(aiMonitoringService.isAiEnabled()).thenReturn(true);
        when(patientService.findById("PAT-01")).thenReturn(Optional.of(patient));
        when(aiConversationService.findById("CONV-01")).thenReturn(Optional.of(conversation));
        when(aiAssistantService.getOrCreateDefaultAssistant()).thenReturn(assistant);

        when(aiProviderManager.generateWithModel(anyString(), anyString(), anyString(), any()))
                .thenReturn(""); // empty response

        ChatResponseDto res = service.sendMessage(requestDto);
        assertNotNull(res);
        assertTrue(res.message().contains("sự cố"));
    }

    @Test
    void testSendMessageStream_NullAssistantId() throws Exception {
        when(aiMonitoringService.isAiEnabled()).thenReturn(true);
        when(patientService.findById("PAT-01")).thenReturn(Optional.of(patient));
        when(aiConversationService.findById("CONV-01")).thenReturn(Optional.of(conversation));
        when(aiAssistantService.getOrCreateDefaultAssistant()).thenReturn(assistant);

        doAnswer(invocation -> {
            Runnable onComplete = invocation.getArgument(5);
            onComplete.run();
            return null;
        }).when(aiProviderManager).generateStreamWithModel(anyString(), anyString(), anyString(), any(), any(), any(), any());

        SseEmitter emitter = service.sendMessageStream(requestDto, null);
        assertNotNull(emitter);
        Thread.sleep(200);
        verify(aiAssistantService).getOrCreateDefaultAssistant();
    }

    @Test
    void testSendMessageStream_EmptyModelName() throws Exception {
        AIAssistant emptyModelAssistant = new AIAssistant();
        emptyModelAssistant.setAiAssistantId(2);
        emptyModelAssistant.setModelName(null);

        when(aiMonitoringService.isAiEnabled()).thenReturn(true);
        when(patientService.findById("PAT-01")).thenReturn(Optional.of(patient));
        when(aiConversationService.findById("CONV-01")).thenReturn(Optional.of(conversation));
        when(aiAssistantService.getOrCreateDefaultAssistant()).thenReturn(emptyModelAssistant);

        doAnswer(invocation -> {
            Runnable onComplete = invocation.getArgument(5);
            onComplete.run();
            return null;
        }).when(aiProviderManager).generateStreamWithModel(anyString(), anyString(), anyString(), any(), any(), any(), any());

        SseEmitter emitter = service.sendMessageStream(requestDto, null);
        assertNotNull(emitter);
        Thread.sleep(200);
        assertEquals("diabetes", emptyModelAssistant.getModelName());
    }

    @Test
    void testPrivateHelpers_ViaReflection() {
        // isGeneralMedicalQuestion
        Boolean genNull = ReflectionTestUtils.invokeMethod(service, "isGeneralMedicalQuestion", (String) null);
        assertFalse(genNull);

        // isSocialGreeting
        Boolean greetNull = ReflectionTestUtils.invokeMethod(service, "isSocialGreeting", (String) null);
        assertFalse(greetNull);

        Boolean greetHello = ReflectionTestUtils.invokeMethod(service, "isSocialGreeting", "hello");
        assertTrue(greetHello);

        Boolean greetHi = ReflectionTestUtils.invokeMethod(service, "isSocialGreeting", "hi");
        assertTrue(greetHi);

        Boolean greetInvalid = ReflectionTestUtils.invokeMethod(service, "isSocialGreeting", "invalid");
        assertFalse(greetInvalid);

        // normalizeChatSlang
        String normNull = ReflectionTestUtils.invokeMethod(service, "normalizeChatSlang", (String) null);
        assertNull(normNull);

        String normEmpty = ReflectionTestUtils.invokeMethod(service, "normalizeChatSlang", "   ");
        assertEquals("   ", normEmpty);

        String normSlang = ReflectionTestUtils.invokeMethod(service, "normalizeChatSlang", "là j");
        assertEquals("là gì", normSlang);

        // cleanAndFormatAiResponse
        String cleanNull = ReflectionTestUtils.invokeMethod(service, "cleanAndFormatAiResponse", (String) null);
        assertNull(cleanNull);

        String cleanEmpty = ReflectionTestUtils.invokeMethod(service, "cleanAndFormatAiResponse", "   ");
        assertEquals("   ", cleanEmpty);

        String cleanFilter = ReflectionTestUtils.invokeMethod(service, "cleanAndFormatAiResponse", "[SYSTEM PROMPT] :");
        assertTrue(cleanFilter.contains("Xin lỗi bạn"));

        // callOllamaGenerate
        when(aiProviderManager.generateWithModel(eq("diabetes"), eq("prompt"), eq("sys"), any()))
                .thenReturn("OllamaResponse");
        String ollamaRes = ReflectionTestUtils.invokeMethod(service, "callOllamaGenerate", "diabetes", "prompt", "sys", null);
        assertEquals("OllamaResponse", ollamaRes);

        // fetchDataFromRepository
        when(aiTool.getGeneralRecord("PAT-01")).thenReturn("GenRec");
        when(aiTool.getClinicalExamination("PAT-01")).thenReturn("ClinExam");
        when(aiTool.getTreatmentPlan("PAT-01")).thenReturn("TreatPlan");
        when(aiTool.getLabResults("PAT-01")).thenReturn("LabRes");
        when(aiTool.getPrescriptions("PAT-01")).thenReturn("Presc");

        assertEquals("GenRec", ReflectionTestUtils.invokeMethod(service, "fetchDataFromRepository", "get_general_record", "PAT-01"));
        assertEquals("ClinExam", ReflectionTestUtils.invokeMethod(service, "fetchDataFromRepository", "get_clinical_examination", "PAT-01"));
        assertEquals("TreatPlan", ReflectionTestUtils.invokeMethod(service, "fetchDataFromRepository", "get_treatment_plan", "PAT-01"));
        assertEquals("LabRes", ReflectionTestUtils.invokeMethod(service, "fetchDataFromRepository", "get_lab_results", "PAT-01"));
        assertEquals("Presc", ReflectionTestUtils.invokeMethod(service, "fetchDataFromRepository", "get_prescriptions", "PAT-01"));
        assertNull(ReflectionTestUtils.invokeMethod(service, "fetchDataFromRepository", "invalid_action", "PAT-01"));
    }

    @Test
    void testSendMessageStream_WithRAGToolAction() throws Exception {
        requestDto = new AiChatRequestDto("cho tôi xem đơn thuốc của tôi", "CONV-01", "PAT-01");
        when(aiMonitoringService.isAiEnabled()).thenReturn(true);
        when(patientService.findById("PAT-01")).thenReturn(Optional.of(patient));
        when(aiConversationService.findById("CONV-01")).thenReturn(Optional.of(conversation));
        when(aiAssistantService.getOrCreateDefaultAssistant()).thenReturn(assistant);

        when(aiTool.getPrescriptions("PAT-01")).thenReturn("DANH SÁCH THUỐC TRONG ĐƠN THUỐC CỦA BỆNH NHÂN:\n1. Metformin");

        doAnswer(invocation -> {
            Runnable onComplete = invocation.getArgument(5);
            onComplete.run();
            return null;
        }).when(aiProviderManager).generateStreamWithModel(anyString(), anyString(), anyString(), any(), any(), any(), any());

        SseEmitter emitter = service.sendMessageStream(requestDto, 1);
        assertNotNull(emitter);
        Thread.sleep(200);
        verify(aiTool).getPrescriptions("PAT-01");
    }

    @Test
    void testSendMessageStream_NoDataDirectResponse() throws Exception {
        requestDto = new AiChatRequestDto("cho tôi xem đơn thuốc của tôi", "CONV-01", "PAT-01");
        when(aiMonitoringService.isAiEnabled()).thenReturn(true);
        when(patientService.findById("PAT-01")).thenReturn(Optional.of(patient));
        when(aiConversationService.findById("CONV-01")).thenReturn(Optional.of(conversation));
        when(aiAssistantService.getOrCreateDefaultAssistant()).thenReturn(assistant);

        // returns no data
        when(aiTool.getPrescriptions("PAT-01")).thenReturn("DANH SÁCH THUỐC TRONG ĐƠN THUỐC CỦA BỆNH NHÂN:\n(Không có dữ liệu)");

        SseEmitter emitter = service.sendMessageStream(requestDto, 1);
        assertNotNull(emitter);
        Thread.sleep(200);
        // Should NOT call generateStreamWithModel since it returns direct response
        verify(aiProviderManager, never()).generateStreamWithModel(anyString(), anyString(), anyString(), any(), any(), any(), any());
    }

    @Test
    void testSendMessageStream_WithKeywordToolFallback_AllCases() throws Exception {
        when(aiMonitoringService.isAiEnabled()).thenReturn(true);
        when(patientService.findById("PAT-01")).thenReturn(Optional.of(patient));
        when(aiConversationService.findById("CONV-01")).thenReturn(Optional.of(conversation));
        when(aiAssistantService.getOrCreateDefaultAssistant()).thenReturn(assistant);

        doAnswer(invocation -> {
            Runnable onComplete = invocation.getArgument(5);
            onComplete.run();
            return null;
        }).when(aiProviderManager).generateStreamWithModel(anyString(), anyString(), anyString(), any(), any(), any(), any());

        // Case 1: Prescription
        requestDto = new AiChatRequestDto("Uống thuốc đó thế nào", "CONV-01", "PAT-01");
        when(aiMessageService.getFormattedConversationHistory("CONV-01", 3))
                .thenReturn("Bệnh nhân: đơn thuốc Metformin");
        when(aiTool.getPrescriptions("PAT-01")).thenReturn("DANH SÁCH THUỐC TRONG ĐƠN THUỐC CỦA BỆNH NHÂN:\n1. Metformin");

        SseEmitter emitter1 = service.sendMessageStream(requestDto, 1);
        assertNotNull(emitter1);
        Thread.sleep(200);
        verify(aiTool, times(1)).getPrescriptions("PAT-01");

        // Case 2: Lab Results
        requestDto = new AiChatRequestDto("Chỉ số đó thế nào", "CONV-01", "PAT-01");
        when(aiMessageService.getFormattedConversationHistory("CONV-01", 3))
                .thenReturn("Bệnh nhân: kết quả xét nghiệm Glucose");
        when(aiTool.getLabResults("PAT-01")).thenReturn("Glucose: 7.5");

        SseEmitter emitter2 = service.sendMessageStream(requestDto, 1);
        assertNotNull(emitter2);
        Thread.sleep(200);
        verify(aiTool, times(1)).getLabResults("PAT-01");

        // Case 3: Treatment Plan
        requestDto = new AiChatRequestDto("Phác đồ đó thế nào", "CONV-01", "PAT-01");
        when(aiMessageService.getFormattedConversationHistory("CONV-01", 3))
                .thenReturn("Bệnh nhân: kế hoạch điều trị");
        when(aiTool.getTreatmentPlan("PAT-01")).thenReturn("Diet plan: less sugar");

        SseEmitter emitter3 = service.sendMessageStream(requestDto, 1);
        assertNotNull(emitter3);
        Thread.sleep(200);
        verify(aiTool, times(1)).getTreatmentPlan("PAT-01");

        // Case 4: Clinical Exam
        requestDto = new AiChatRequestDto("Lịch khám đó thế nào", "CONV-01", "PAT-01");
        when(aiMessageService.getFormattedConversationHistory("CONV-01", 3))
                .thenReturn("Bệnh nhân: bác sĩ khám lâm sàng");
        when(aiTool.getClinicalExamination("PAT-01")).thenReturn("Exam date: 22/07/2026");

        SseEmitter emitter4 = service.sendMessageStream(requestDto, 1);
        assertNotNull(emitter4);
        Thread.sleep(200);
        verify(aiTool, times(1)).getClinicalExamination("PAT-01");
    }

    @Test
    void testSendMessage_ConversationBelongsToDifferentPatient() {
        Patient differentPatient = new Patient();
        differentPatient.setUserId("PAT-99");

        AIConversation otherConv = new AIConversation();
        otherConv.setAiConversationId("CONV-01");
        otherConv.setPatient(differentPatient);

        requestDto = new AiChatRequestDto("hello", "CONV-01", "PAT-01");

        when(aiMonitoringService.isAiEnabled()).thenReturn(true);
        when(patientService.findById("PAT-01")).thenReturn(Optional.of(patient));
        when(aiConversationService.findById("CONV-01")).thenReturn(Optional.of(otherConv));
        when(aiAssistantService.getOrCreateDefaultAssistant()).thenReturn(assistant);

        ChatResponseDto res = service.sendMessage(requestDto);
        assertNotNull(res);
        assertFalse(res.success());
        assertTrue(res.error().contains("belong"));
    }

    @Test
    void testGetAvailableAssistants_EmptyListFallback() {
        when(aiAssistantService.findAll()).thenReturn(Collections.emptyList());
        when(aiAssistantService.getOrCreateDefaultAssistant()).thenReturn(assistant);

        List<AIAssistantDto> res = service.getAvailableAssistants();
        assertEquals(1, res.size());
        assertEquals("Specialist", res.get(0).name());
    }

    @Test
    void testPrivateMethods_Reflection() {
        // extractToolCallJson
        assertNull(ReflectionTestUtils.invokeMethod(service, "extractToolCallJson", (String) null));
        assertNull(ReflectionTestUtils.invokeMethod(service, "extractToolCallJson", "   "));
        assertNull(ReflectionTestUtils.invokeMethod(service, "extractToolCallJson", "{ \"action\": \"invalid\" }"));
        assertNull(ReflectionTestUtils.invokeMethod(service, "extractToolCallJson", "invalid json"));
        
        String validJson = "{ \"action\": \"get_prescriptions\", \"patient_id\": \"PAT-01\" }";
        assertEquals(validJson, ReflectionTestUtils.invokeMethod(service, "extractToolCallJson", validJson));
        assertEquals(validJson, ReflectionTestUtils.invokeMethod(service, "extractToolCallJson", "prefix " + validJson + " suffix"));

        // extractCategoryTitle
        assertEquals("Hồ sơ sức khỏe", ReflectionTestUtils.invokeMethod(service, "extractCategoryTitle", (String) null));
        assertEquals("Kết quả xét nghiệm", ReflectionTestUtils.invokeMethod(service, "extractCategoryTitle", "kết quả xét nghiệm"));
        assertEquals("Đơn thuốc điều trị", ReflectionTestUtils.invokeMethod(service, "extractCategoryTitle", "đơn thuốc"));
        assertEquals("Đơn thuốc điều trị", ReflectionTestUtils.invokeMethod(service, "extractCategoryTitle", "thuốc trong đơn"));
        assertEquals("Lịch sử thăm khám lâm sàng", ReflectionTestUtils.invokeMethod(service, "extractCategoryTitle", "khám lâm sàng"));
        assertEquals("Phác đồ & Kế hoạch điều trị", ReflectionTestUtils.invokeMethod(service, "extractCategoryTitle", "kế hoạch điều trị"));
        assertEquals("Phác đồ & Kế hoạch điều trị", ReflectionTestUtils.invokeMethod(service, "extractCategoryTitle", "phác đồ"));
        assertEquals("Hồ sơ sức khỏe cá nhân", ReflectionTestUtils.invokeMethod(service, "extractCategoryTitle", "hồ sơ bệnh án chung"));
        assertEquals("Hồ sơ y tế cá nhân", ReflectionTestUtils.invokeMethod(service, "extractCategoryTitle", "random content"));

        // formatDirectRagResponse
        String resPresc = ReflectionTestUtils.invokeMethod(service, "formatDirectRagResponse", "đơn thuốc data", "question");
        assertTrue(resPresc.contains("Đơn thuốc điều trị"));

        String resPlan = ReflectionTestUtils.invokeMethod(service, "formatDirectRagResponse", "phác đồ data", "question");
        assertTrue(resPlan.contains("Phác đồ"));

        String resLab = ReflectionTestUtils.invokeMethod(service, "formatDirectRagResponse", "kết quả xét nghiệm data", "question");
        assertTrue(resLab.contains("Kết quả xét nghiệm"));

        String resExam = ReflectionTestUtils.invokeMethod(service, "formatDirectRagResponse", "khám lâm sàng data", "question");
        assertTrue(resExam.contains("Lịch sử thăm khám"));

        String resOther = ReflectionTestUtils.invokeMethod(service, "formatDirectRagResponse", "other data", "question");
        assertTrue(resOther.contains("Hồ sơ sức khỏe cá nhân"));

        // isSocialGreeting
        String[] greetings = {"hello", "hi", "chào", "xin chào", "chào bạn", "chào bác sĩ", "alo", "hey", "chào ai", "hi bác sĩ", "hello bác sĩ"};
        for (String g : greetings) {
            assertTrue((Boolean) ReflectionTestUtils.invokeMethod(service, "isSocialGreeting", g));
        }
        assertFalse((Boolean) ReflectionTestUtils.invokeMethod(service, "isSocialGreeting", (String) null));
        assertFalse((Boolean) ReflectionTestUtils.invokeMethod(service, "isSocialGreeting", "not a greeting"));

        // isGeneralMedicalQuestion
        assertFalse((Boolean) ReflectionTestUtils.invokeMethod(service, "isGeneralMedicalQuestion", (String) null));
        
        String[] generalKeywordsTrue = {
            "tác dụng phụ", "tac dung phu", "tác dụng gì", "tac dung gi", "tác dụng như thế nào", "tac dung nhu the nao",
            "công dụng", "cong dung", "cơ chế", "co che", "tại sao", "tai sao", "như thế nào", "nhu the nao", "ra sao",
            "có sao không", "co sao khong", "có tốt không", "co tot khong", "nguy hiểm không", "nguy hiem khong",
            "nghĩa là gì", "nghia la gi", "ý nghĩa gì", "y nghia gi", "ảnh hưởng", "anh huong", "kiêng gì", "kieng gi",
            "nên làm gì", "nen lam gi", "cách dùng", "cach dung", "sử dụng thế nào", "su dung the nao", "uống thế nào",
            "uong the nao", "ăn thế nào", "an the nao", "tập thế nào", "tap the nao", "nguyên nhân", "nguyen nhan",
            "triệu chứng", "trieu chung", "hướng dẫn", "huong dan", "lời khuyên", "loi khuyen",
            "các loại thuốc", "cac loai thuoc", "nhóm thuốc", "nhom thuoc", "thuốc điều trị", "thuoc dieu tri",
            "thuốc chữa", "thuoc chua", "là thuốc", "la thuoc", "thuốc gì", "thuoc gi", "thuốc j", "thuoc j",
            "metformin", "insulin", "tập thể dục", "tap the duc",
            "lịch tập", "lich tap", "bài tập", "bai tap", "chế độ ăn", "che do an", "nên ăn gì", "nen an gi",
            "kiêng ăn gì", "kieng an gi", "thực đơn", "thuc don", "bao nhiêu là bình thường", "bao nhieu la binh thuong",
            "tiểu đường là gì", "tieu duong la gi", "chữa thế nào", "chua the nao", "điều trị thế nào", "dieu tri the nao",
            "nên uống thuốc gì", "nen uong thuoc gi"
        };
        for (String kw : generalKeywordsTrue) {
            assertTrue((Boolean) ReflectionTestUtils.invokeMethod(service, "isGeneralMedicalQuestion", kw));
        }

        String[] generalKeywordsFalse = {
            "của tôi", "cua toi", "của mình", "cua minh", "cho tôi xem", "cho toi xem", "tôi đang", "toi dang",
            "đơn thuốc của", "don thuoc cua", "phác đồ của", "phac do cua", "thuốc đó", "thuoc do", "thuốc này", "thuoc nay",
            "chỉ số đó", "chi so do", "phác đồ đó", "phac do do"
        };
        for (String kw : generalKeywordsFalse) {
            assertFalse((Boolean) ReflectionTestUtils.invokeMethod(service, "isGeneralMedicalQuestion", kw));
        }

        // checkKeywordToolFallback
        assertNotNull(ReflectionTestUtils.invokeMethod(service, "checkKeywordToolFallback", (String) null, "PAT-01"));
        assertNotNull(ReflectionTestUtils.invokeMethod(service, "checkKeywordToolFallback", "   ", "PAT-01"));
        
        String[] prescKeywords = {"đơn thuốc", "don thuoc", "toa thuốc", "toa thuoc", "thuốc bác sĩ kê", "thuoc bac si ke",
            "thuốc đã kê", "thuoc da ke", "thuốc tôi đang", "thuoc toi dang", "thuốc của tôi", "thuoc cua toi",
            "thuốc đang uống", "thuoc dang uong", "lịch sử dùng thuốc", "lich su dung thuoc", "thuốc của mình",
            "thuốc cho tôi xem", "thuốc tôi uống"};
        for (String kw : prescKeywords) {
            Map<String, String> res = ReflectionTestUtils.invokeMethod(service, "checkKeywordToolFallback", kw, "PAT-01");
            assertEquals("get_prescriptions", res.get("action"));
        }

        String[] examKeywords = {"lịch tái khám", "lich tai kham", "tái khám của tôi", "tai kham cua toi",
            "khi nào tôi tái khám", "khi nao toi tai kham", "ngày tái khám", "ngay tai kham", "lịch hẹn", "lich hen",
            "hẹn khám", "hen kham", "bệnh án của tôi", "benh an cua toi", "lịch sử khám của tôi", "lich su kham cua toi",
            "chẩn đoán của tôi", "chẩn đoán bác sĩ"};
        for (String kw : examKeywords) {
            Map<String, String> res = ReflectionTestUtils.invokeMethod(service, "checkKeywordToolFallback", kw, "PAT-01");
            assertEquals("get_clinical_examination", res.get("action"));
        }

        String[] labKeywords = {"kết quả xét nghiệm", "ket qua xet nghiem", "xét nghiệm của tôi", "xet nghiem cua toi",
            "kết quả của tôi", "ket qua cua toi", "chỉ số của tôi", "chi so cua toi", "hba1c của tôi", "đường huyết của tôi",
            "hba1c của mình", "đường huyết của mình", "hba1c xem", "đường huyết xem"};
        for (String kw : labKeywords) {
            Map<String, String> res = ReflectionTestUtils.invokeMethod(service, "checkKeywordToolFallback", kw, "PAT-01");
            assertEquals("get_lab_results", res.get("action"));
        }

        String[] planKeywords = {"kế hoạch điều trị", "ke hoach dieu tri", "phác đồ điều trị", "phac do dieu tri",
            "phác đồ và kế hoạch", "phac do va ke hoach", "phác đồ của tôi", "phac do cua toi",
            "chế độ ăn của tôi", "che do an cua toi",
            "chế độ tập luyện của tôi", "che do tap luyen cua toi",
            "mục tiêu điều trị của tôi", "muc tieu dieu tri của tôi", "bác sĩ dặn", "bac si dan", "lời dặn", "loi dan"};
        for (String kw : planKeywords) {
            Map<String, String> res = ReflectionTestUtils.invokeMethod(service, "checkKeywordToolFallback", kw, "PAT-01");
            assertEquals("get_treatment_plan", res.get("action"));
        }

        // Test the ones that bypass to NONE due to general medical question check priority
        String[] planKeywordsGeneral = {"thực đơn điều trị", "thuc don dieu tri"};
        for (String kw : planKeywordsGeneral) {
            Map<String, String> res = ReflectionTestUtils.invokeMethod(service, "checkKeywordToolFallback", kw, "PAT-01");
            assertEquals("NONE", res.get("action"));
        }

        String[] recordKeywords = {"hồ sơ của tôi", "ho so cua toi", "hồ sơ y tế", "ho so y te", "thông tin cá nhân",
            "thong tin ca nhan", "thông tin của tôi", "thong tin cua toi", "tôi bị bệnh gì", "toi bi benh gi",
            "bệnh của tôi", "benh cua toi", "tiền sử bệnh", "tien su benh", "nhóm máu của tôi", "bmi của tôi",
            "chiều cao của tôi", "cân nặng của tôi"};
        for (String kw : recordKeywords) {
            Map<String, String> res = ReflectionTestUtils.invokeMethod(service, "checkKeywordToolFallback", kw, "PAT-01");
            assertEquals("get_general_record", res.get("action"));
        }

        // Test combinations for get_lab_results second containsAny check false path
        Map<String, String> resLabFalse = ReflectionTestUtils.invokeMethod(service, "checkKeywordToolFallback", "hba1c", "PAT-01");
        assertEquals("NONE", resLabFalse.get("action"));

        // Test combinations for get_treatment_plan second contains check false path
        Map<String, String> resPlanFalse = ReflectionTestUtils.invokeMethod(service, "checkKeywordToolFallback", "phác đồ", "PAT-01");
        assertEquals("NONE", resPlanFalse.get("action"));

        // Test combinations for get_general_record second contains check false path
        Map<String, String> resRecFalse = ReflectionTestUtils.invokeMethod(service, "checkKeywordToolFallback", "bmi", "PAT-01");
        assertEquals("NONE", resRecFalse.get("action"));

        // cleanAndFormatAiResponse regex cleanup cases
        String input1 = "QUY TẮC BẮT BUỘC:\nSome text\nMẠNH với tuổi thọ:\nOther text\nMã lỗi: 123\nMã bệnh nhân: 456\nCâu hỏi: abc\nAI: Hello";
        String cleanRes = ReflectionTestUtils.invokeMethod(service, "cleanAndFormatAiResponse", input1);
        assertFalse(cleanRes.contains("QUY TẮC BẮT BUỘC"));
        assertFalse(cleanRes.contains("MẠNH với tuổi thọ"));
        assertFalse(cleanRes.contains("Mã lỗi"));
        assertFalse(cleanRes.contains("Mã bệnh nhân"));
        assertFalse(cleanRes.contains("Câu hỏi"));
        assertFalse(cleanRes.contains("AI:"));

        // buildStage2Prompt reflection test combinations
        assertNotNull(ReflectionTestUtils.invokeMethod(service, "buildStage2Prompt", (String) null, (String) null, (String) null));
        
        // isFollowUp combinations
        String[] followUpKeywords = {"nó", "đó", "này", "trên", "vừa rồi", "như vậy", "tiếp theo"};
        for (String kw : followUpKeywords) {
            String res = ReflectionTestUtils.invokeMethod(service, "buildStage2Prompt", "Hỏi về " + kw, (String) null, "Lịch sử");
            assertTrue(res.contains("[LỊCH SỬ TRÒ CHUYỆN GẦN ĐÂY"));
        }
        
        // isFollowUp is true, but sqlData is present
        String resWithSql = ReflectionTestUtils.invokeMethod(service, "buildStage2Prompt", "Hỏi về nó", "Dữ liệu SQL", "Lịch sử");
        assertFalse(resWithSql.contains("[LỊCH SỬ TRÒ CHUYỆN GẦN ĐÂY"));
        assertTrue(resWithSql.contains("[DỮ LIỆU HỒ SƠ CÁ NHÂN"));
        
        // isFollowUp is true, sqlData is empty, formattedHistory is empty
        String resNoHistory = ReflectionTestUtils.invokeMethod(service, "buildStage2Prompt", "Hỏi về nó", (String) null, "  ");
        assertFalse(resNoHistory.contains("[LỊCH SỬ TRÒ CHUYỆN GẦN ĐÂY"));
        
        // sqlData is empty string
        String resSqlEmpty = ReflectionTestUtils.invokeMethod(service, "buildStage2Prompt", "câu hỏi", "   ", (String) null);
        assertFalse(resSqlEmpty.contains("[DỮ LIỆU HỒ SƠ CÁ NHÂN"));

        // containsAny null check
        assertFalse((Boolean) ReflectionTestUtils.invokeMethod(service, "containsAny", (String) null, new String[]{"kw"}));
        
        // hasText helper checks
        assertTrue((Boolean) ReflectionTestUtils.invokeMethod(service, "hasText", "valid"));
        assertFalse((Boolean) ReflectionTestUtils.invokeMethod(service, "hasText", (String) null));
        assertFalse((Boolean) ReflectionTestUtils.invokeMethod(service, "hasText", "   "));

        // callOllamaGenerate
        when(aiProviderManager.generateWithModel(anyString(), anyString(), anyString(), any())).thenReturn("Ollama response");
        String resOllama = ReflectionTestUtils.invokeMethod(service, "callOllamaGenerate", "model", "prompt", "system", (Object) null);
        assertEquals("Ollama response", resOllama);

        com.quan.diabetes.dto.AIChat.OllamaGenerateRequest.Options ollamaOptions = new com.quan.diabetes.dto.AIChat.OllamaGenerateRequest.Options(0.2, 0.8, 2048, 1.1, 64, 2048);
        String resOllamaWithOptions = ReflectionTestUtils.invokeMethod(service, "callOllamaGenerate", "model", "prompt", "system", ollamaOptions);
        assertEquals("Ollama response", resOllamaWithOptions);

        // extractToolCallJson invalid JSON catch block coverage
        String invalidJsonResult = ReflectionTestUtils.invokeMethod(service, "extractToolCallJson", "{\"action\":\"get_general_record\", invalid}");
        assertNull(invalidJsonResult);
    }

    @Test
    void testSendMessageStream_ErrorCallback() throws Exception {
        when(aiMonitoringService.isAiEnabled()).thenReturn(true);
        when(patientService.findById("PAT-01")).thenReturn(Optional.of(patient));
        when(aiConversationService.findById("CONV-01")).thenReturn(Optional.of(conversation));
        when(aiAssistantService.getOrCreateDefaultAssistant()).thenReturn(assistant);

        doAnswer(invocation -> {
            java.util.function.Consumer<Throwable> onError = invocation.getArgument(6);
            onError.accept(new RuntimeException("Ollama stream connection failed"));
            return null;
        }).when(aiProviderManager).generateStreamWithModel(anyString(), anyString(), anyString(), any(), any(), any(), any());

        SseEmitter emitter = service.sendMessageStream(requestDto, 1);
        assertNotNull(emitter);
        Thread.sleep(200);
    }

    @Test
    void testSendMessageStream_AsyncException() throws Exception {
        when(aiMonitoringService.isAiEnabled()).thenReturn(true);
        when(patientService.findById("PAT-01")).thenThrow(new RuntimeException("DB offline"));

        SseEmitter emitter = service.sendMessageStream(requestDto, 1);
        assertNotNull(emitter);
        Thread.sleep(200);
    }

    @Test
    void testSendMessageWithAssistant_Success() {
        when(aiMonitoringService.isAiEnabled()).thenReturn(true);
        when(patientService.findById("PAT-01")).thenReturn(Optional.of(patient));
        when(aiConversationService.findById("CONV-01")).thenReturn(Optional.of(conversation));
        when(aiAssistantService.findById(1)).thenReturn(Optional.of(assistant));
        
        when(aiProviderManager.generateWithModel(eq("diabetes"), anyString(), anyString(), any()))
                .thenReturn("Hello patient");

        ChatResponseDto res = service.sendMessageWithAssistant(requestDto, 1);
        assertNotNull(res);
        assertTrue(res.success());
        assertEquals("Hello patient", res.message());
    }

    @Test
    void testSendMessageWithAssistant_AssistantNotFound() {
        when(aiMonitoringService.isAiEnabled()).thenReturn(true);
        when(patientService.findById("PAT-01")).thenReturn(Optional.of(patient));
        when(aiConversationService.findById("CONV-01")).thenReturn(Optional.of(conversation));
        when(aiAssistantService.findById(99)).thenReturn(Optional.empty());
        when(aiAssistantService.getOrCreateDefaultAssistant()).thenReturn(assistant);

        when(aiProviderManager.generateWithModel(eq("diabetes"), anyString(), anyString(), any()))
                .thenReturn("Hello default assistant");

        ChatResponseDto res = service.sendMessageWithAssistant(requestDto, 99);
        assertNotNull(res);
        assertTrue(res.success());
        assertEquals("Hello default assistant", res.message());
        verify(aiAssistantService).getOrCreateDefaultAssistant();
    }

    @Test
    void testSendMessage_ConversationNotFound() {
        requestDto = new AiChatRequestDto("hello", "CONV-NOT-FOUND", "PAT-01");

        when(aiMonitoringService.isAiEnabled()).thenReturn(true);
        when(patientService.findById("PAT-01")).thenReturn(Optional.of(patient));
        when(aiConversationService.findById("CONV-NOT-FOUND")).thenReturn(Optional.empty());
        when(aiAssistantService.getOrCreateDefaultAssistant()).thenReturn(assistant);

        ChatResponseDto res = service.sendMessage(requestDto);
        assertNotNull(res);
        assertFalse(res.success());
        assertTrue(res.error().contains("Conversation not found"));
    }

    @Test
    void testSendMessageStream_PatientNotFound() throws Exception {
        when(aiMonitoringService.isAiEnabled()).thenReturn(true);
        when(patientService.findById("PAT-01")).thenReturn(Optional.empty());

        SseEmitter emitter = service.sendMessageStream(requestDto, 1);
        assertNotNull(emitter);
        Thread.sleep(200);
    }

    @Test
    void testSendMessage_JacksonExceptionInChặng1() {
        requestDto = new AiChatRequestDto("cho tôi xem đơn thuốc của tôi", "CONV-01", "PAT-01");
        when(aiMonitoringService.isAiEnabled()).thenReturn(true);
        when(patientService.findById("PAT-01")).thenReturn(Optional.of(patient));
        when(aiConversationService.findById("CONV-01")).thenReturn(Optional.of(conversation));
        when(aiAssistantService.getOrCreateDefaultAssistant()).thenReturn(assistant);

        try (var mocked = mockConstruction(com.fasterxml.jackson.databind.ObjectMapper.class,
                (mock, context) -> {
                    when(mock.writeValueAsString(any())).thenReturn("{\"action\":\"get_prescriptions\",\"patient_id\":\"PAT-01\"}");
                    when(mock.readValue(anyString(), eq(Map.class))).thenThrow(new RuntimeException("Jackson read error"));
                })) {
            ChatResponseDto res = service.sendMessage(requestDto);
            assertNotNull(res);
            assertTrue(res.success());
            assertTrue(res.message().contains("chưa có bản ghi"));
        }
    }

    @Test
    void testSendMessageStream_JacksonException() throws Exception {
        requestDto = new AiChatRequestDto("cho tôi xem đơn thuốc của tôi", "CONV-01", "PAT-01");
        when(aiMonitoringService.isAiEnabled()).thenReturn(true);
        when(patientService.findById("PAT-01")).thenReturn(Optional.of(patient));
        when(aiConversationService.findById("CONV-01")).thenReturn(Optional.of(conversation));
        when(aiAssistantService.getOrCreateDefaultAssistant()).thenReturn(assistant);

        try (org.mockito.MockedStatic<java.util.concurrent.CompletableFuture> mockedCF = org.mockito.Mockito.mockStatic(java.util.concurrent.CompletableFuture.class);
             var mockedEmitter = mockConstruction(SseEmitter.class);
             var mocked = mockConstruction(com.fasterxml.jackson.databind.ObjectMapper.class,
                (mock, context) -> {
                    when(mock.writeValueAsString(any())).thenReturn("{\"action\":\"get_prescriptions\",\"patient_id\":\"PAT-01\"}");
                    when(mock.readValue(anyString(), eq(Map.class))).thenThrow(new RuntimeException("Jackson stream read error"));
                })) {

            mockedCF.when(() -> java.util.concurrent.CompletableFuture.runAsync(any(Runnable.class)))
                    .thenAnswer(invocation -> {
                        Runnable runnable = invocation.getArgument(0);
                        runnable.run();
                        return java.util.concurrent.CompletableFuture.completedFuture(null);
                    });

            SseEmitter emitter = service.sendMessageStream(requestDto, 1);
            assertNotNull(emitter);
            // Since it runs synchronously on the same thread, the catch block is executed immediately,
            // and we can verify that create was called on our mocks.
            verify(aiMessageService, atLeastOnce()).create(any());
        }
    }

    @Test
    void testSendMessageStream_AiDisabled_EmitterException() throws Exception {
        when(aiMonitoringService.isAiEnabled()).thenReturn(false);
        try (var mocked = mockConstruction(SseEmitter.class,
                (mock, context) -> {
                    doThrow(new RuntimeException("Emitter write failed")).when(mock).send(any(SseEmitter.SseEventBuilder.class));
                })) {
            SseEmitter emitter = service.sendMessageStream(requestDto, 1);
            assertNotNull(emitter);
        }
    }

    @Test
    void testSendMessageStream_AsyncException_EmitterException() throws Exception {
        when(aiMonitoringService.isAiEnabled()).thenReturn(true);
        when(patientService.findById("PAT-01")).thenThrow(new RuntimeException("DB offline"));
        try (var mocked = mockConstruction(SseEmitter.class,
                (mock, context) -> {
                    doThrow(new RuntimeException("Emitter write failed")).when(mock).send(any(SseEmitter.SseEventBuilder.class));
                })) {
            SseEmitter emitter = service.sendMessageStream(requestDto, 1);
            assertNotNull(emitter);
            Thread.sleep(200);
        }
    }

    @Test
    void testSendMessageStream_NoDataDirectResponse_EmitterException() throws Exception {
        requestDto = new AiChatRequestDto("cho tôi xem đơn thuốc của tôi", "CONV-01", "PAT-01");
        when(aiMonitoringService.isAiEnabled()).thenReturn(true);
        when(patientService.findById("PAT-01")).thenReturn(Optional.of(patient));
        when(aiConversationService.findById("CONV-01")).thenReturn(Optional.of(conversation));
        when(aiAssistantService.getOrCreateDefaultAssistant()).thenReturn(assistant);
        when(aiTool.getPrescriptions("PAT-01")).thenReturn("DANH SÁCH THUỐC TRONG ĐƠN THUỐC CỦA BỆNH NHÂN:\n(Không có dữ liệu)");

        try (var mocked = mockConstruction(SseEmitter.class,
                (mock, context) -> {
                    doThrow(new RuntimeException("Emitter write failed")).when(mock).send(any(SseEmitter.SseEventBuilder.class));
                })) {
            SseEmitter emitter = service.sendMessageStream(requestDto, 1);
            assertNotNull(emitter);
            Thread.sleep(200);
        }
    }

    @Test
    void testSendMessageStream_StreamComplete_EmitterException() throws Exception {
        requestDto = new AiChatRequestDto("cho tôi xem đơn thuốc của tôi", "CONV-01", "PAT-01");
        when(aiMonitoringService.isAiEnabled()).thenReturn(true);
        when(patientService.findById("PAT-01")).thenReturn(Optional.of(patient));
        when(aiConversationService.findById("CONV-01")).thenReturn(Optional.of(conversation));
        when(aiAssistantService.getOrCreateDefaultAssistant()).thenReturn(assistant);
        when(aiTool.getPrescriptions("PAT-01")).thenReturn("DANH SÁCH THUỐC TRONG ĐƠN THUỐC CỦA BỆNH NHÂN:\n1. Metformin");

        doAnswer(invocation -> {
            Runnable onComplete = invocation.getArgument(5);
            onComplete.run();
            return null;
        }).when(aiProviderManager).generateStreamWithModel(anyString(), anyString(), anyString(), any(), any(), any(), any());

        try (var mocked = mockConstruction(SseEmitter.class,
                (mock, context) -> {
                    doThrow(new RuntimeException("Emitter complete write failed")).when(mock).send(any(SseEmitter.SseEventBuilder.class));
                })) {
            SseEmitter emitter = service.sendMessageStream(requestDto, 1);
            assertNotNull(emitter);
            Thread.sleep(200);
        }
    }

    @Test
    void testSendMessageStream_StreamError_EmitterException() throws Exception {
        requestDto = new AiChatRequestDto("cho tôi xem đơn thuốc của tôi", "CONV-01", "PAT-01");
        when(aiMonitoringService.isAiEnabled()).thenReturn(true);
        when(patientService.findById("PAT-01")).thenReturn(Optional.of(patient));
        when(aiConversationService.findById("CONV-01")).thenReturn(Optional.of(conversation));
        when(aiAssistantService.getOrCreateDefaultAssistant()).thenReturn(assistant);
        when(aiTool.getPrescriptions("PAT-01")).thenReturn("DANH SÁCH THUỐC TRONG ĐƠN THUỐC CỦA BỆNH NHÂN:\n1. Metformin");

        doAnswer(invocation -> {
            java.util.function.Consumer<Throwable> onError = invocation.getArgument(6);
            onError.accept(new RuntimeException("Stream error"));
            return null;
        }).when(aiProviderManager).generateStreamWithModel(anyString(), anyString(), anyString(), any(), any(), any(), any());

        try (var mocked = mockConstruction(SseEmitter.class,
                (mock, context) -> {
                    doThrow(new RuntimeException("Emitter error write failed")).when(mock).send(any(SseEmitter.SseEventBuilder.class));
                })) {
            SseEmitter emitter = service.sendMessageStream(requestDto, 1);
            assertNotNull(emitter);
            Thread.sleep(200);
        }
    }

    @Test
    void testSendMessage_FollowUp_Prescription() {
        requestDto = new AiChatRequestDto("Tôi nên dùng liều thế nào của nó?", "CONV-01", "PAT-01");
        when(aiMonitoringService.isAiEnabled()).thenReturn(true);
        when(patientService.findById("PAT-01")).thenReturn(Optional.of(patient));
        when(aiConversationService.findById("CONV-01")).thenReturn(Optional.of(conversation));
        when(aiAssistantService.getOrCreateDefaultAssistant()).thenReturn(assistant);
        when(aiMessageService.getFormattedConversationHistory(anyString(), anyInt())).thenReturn("Lịch sử: đơn thuốc");
        when(aiTool.getPrescriptions("PAT-01")).thenReturn("đơn thuốc data");
        when(aiProviderManager.generateWithModel(anyString(), anyString(), anyString(), any())).thenReturn("AI response");

        ChatResponseDto res = service.sendMessage(requestDto);
        assertNotNull(res);
        assertTrue(res.success());
        verify(aiTool).getPrescriptions("PAT-01");
    }

    @Test
    void testSendMessage_FollowUp_LabResults() {
        requestDto = new AiChatRequestDto("Chỉ số đó có cao quá không?", "CONV-01", "PAT-01");
        when(aiMonitoringService.isAiEnabled()).thenReturn(true);
        when(patientService.findById("PAT-01")).thenReturn(Optional.of(patient));
        when(aiConversationService.findById("CONV-01")).thenReturn(Optional.of(conversation));
        when(aiAssistantService.getOrCreateDefaultAssistant()).thenReturn(assistant);
        when(aiMessageService.getFormattedConversationHistory(anyString(), anyInt())).thenReturn("Lịch sử: xét nghiệm");
        when(aiTool.getLabResults("PAT-01")).thenReturn("kết quả xét nghiệm data");
        when(aiProviderManager.generateWithModel(anyString(), anyString(), anyString(), any())).thenReturn("AI response");

        ChatResponseDto res = service.sendMessage(requestDto);
        assertNotNull(res);
        assertTrue(res.success());
        verify(aiTool).getLabResults("PAT-01");
    }

    @Test
    void testSendMessage_FollowUp_TreatmentPlan() {
        requestDto = new AiChatRequestDto("Phác đồ đó có cần kiêng khem gì thêm?", "CONV-01", "PAT-01");
        when(aiMonitoringService.isAiEnabled()).thenReturn(true);
        when(patientService.findById("PAT-01")).thenReturn(Optional.of(patient));
        when(aiConversationService.findById("CONV-01")).thenReturn(Optional.of(conversation));
        when(aiAssistantService.getOrCreateDefaultAssistant()).thenReturn(assistant);
        when(aiMessageService.getFormattedConversationHistory(anyString(), anyInt())).thenReturn("Lịch sử: phác đồ");
        when(aiTool.getTreatmentPlan("PAT-01")).thenReturn("phác đồ data");
        when(aiProviderManager.generateWithModel(anyString(), anyString(), anyString(), any())).thenReturn("AI response");

        ChatResponseDto res = service.sendMessage(requestDto);
        assertNotNull(res);
        assertTrue(res.success());
        verify(aiTool).getTreatmentPlan("PAT-01");
    }

    @Test
    void testSendMessage_FollowUp_ClinicalExam() {
        requestDto = new AiChatRequestDto("Lịch hẹn khám đó với bác sĩ nào?", "CONV-01", "PAT-01");
        when(aiMonitoringService.isAiEnabled()).thenReturn(true);
        when(patientService.findById("PAT-01")).thenReturn(Optional.of(patient));
        when(aiConversationService.findById("CONV-01")).thenReturn(Optional.of(conversation));
        when(aiAssistantService.getOrCreateDefaultAssistant()).thenReturn(assistant);
        when(aiMessageService.getFormattedConversationHistory(anyString(), anyInt())).thenReturn("Lịch sử: khám");
        when(aiTool.getClinicalExamination("PAT-01")).thenReturn("khám lâm sàng data");
        when(aiProviderManager.generateWithModel(anyString(), anyString(), anyString(), any())).thenReturn("AI response");

        ChatResponseDto res = service.sendMessage(requestDto);
        assertNotNull(res);
        assertTrue(res.success());
        verify(aiTool).getClinicalExamination("PAT-01");
    }

    @Test
    void testSendMessageStream_FollowUp_Prescription() throws Exception {
        requestDto = new AiChatRequestDto("Tôi nên dùng liều thế nào của nó?", "CONV-01", "PAT-01");
        when(aiMonitoringService.isAiEnabled()).thenReturn(true);
        when(patientService.findById("PAT-01")).thenReturn(Optional.of(patient));
        when(aiConversationService.findById("CONV-01")).thenReturn(Optional.of(conversation));
        when(aiAssistantService.getOrCreateDefaultAssistant()).thenReturn(assistant);
        when(aiMessageService.getFormattedConversationHistory(anyString(), anyInt())).thenReturn("Lịch sử: đơn thuốc");
        when(aiTool.getPrescriptions("PAT-01")).thenReturn("đơn thuốc data");

        SseEmitter emitter = service.sendMessageStream(requestDto, 1);
        assertNotNull(emitter);
        Thread.sleep(200);
        verify(aiTool).getPrescriptions("PAT-01");
    }

    @Test
    void testSendMessageStream_FollowUp_LabResults() throws Exception {
        requestDto = new AiChatRequestDto("Chỉ số đó có cao quá không?", "CONV-01", "PAT-01");
        when(aiMonitoringService.isAiEnabled()).thenReturn(true);
        when(patientService.findById("PAT-01")).thenReturn(Optional.of(patient));
        when(aiConversationService.findById("CONV-01")).thenReturn(Optional.of(conversation));
        when(aiAssistantService.getOrCreateDefaultAssistant()).thenReturn(assistant);
        when(aiMessageService.getFormattedConversationHistory(anyString(), anyInt())).thenReturn("Lịch sử: xét nghiệm");
        when(aiTool.getLabResults("PAT-01")).thenReturn("kết quả xét nghiệm data");

        SseEmitter emitter = service.sendMessageStream(requestDto, 1);
        assertNotNull(emitter);
        Thread.sleep(200);
        verify(aiTool).getLabResults("PAT-01");
    }

    @Test
    void testSendMessageStream_FollowUp_TreatmentPlan() throws Exception {
        requestDto = new AiChatRequestDto("Phác đồ đó có cần kiêng khem gì thêm?", "CONV-01", "PAT-01");
        when(aiMonitoringService.isAiEnabled()).thenReturn(true);
        when(patientService.findById("PAT-01")).thenReturn(Optional.of(patient));
        when(aiConversationService.findById("CONV-01")).thenReturn(Optional.of(conversation));
        when(aiAssistantService.getOrCreateDefaultAssistant()).thenReturn(assistant);
        when(aiMessageService.getFormattedConversationHistory(anyString(), anyInt())).thenReturn("Lịch sử: phác đồ");
        when(aiTool.getTreatmentPlan("PAT-01")).thenReturn("phác đồ data");

        SseEmitter emitter = service.sendMessageStream(requestDto, 1);
        assertNotNull(emitter);
        Thread.sleep(200);
        verify(aiTool).getTreatmentPlan("PAT-01");
    }

    @Test
    void testSendMessageStream_FollowUp_ClinicalExam() throws Exception {
        requestDto = new AiChatRequestDto("Lịch hẹn khám đó với bác sĩ nào?", "CONV-01", "PAT-01");
        when(aiMonitoringService.isAiEnabled()).thenReturn(true);
        when(patientService.findById("PAT-01")).thenReturn(Optional.of(patient));
        when(aiConversationService.findById("CONV-01")).thenReturn(Optional.of(conversation));
        when(aiAssistantService.getOrCreateDefaultAssistant()).thenReturn(assistant);
        when(aiMessageService.getFormattedConversationHistory(anyString(), anyInt())).thenReturn("Lịch sử: khám");
        when(aiTool.getClinicalExamination("PAT-01")).thenReturn("khám lâm sàng data");

        SseEmitter emitter = service.sendMessageStream(requestDto, 1);
        assertNotNull(emitter);
        Thread.sleep(200);
        verify(aiTool).getClinicalExamination("PAT-01");
    }

    @Test
    void testSendMessage_BranchCoverageLoop() {
        when(aiMonitoringService.isAiEnabled()).thenReturn(true);
        when(patientService.findById("PAT-01")).thenReturn(Optional.of(patient));
        when(aiConversationService.findById("CONV-01")).thenReturn(Optional.of(conversation));
        when(aiAssistantService.getOrCreateDefaultAssistant()).thenReturn(assistant);
        when(aiProviderManager.generateWithModel(anyString(), anyString(), anyString(), any())).thenReturn("AI response");

        String[] cat1Q = {"thuốc của tôi", "uống của tôi", "dùng của tôi", "liều của tôi", "nó của tôi", "đó của tôi", "này của tôi", "khác của tôi"};
        String[] cat2Q = {"chỉ số của tôi", "xét nghiệm của tôi", "kết quả của tôi", "cao của tôi", "thấp của tôi", "đó của tôi", "này của tôi", "tại sao của tôi", "khác của tôi"};
        String[] cat3Q = {"phác đồ của tôi", "kế hoạch của tôi", "ăn của tôi", "kiêng của tôi", "tập của tôi", "đó của tôi", "này của tôi", "lời dặn của tôi", "khác của tôi"};
        String[] cat4Q = {"khám của tôi", "bác sĩ của tôi", "tái khám của tôi", "lịch của tôi", "chẩn đoán của tôi", "đó của tôi", "này của tôi", "khác của tôi"};

        // Run with all-inclusive history
        when(aiMessageService.getFormattedConversationHistory(anyString(), anyInt()))
                .thenReturn("đơn tablet thuốc mg hba1c glucose mmol mg/dl phác đồ kế hoạch dinh dưỡng tập luyện mục tiêu khám bác sĩ lượt khám chẩn đoán");

        for (String q : cat1Q) {
            service.sendMessage(new AiChatRequestDto(q, "CONV-01", "PAT-01"));
        }
        for (String q : cat2Q) {
            service.sendMessage(new AiChatRequestDto(q, "CONV-01", "PAT-01"));
        }
        for (String q : cat3Q) {
            service.sendMessage(new AiChatRequestDto(q, "CONV-01", "PAT-01"));
        }
        for (String q : cat4Q) {
            service.sendMessage(new AiChatRequestDto(q, "CONV-01", "PAT-01"));
        }

        // Run with non-matching history
        when(aiMessageService.getFormattedConversationHistory(anyString(), anyInt())).thenReturn("khác");

        for (String q : cat1Q) {
            service.sendMessage(new AiChatRequestDto(q, "CONV-01", "PAT-01"));
        }
        for (String q : cat2Q) {
            service.sendMessage(new AiChatRequestDto(q, "CONV-01", "PAT-01"));
        }
        for (String q : cat3Q) {
            service.sendMessage(new AiChatRequestDto(q, "CONV-01", "PAT-01"));
        }
        for (String q : cat4Q) {
            service.sendMessage(new AiChatRequestDto(q, "CONV-01", "PAT-01"));
        }
    }

    @Test
    void testSendMessageStream_BranchCoverageLoop() throws Exception {
        when(aiMonitoringService.isAiEnabled()).thenReturn(true);
        when(patientService.findById("PAT-01")).thenReturn(Optional.of(patient));
        when(aiConversationService.findById("CONV-01")).thenReturn(Optional.of(conversation));
        when(aiAssistantService.getOrCreateDefaultAssistant()).thenReturn(assistant);

        String[] cat1Q = {"thuốc của tôi", "uống của tôi", "dùng của tôi", "liều của tôi", "nó của tôi", "đó của tôi", "này của tôi", "khác của tôi"};
        String[] cat2Q = {"chỉ số của tôi", "xét nghiệm của tôi", "kết quả của tôi", "cao của tôi", "thấp của tôi", "đó của tôi", "này của tôi", "tại sao của tôi", "khác của tôi"};
        String[] cat3Q = {"phác đồ của tôi", "kế hoạch của tôi", "ăn của tôi", "kiêng của tôi", "tập của tôi", "đó của tôi", "này của tôi", "lời dặn của tôi", "khác của tôi"};
        String[] cat4Q = {"khám của tôi", "bác sĩ của tôi", "tái khám của tôi", "lịch của tôi", "chẩn đoán của tôi", "đó của tôi", "này của tôi", "khác của tôi"};

        // Run with all-inclusive history
        when(aiMessageService.getFormattedConversationHistory(anyString(), anyInt()))
                .thenReturn("đơn tablet thuốc mg hba1c glucose mmol mg/dl phác đồ kế hoạch dinh dưỡng tập luyện mục tiêu khám bác sĩ lượt khám chẩn đoán");

        for (String q : cat1Q) {
            service.sendMessageStream(new AiChatRequestDto(q, "CONV-01", "PAT-01"), 1);
        }
        for (String q : cat2Q) {
            service.sendMessageStream(new AiChatRequestDto(q, "CONV-01", "PAT-01"), 1);
        }
        for (String q : cat3Q) {
            service.sendMessageStream(new AiChatRequestDto(q, "CONV-01", "PAT-01"), 1);
        }
        for (String q : cat4Q) {
            service.sendMessageStream(new AiChatRequestDto(q, "CONV-01", "PAT-01"), 1);
        }

        Thread.sleep(300); // wait for all threads to complete

        // Run with non-matching history
        when(aiMessageService.getFormattedConversationHistory(anyString(), anyInt())).thenReturn("khác");

        for (String q : cat1Q) {
            service.sendMessageStream(new AiChatRequestDto(q, "CONV-01", "PAT-01"), 1);
        }
        for (String q : cat2Q) {
            service.sendMessageStream(new AiChatRequestDto(q, "CONV-01", "PAT-01"), 1);
        }
        for (String q : cat3Q) {
            service.sendMessageStream(new AiChatRequestDto(q, "CONV-01", "PAT-01"), 1);
        }
        for (String q : cat4Q) {
            service.sendMessageStream(new AiChatRequestDto(q, "CONV-01", "PAT-01"), 1);
        }

        Thread.sleep(300); // wait for all threads to complete
    }

    @Test
    void testClassifyAndGetToolJson_JacksonWriteException() {
        requestDto = new AiChatRequestDto("cho tôi xem đơn thuốc của tôi", "CONV-01", "PAT-01");
        when(aiMonitoringService.isAiEnabled()).thenReturn(true);
        when(patientService.findById("PAT-01")).thenReturn(Optional.of(patient));
        when(aiConversationService.findById("CONV-01")).thenReturn(Optional.of(conversation));
        when(aiAssistantService.getOrCreateDefaultAssistant()).thenReturn(assistant);

        try (var mocked = mockConstruction(com.fasterxml.jackson.databind.ObjectMapper.class,
                (mock, context) -> {
                    when(mock.writeValueAsString(any())).thenThrow(new RuntimeException("Jackson write error"));
                })) {
            ChatResponseDto res = service.sendMessage(requestDto);
            assertNotNull(res);
            assertTrue(res.success());
        }
    }

    @Test
    void testSendMessageStream_ChunkEmitterException() throws Exception {
        requestDto = new AiChatRequestDto("cho tôi xem đơn thuốc của tôi", "CONV-01", "PAT-01");
        when(aiMonitoringService.isAiEnabled()).thenReturn(true);
        when(patientService.findById("PAT-01")).thenReturn(Optional.of(patient));
        when(aiConversationService.findById("CONV-01")).thenReturn(Optional.of(conversation));
        when(aiAssistantService.getOrCreateDefaultAssistant()).thenReturn(assistant);
        when(aiTool.getPrescriptions("PAT-01")).thenReturn("DANH SÁCH THUỐC TRONG ĐƠN THUỐC CỦA BỆNH NHÂN:\n1. Metformin");

        doAnswer(invocation -> {
            java.util.function.Consumer<String> onNext = invocation.getArgument(4);
            onNext.accept("some chunk");
            return null;
        }).when(aiProviderManager).generateStreamWithModel(anyString(), anyString(), anyString(), any(), any(), any(), any());

        try (var mocked = mockConstruction(SseEmitter.class,
                (mock, context) -> {
                    doThrow(new RuntimeException("Chunk send failed")).when(mock).send(any(SseEmitter.SseEventBuilder.class));
                })) {
            SseEmitter emitter = service.sendMessageStream(requestDto, 1);
            assertNotNull(emitter);
            Thread.sleep(200);
        }
    }

    @Test
    void testSendMessageStream_AssistantNotFound() throws Exception {
        when(aiMonitoringService.isAiEnabled()).thenReturn(true);
        when(patientService.findById("PAT-01")).thenReturn(Optional.of(patient));
        when(aiConversationService.findById("CONV-01")).thenReturn(Optional.of(conversation));
        when(aiAssistantService.findById(99)).thenReturn(Optional.empty());
        when(aiAssistantService.getOrCreateDefaultAssistant()).thenReturn(assistant);

        SseEmitter emitter = service.sendMessageStream(requestDto, 99);
        assertNotNull(emitter);
        Thread.sleep(200);
        verify(aiAssistantService).getOrCreateDefaultAssistant();
    }

    @Test
    void testSendMessageWithAssistant_EmptyModelName() {
        AIAssistant assistantWithNoModel = new AIAssistant();
        assistantWithNoModel.setAiAssistantId(2);
        assistantWithNoModel.setAiName("No Model Assistant");
        assistantWithNoModel.setModelName("");

        when(aiMonitoringService.isAiEnabled()).thenReturn(true);
        when(patientService.findById("PAT-01")).thenReturn(Optional.of(patient));
        when(aiConversationService.findById("CONV-01")).thenReturn(Optional.of(conversation));
        when(aiAssistantService.findById(2)).thenReturn(Optional.of(assistantWithNoModel));
        when(aiProviderManager.generateWithModel(anyString(), anyString(), anyString(), any())).thenReturn("AI Response");

        ChatResponseDto res = service.sendMessageWithAssistant(requestDto, 2);
        assertNotNull(res);
        assertTrue(res.success());
        assertEquals("diabetes", assistantWithNoModel.getModelName());
        verify(aiAssistantService).update(eq(2), any());
    }

    @Test
    void testSendMessageStream_EmptyModelName_SpecificAssistant() throws Exception {
        AIAssistant assistantWithNoModel = new AIAssistant();
        assistantWithNoModel.setAiAssistantId(2);
        assistantWithNoModel.setAiName("No Model Assistant");
        assistantWithNoModel.setModelName(null);

        when(aiMonitoringService.isAiEnabled()).thenReturn(true);
        when(patientService.findById("PAT-01")).thenReturn(Optional.of(patient));
        when(aiConversationService.findById("CONV-01")).thenReturn(Optional.of(conversation));
        when(aiAssistantService.findById(2)).thenReturn(Optional.of(assistantWithNoModel));

        SseEmitter emitter = service.sendMessageStream(requestDto, 2);
        assertNotNull(emitter);
        Thread.sleep(200);
        assertEquals("diabetes", assistantWithNoModel.getModelName());
        verify(aiAssistantService).update(eq(2), any());
    }
    void testSendMessageWithAssistant_EmptyAiResponse() {
        when(aiMonitoringService.isAiEnabled()).thenReturn(true);
        when(patientService.findById("PAT-01")).thenReturn(Optional.of(patient));
        when(aiConversationService.findById("CONV-01")).thenReturn(Optional.of(conversation));
        when(aiAssistantService.getOrCreateDefaultAssistant()).thenReturn(assistant);
        when(aiProviderManager.generateWithModel(anyString(), anyString(), anyString(), any())).thenReturn("");

        ChatResponseDto res = service.sendMessage(requestDto);
        assertNotNull(res);
        assertTrue(res.success());
        assertTrue(res.message().contains("gặp sự cố"));
    }

    @Test
    void testAIChatServiceImpl_ExtraBranchCoverage() throws Exception {
        AIMessage aiMsg = new AIMessage();
        aiMsg.setSender("AI");
        aiMsg.setContent("AI content");
        aiMsg.setTime(LocalDateTime.now());
        
        AIMessage userMsg = new AIMessage();
        userMsg.setSender("Patient");
        userMsg.setContent("User content");
        userMsg.setTime(LocalDateTime.now());


        when(aiMessageService.findByConversationId("CONV-01")).thenReturn(List.of(aiMsg, userMsg));
        var historyDto = service.getConversationHistory("CONV-01");
        assertNotNull(historyDto);

        var conversations = service.getPatientConversations("PAT-01");
        assertNotNull(conversations);
        
        var conversationsWithAsst = service.getPatientConversationsWithAssistant("PAT-01", 1);
        assertNotNull(conversationsWithAsst);

        String nullAccents = ReflectionTestUtils.invokeMethod(service, "removeVietnameseAccents", (Object) null);
        assertEquals("", nullAccents);

        String[] testQuestions = {
            "thuốc của tôi uống thế nào?",
            "chẩn đoán bác sĩ thế nào?",
            "chỉ số hba1c xem ở đâu?",
            "phác đồ của tôi là gì?",
            "thực đơn điều trị thế nào?",
            "chỉ số nhóm máu cân nặng của tôi thế nào?",
            "nên uống thuốc gì?",
            "nguyên nhân triệu chứng bệnh tiểu đường"
        };
        for (String q : testQuestions) {
            ReflectionTestUtils.invokeMethod(service, "checkKeywordToolFallback", q, "PAT-01");
        }

        String[] medQuestions = {
            "co tac dung gi",
            "nen uong thuoc gi",
            "nguyen nhan",
            "trieu chung"
        };
        for (String q : medQuestions) {
            ReflectionTestUtils.invokeMethod(service, "isGeneralMedicalQuestion", q);
        }

        String promptWithEmptyHistory = ReflectionTestUtils.invokeMethod(service, "buildStage2Prompt", "q", "sqlData", "");
        assertNotNull(promptWithEmptyHistory);

        String promptWithNoSql = ReflectionTestUtils.invokeMethod(service, "buildStage2Prompt", "q", null, "history");
        assertNotNull(promptWithNoSql);
    }

    @Test
    void testAIChatServiceImpl_RAGKeywordCombinations() throws Exception {
        lenient().when(aiMonitoringService.isAiEnabled()).thenReturn(true);
        lenient().when(patientService.findById("PAT-01")).thenReturn(Optional.of(patient));
        lenient().when(aiConversationService.findById("CONV-01")).thenReturn(Optional.of(conversation));
        lenient().when(aiAssistantService.getOrCreateDefaultAssistant()).thenReturn(assistant);
        lenient().when(aiProviderManager.generateWithModel(anyString(), anyString(), anyString(), any())).thenReturn("NONE");

        String[] qKeywords = {
            "thuốc", "uống", "dùng", "liều", "nó", "đó", "này",
            "chỉ số", "xét nghiệm", "kết quả", "cao", "thấp", "tại sao",
            "phác đồ", "kế hoạch", "ăn", "kiêng", "tập", "lời dặn",
            "khám", "bác sĩ", "tái khám", "lịch", "chẩn đoán"
        };
        
        String[] hKeywords = {
            "thuốc", "tablet", "đơn", "mg",
            "xét nghiệm", "hba1c", "glucose", "mmol", "mg/dl",
            "phác đồ", "kế hoạch", "dinh dưỡng", "tập luyện", "mục tiêu",
            "khám", "bác sĩ", "lượt khám", "chẩn đoán"
        };

        AIMessage userMsg = new AIMessage();
        userMsg.setSender("Patient");
        userMsg.setTime(LocalDateTime.now());

        try (org.mockito.MockedStatic<java.util.concurrent.CompletableFuture> mockedCF = org.mockito.Mockito.mockStatic(java.util.concurrent.CompletableFuture.class);
             var mockedEmitter = mockConstruction(SseEmitter.class)) {
             
            mockedCF.when(() -> java.util.concurrent.CompletableFuture.runAsync(any(Runnable.class)))
                    .thenAnswer(invocation -> {
                        Runnable runnable = invocation.getArgument(0);
                        runnable.run();
                        return java.util.concurrent.CompletableFuture.completedFuture(null);
                    });

            for (String q : qKeywords) {
                for (String h : hKeywords) {
                    userMsg.setContent(h);
                    lenient().when(aiMessageService.findByConversationId("CONV-01")).thenReturn(List.of(userMsg));
                    
                    AiChatRequestDto req = new AiChatRequestDto(q, "CONV-01", "PAT-01");
                    try {
                        service.sendMessage(req);
                    } catch (Exception ignored) {}
                    try {
                        service.sendMessageStream(req, 1);
                    } catch (Exception ignored) {}
                }
            }
        }
    }

    @Test
    void testSendMessageWithAssistant_NullModelName() {
        AIAssistant assistantWithNoModel = new AIAssistant();
        assistantWithNoModel.setAiAssistantId(3);
        assistantWithNoModel.setAiName("Null Model Assistant");
        assistantWithNoModel.setModelName(null);

        when(aiMonitoringService.isAiEnabled()).thenReturn(true);
        when(patientService.findById("PAT-01")).thenReturn(Optional.of(patient));
        when(aiConversationService.findById("CONV-01")).thenReturn(Optional.of(conversation));
        when(aiAssistantService.findById(3)).thenReturn(Optional.of(assistantWithNoModel));
        when(aiProviderManager.generateWithModel(anyString(), anyString(), anyString(), any())).thenReturn("AI Response");

        ChatResponseDto res = service.sendMessageWithAssistant(requestDto, 3);
        assertNotNull(res);
        assertTrue(res.success());
        assertEquals("diabetes", assistantWithNoModel.getModelName());
    }

    @Test
    void testSendMessageWithAssistant_NullAndEmptyToolJsonAndKeywordToolFallback() {
        when(aiMonitoringService.isAiEnabled()).thenReturn(true);
        when(patientService.findById("PAT-01")).thenReturn(Optional.of(patient));
        when(aiConversationService.findById("CONV-01")).thenReturn(Optional.of(conversation));
        when(aiAssistantService.getOrCreateDefaultAssistant()).thenReturn(assistant);

        // 1. Tool JSON is null
        when(aiProviderManager.generateWithModel(anyString(), anyString(), anyString(), any())).thenReturn(null);
        try {
            service.sendMessage(requestDto);
        } catch (Exception ignored) {}

        // 2. Keyword tool fallback (keywordTool != null) in sendMessage
        AiChatRequestDto fallbackReq = new AiChatRequestDto("cho tôi xem đơn thuốc", "CONV-01", "PAT-01");
        try {
            service.sendMessage(fallbackReq);
        } catch (Exception ignored) {}
    }

    @Test
    void testSendMessageStream_NullChunkAndSocialGreetings() throws Exception {
        lenient().when(aiMonitoringService.isAiEnabled()).thenReturn(true);
        lenient().when(patientService.findById("PAT-01")).thenReturn(Optional.of(patient));
        lenient().when(aiConversationService.findById("CONV-01")).thenReturn(Optional.of(conversation));
        lenient().when(aiAssistantService.findById(1)).thenReturn(Optional.of(assistant));

        lenient().doAnswer(invocation -> {
            java.util.function.Consumer<String> onNext = invocation.getArgument(4);
            onNext.accept(null); // triggers chunkText == null branch
            return null;
        }).when(aiProviderManager).generateStreamWithModel(anyString(), anyString(), anyString(), any(), any(), any(), any());

        try (org.mockito.MockedStatic<java.util.concurrent.CompletableFuture> mockedCF = org.mockito.Mockito.mockStatic(java.util.concurrent.CompletableFuture.class);
             var mockedEmitter = mockConstruction(SseEmitter.class)) {
             
            mockedCF.when(() -> java.util.concurrent.CompletableFuture.runAsync(any(Runnable.class)))
                    .thenAnswer(invocation -> {
                        Runnable runnable = invocation.getArgument(0);
                        runnable.run();
                        return java.util.concurrent.CompletableFuture.completedFuture(null);
                    });

            // 1. Test social greeting "hello" with message count = 1 to cover messageCount <= 2 branch
            AIMessage msg1 = new AIMessage();
            lenient().when(aiMessageService.findByConversationId("CONV-01")).thenReturn(List.of(msg1));

            AiChatRequestDto req = new AiChatRequestDto("hello", "CONV-01", "PAT-01");
            service.sendMessageStream(req, 1);

            // 1b. Test social greeting "hello" with message count = 3 to cover messageCount > 2 branch
            AIMessage msg2 = new AIMessage();
            AIMessage msg3 = new AIMessage();
            lenient().when(aiMessageService.findByConversationId("CONV-01")).thenReturn(List.of(msg1, msg2, msg3));
            service.sendMessageStream(req, 1);

            // 2. Test request conversationId is null/empty to cover line 936 branch
            lenient().when(aiAssistantService.getOrCreateDefaultAssistant()).thenReturn(assistant);
            AiChatRequestDto nullConvReq = new AiChatRequestDto("hello", null, "PAT-01");
            try {
                service.sendMessage(nullConvReq);
            } catch (Exception ignored) {}

            AiChatRequestDto emptyConvReq = new AiChatRequestDto("hello", "", "PAT-01");
            try {
                service.sendMessage(emptyConvReq);
            } catch (Exception ignored) {}
        }
    }

    @Test
    void testCleanResponseHistory_ThinkingBranch() throws Exception {
        // Exercise cleanAndFormatAiResponse with a rule prefix to trigger changed = !cleaned.equals(prev) being true
        String input = "YÊU CẦU: YÊU CẦU: Hello World";
        String res = ReflectionTestUtils.invokeMethod(service, "cleanAndFormatAiResponse", input);
        assertEquals("Hello World", res);
    }

    @Test
    void testSenderMappingAndHistoryMethods() {
        AIMessage msgAI = new AIMessage();
        msgAI.setSender("AI");
        msgAI.setContent("AI response");
        msgAI.setTime(LocalDateTime.now());

        AIMessage msgUser = new AIMessage();
        msgUser.setSender("User");
        msgUser.setContent("User question");
        msgUser.setTime(LocalDateTime.now());

        when(aiMessageService.findByConversationId("CONV-01")).thenReturn(List.of(msgAI, msgUser));
        when(aiConversationService.findByPatientId("PAT-01")).thenReturn(List.of(conversation));
        when(aiConversationService.findByPatientIdAndAssistantId("PAT-01", 1)).thenReturn(List.of(conversation));

        ConversationHistoryDto history = service.getConversationHistory("CONV-01");
        assertNotNull(history);
        assertEquals(2, history.messages().size());
        assertEquals("AI", history.messages().get(0).sender());
        assertEquals("User", history.messages().get(1).sender());

        List<ConversationHistoryDto> patientConvs = service.getPatientConversations("PAT-01");
        assertEquals(1, patientConvs.size());

        List<ConversationHistoryDto> patientAssistantConvs = service.getPatientConversationsWithAssistant("PAT-01", 1);
        assertEquals(1, patientAssistantConvs.size());
    }

    @Test
    void testSendMessage_EmptyModelNameAndSqlDataExceptions() {
        when(aiMonitoringService.isAiEnabled()).thenReturn(true);
        when(patientService.findById("PAT-01")).thenReturn(Optional.of(patient));
        when(aiConversationService.findById("CONV-01")).thenReturn(Optional.of(conversation));
        
        AIAssistant emptyModelAssistant = new AIAssistant();
        emptyModelAssistant.setAiAssistantId(1);
        emptyModelAssistant.setAiName("Specialist");
        emptyModelAssistant.setModelName(""); // Empty model to cover modelToUse check branch
        emptyModelAssistant.setStatus("Active");
        when(aiAssistantService.getOrCreateDefaultAssistant()).thenReturn(emptyModelAssistant);

        // Test sqlData with special error patterns
        lenient().when(aiProviderManager.generateWithModel(anyString(), anyString(), anyString(), any())).thenReturn("Direct answer");

        String[] errorResponses = {"(Không có dữ liệu)", "Không thể truy xuất", "hiện chưa được kê đơn thuốc nào"};
        for (String err : errorResponses) {
            when(aiTool.getPrescriptions("PAT-01")).thenReturn(err);
            AiChatRequestDto req = new AiChatRequestDto("đơn thuốc của tôi", "CONV-01", "PAT-01");
            ChatResponseDto res = service.sendMessage(req);
            assertNotNull(res);
        }
    }

    @Test
    void testSendMessageStream_MessageCountGreaterThanTwo() throws Exception {
        requestDto = new AiChatRequestDto("cho tôi xem đơn thuốc của tôi", "CONV-01", "PAT-01");
        when(aiMonitoringService.isAiEnabled()).thenReturn(true);
        when(patientService.findById("PAT-01")).thenReturn(Optional.of(patient));
        when(aiConversationService.findById("CONV-01")).thenReturn(Optional.of(conversation));
        when(aiAssistantService.getOrCreateDefaultAssistant()).thenReturn(assistant);
        when(aiTool.getPrescriptions("PAT-01")).thenReturn("DANH SÁCH THUỐC TRONG ĐƠN THUỐC CỦA BỆNH NHÂN:\n1. Metformin");
        when(aiMessageService.countByConversationId("CONV-01")).thenReturn(3L);

        doAnswer(invocation -> {
            Runnable onComplete = invocation.getArgument(5);
            onComplete.run();
            return null;
        }).when(aiProviderManager).generateStreamWithModel(anyString(), anyString(), anyString(), any(), any(), any(), any());

        SseEmitter emitter = service.sendMessageStream(requestDto, 1);
        assertNotNull(emitter);
        Thread.sleep(200);
    }

    @Test
    void testSendMessageStream_NoPrescriptionDirectResponse() throws Exception {
        requestDto = new AiChatRequestDto("cho tôi xem đơn thuốc của tôi", "CONV-01", "PAT-01");
        when(aiMonitoringService.isAiEnabled()).thenReturn(true);
        when(patientService.findById("PAT-01")).thenReturn(Optional.of(patient));
        when(aiConversationService.findById("CONV-01")).thenReturn(Optional.of(conversation));
        when(aiAssistantService.getOrCreateDefaultAssistant()).thenReturn(assistant);

        when(aiTool.getPrescriptions("PAT-01")).thenReturn("DANH SÁCH THUỐC TRONG ĐƠN THUỐC CỦA BỆNH NHÂN:\nhiện chưa được kê đơn thuốc nào");

        SseEmitter emitter = service.sendMessageStream(requestDto, 1);
        assertNotNull(emitter);
        Thread.sleep(200);
    }
}

