package com.quan.diabetes.service.ai;

import com.quan.diabetes.entity.AIAssistant;
import com.quan.diabetes.repository.AIAssistantRepository;
import com.quan.diabetes.service.ai.impl.AIAssistantServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AIAssistantServiceImplTest {

    @Mock
    private AIAssistantRepository repository;

    @InjectMocks
    private AIAssistantServiceImpl service;

    private AIAssistant assistant;

    @BeforeEach
    void setUp() {
        assistant = new AIAssistant();
        assistant.setAiAssistantId(1);
        assistant.setAiName("Test AI");
        assistant.setStatus("Active");
        assistant.setModelName("test-model");

        ReflectionTestUtils.setField(service, "defaultModel", "diabetes");
        ReflectionTestUtils.setField(service, "geminiDefaultModel", "gemini-2.5-flash");
    }

    @Test
    void testFindAll() {
        when(repository.findAll()).thenReturn(List.of(assistant));
        List<AIAssistant> result = service.findAll();
        assertEquals(1, result.size());
        assertEquals("Test AI", result.get(0).getAiName());
    }

    @Test
    void testFindById() {
        when(repository.findById(1)).thenReturn(Optional.of(assistant));
        Optional<AIAssistant> result = service.findById(1);
        assertTrue(result.isPresent());
        assertEquals("Test AI", result.get().getAiName());

        when(repository.findById(2)).thenReturn(Optional.empty());
        assertFalse(service.findById(2).isPresent());
    }

    @Test
    void testFindByStatus() {
        when(repository.findByStatus("Active")).thenReturn(List.of(assistant));
        List<AIAssistant> result = service.findByStatus("Active");
        assertEquals(1, result.size());
    }

    @Test
    void testFindByModelName() {
        when(repository.findByModelName("test-model")).thenReturn(Optional.of(assistant));
        Optional<AIAssistant> result = service.findByModelName("test-model");
        assertTrue(result.isPresent());

        when(repository.findByModelName("other")).thenReturn(Optional.empty());
        assertFalse(service.findByModelName("other").isPresent());
    }

    @Test
    void testCreate_Success() {
        when(repository.save(assistant)).thenReturn(assistant);
        AIAssistant result = service.create(assistant);
        assertNotNull(result);
        assertEquals("Test AI", result.getAiName());
    }

    @Test
    void testCreate_DuplicateName_Success() {
        AIAssistant newAssistant = new AIAssistant();
        newAssistant.setAiName("test ai");
        newAssistant.setStatus("Inactive");
        newAssistant.setModelName("new-model");

        when(repository.save(newAssistant)).thenThrow(new DataIntegrityViolationException("Duplicate key"));
        
        AIAssistant existing = new AIAssistant();
        existing.setAiAssistantId(2);
        existing.setAiName("Test AI");
        existing.setStatus("Active");
        existing.setModelName("old-model");
        
        when(repository.findAll()).thenReturn(List.of(existing));
        when(repository.save(existing)).thenReturn(existing);

        AIAssistant result = service.create(newAssistant);
        assertNotNull(result);
        assertEquals("Inactive", existing.getStatus());
        assertEquals("new-model", existing.getModelName());
        verify(repository, times(2)).save(any());
    }

    @Test
    void testCreate_DuplicateName_NotFound() {
        AIAssistant newAssistant = new AIAssistant();
        newAssistant.setAiName("test ai");

        when(repository.save(newAssistant)).thenThrow(new DataIntegrityViolationException("Duplicate key"));
        when(repository.findAll()).thenReturn(Collections.emptyList());

        assertThrows(DataIntegrityViolationException.class, () -> service.create(newAssistant));
    }

    @Test
    void testUpdate_Success() {
        when(repository.existsById(1)).thenReturn(true);
        when(repository.save(assistant)).thenReturn(assistant);

        AIAssistant result = service.update(1, assistant);
        assertNotNull(result);
        assertEquals(1, result.getAiAssistantId());
    }

    @Test
    void testUpdate_NotFound() {
        when(repository.existsById(99)).thenReturn(false);
        assertThrows(EntityNotFoundException.class, () -> service.update(99, assistant));
    }

    @Test
    void testDeleteById_Success() {
        when(repository.existsById(1)).thenReturn(true);
        doNothing().when(repository).deleteById(1);

        assertDoesNotThrow(() -> service.deleteById(1));
        verify(repository).deleteById(1);
    }

    @Test
    void testDeleteById_NotFound() {
        when(repository.existsById(99)).thenReturn(false);
        assertThrows(EntityNotFoundException.class, () -> service.deleteById(99));
    }

    @Test
    void testExistsById() {
        when(repository.existsById(1)).thenReturn(true);
        assertTrue(service.existsById(1));

        when(repository.existsById(2)).thenReturn(false);
        assertFalse(service.existsById(2));
    }


    @Test
    void testInitDefaultAssistants_NoLocalNoGemini_Empty() {
        when(repository.findAll()).thenReturn(new ArrayList<>());
        when(repository.save(any(AIAssistant.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.initDefaultAssistants();
        verify(repository, times(2)).save(any(AIAssistant.class));
    }

    @Test
    void testInitDefaultAssistants_WithAllAssistantsExistAndCorrect() {
        AIAssistant local = new AIAssistant();
        local.setAiName("Local Ollama Specialist");
        local.setModelName("diabetes");

        AIAssistant gemini = new AIAssistant();
        gemini.setAiName("Gemini Specialist");
        gemini.setModelName("gemini-2.5-flash");

        when(repository.findAll()).thenReturn(List.of(local, gemini));

        service.initDefaultAssistants();
        verify(repository, never()).save(any());
    }

    @Test
    void testInitDefaultAssistants_WithGeminiModelMigration() {
        AIAssistant local = new AIAssistant();
        local.setAiName("local");
        local.setModelName("ollama");

        AIAssistant gemini = new AIAssistant();
        gemini.setAiName("gemini");
        gemini.setModelName("gemini-pro");

        when(repository.findAll()).thenReturn(List.of(local, gemini));
        when(repository.save(gemini)).thenReturn(gemini);

        service.initDefaultAssistants();
        assertEquals("gemini-2.5-flash", gemini.getModelName());
        verify(repository).save(gemini);
    }

    @Test
    void testInitDefaultAssistants_WithNameNullAndNullModel() {
        AIAssistant emptyAss = new AIAssistant();
        emptyAss.setAiName(null);
        emptyAss.setModelName(null);

        when(repository.findAll()).thenReturn(List.of(emptyAss));
        when(repository.save(any(AIAssistant.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.initDefaultAssistants();
        // Since all is not empty, only Gemini is created (1 save call)
        verify(repository, times(1)).save(any(AIAssistant.class));
    }

    @Test
    void testInitDefaultAssistants_CreateException() {
        when(repository.findAll()).thenReturn(new ArrayList<>());
        when(repository.save(any(AIAssistant.class))).thenThrow(new RuntimeException("DB Error"));

        assertDoesNotThrow(() -> service.initDefaultAssistants());
    }

    @Test
    void testSwitchActiveAssistant_Success() {
        AIAssistant ass1 = new AIAssistant();
        ass1.setAiAssistantId(1);
        ass1.setStatus("Active");

        AIAssistant ass2 = new AIAssistant();
        ass2.setAiAssistantId(2);
        ass2.setStatus("Inactive");

        AIAssistant ass3 = new AIAssistant();
        ass3.setAiAssistantId(3);
        ass3.setStatus("Inactive");

        when(repository.findAll()).thenReturn(List.of(ass1, ass2, ass3));
        when(repository.save(any(AIAssistant.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AIAssistant result = service.switchActiveAssistant(2);
        assertNotNull(result);
        assertEquals("Active", ass2.getStatus());
        assertEquals("Inactive", ass1.getStatus());
        assertEquals("Inactive", ass3.getStatus());
    }

    @Test
    void testGetActiveAssistant_InitActiveSuccess() {
        when(repository.findByStatus("Active"))
                .thenReturn(Collections.emptyList())
                .thenReturn(List.of(assistant));

        when(repository.findAll()).thenReturn(new ArrayList<>());
        when(repository.save(any(AIAssistant.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AIAssistant result = service.getActiveAssistant();
        assertNotNull(result);
        assertEquals(assistant, result);
    }

    @Test
    void testSwitchActiveAssistant_NotFound() {
        AIAssistant ass1 = new AIAssistant();
        ass1.setAiAssistantId(1);
        ass1.setStatus("Active");

        when(repository.findAll()).thenReturn(List.of(ass1));

        assertThrows(EntityNotFoundException.class, () -> service.switchActiveAssistant(99));
    }

    @Test
    void testGetActiveAssistant_NotActive_Fallback() {
        AIAssistant local = new AIAssistant();
        local.setAiName("Local Ollama Specialist");
        local.setModelName("diabetes");
        local.setStatus("Inactive");

        AIAssistant gemini = new AIAssistant();
        gemini.setAiName("Gemini Specialist");
        gemini.setModelName("gemini-2.5-flash");
        gemini.setStatus("Inactive");

        when(repository.findByStatus("Active")).thenReturn(Collections.emptyList());
        when(repository.findAll()).thenReturn(List.of(local, gemini));
        when(repository.save(any(AIAssistant.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AIAssistant result = service.getActiveAssistant();
        assertNotNull(result);
        assertEquals("Active", result.getStatus());
        assertEquals("Local Ollama Specialist", result.getAiName());
        verify(repository).save(local);
    }

    @Test
    void testGetActiveAssistant_NoAssistantAtAll() {
        when(repository.findByStatus("Active")).thenReturn(Collections.emptyList());
        when(repository.findAll()).thenReturn(Collections.emptyList());

        assertThrows(RuntimeException.class, () -> service.getActiveAssistant());
    }

    @Test
    void testGetOrCreateDefaultAssistant() {
        when(repository.findByStatus("Active")).thenReturn(List.of(assistant));
        AIAssistant result = service.getOrCreateDefaultAssistant();
        assertEquals(assistant, result);
    }

    @Test
    void testCreate_DuplicateName_NPE_OnNullAiName() {
        AIAssistant newAssistant = new AIAssistant();
        newAssistant.setAiName("test ai");

        AIAssistant nullNameAssistant = new AIAssistant();
        nullNameAssistant.setAiName(null);

        when(repository.save(newAssistant)).thenThrow(new DataIntegrityViolationException("Duplicate key"));
        when(repository.findAll()).thenReturn(List.of(nullNameAssistant));

        assertThrows(NullPointerException.class, () -> service.create(newAssistant));
    }

    @Test
    void testInitDefaultAssistants_ShortCircuitBranches() {
        AIAssistant a1 = new AIAssistant();
        a1.setAiName("ollama");
        a1.setModelName("other");

        AIAssistant a2 = new AIAssistant();
        a2.setAiName("other");
        a2.setModelName("diabetes");

        AIAssistant a3 = new AIAssistant();
        a3.setAiName("other");
        a3.setModelName("ollama");

        AIAssistant a4 = new AIAssistant();
        a4.setAiName("other");
        a4.setModelName("gemini");

        when(repository.findAll()).thenReturn(List.of(a1, a2, a3, a4));
        when(repository.save(any(AIAssistant.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.initDefaultAssistants();
        verify(repository).save(a4);
        assertEquals("gemini-2.5-flash", a4.getModelName());
    }
}
