package com.quan.diabetes.service.ai;

import com.quan.diabetes.entity.AIConversation;
import com.quan.diabetes.repository.AIConversationRepository;
import com.quan.diabetes.service.ai.impl.AIConversationServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AIConversationServiceImplTest {

    @Mock
    private AIConversationRepository repository;

    @InjectMocks
    private AIConversationServiceImpl service;

    private AIConversation conversation;

    @BeforeEach
    void setUp() {
        conversation = new AIConversation();
        conversation.setAiConversationId("CONV-01");
    }

    @Test
    void testFindAll() {
        when(repository.findAll()).thenReturn(List.of(conversation));
        List<AIConversation> result = service.findAll();
        assertEquals(1, result.size());
        assertEquals("CONV-01", result.get(0).getAiConversationId());
    }

    @Test
    void testFindById() {
        when(repository.findById("CONV-01")).thenReturn(Optional.of(conversation));
        Optional<AIConversation> result = service.findById("CONV-01");
        assertTrue(result.isPresent());

        when(repository.findById("CONV-02")).thenReturn(Optional.empty());
        assertFalse(service.findById("CONV-02").isPresent());
    }

    @Test
    void testFindByPatientId() {
        when(repository.findByPatientUserIdOrderByCreatedAtDesc("PAT-01")).thenReturn(List.of(conversation));
        List<AIConversation> result = service.findByPatientId("PAT-01");
        assertEquals(1, result.size());
    }

    @Test
    void testFindByAssistantId() {
        when(repository.findByAiAssistantIdOrderByCreatedAtDesc(1)).thenReturn(List.of(conversation));
        List<AIConversation> result = service.findByAssistantId(1);
        assertEquals(1, result.size());
    }

    @Test
    void testFindByPatientIdAndAssistantId() {
        when(repository.findByPatientUserIdAndAiAssistantId("PAT-01", 1)).thenReturn(List.of(conversation));
        List<AIConversation> result = service.findByPatientIdAndAssistantId("PAT-01", 1);
        assertEquals(1, result.size());
    }

    @Test
    void testCreate() {
        when(repository.save(conversation)).thenReturn(conversation);
        AIConversation result = service.create(conversation);
        assertNotNull(result);
    }

    @Test
    void testUpdate_Success() {
        when(repository.existsById("CONV-01")).thenReturn(true);
        when(repository.save(conversation)).thenReturn(conversation);

        AIConversation result = service.update("CONV-01", conversation);
        assertNotNull(result);
        assertEquals("CONV-01", result.getAiConversationId());
    }

    @Test
    void testUpdate_NotFound() {
        when(repository.existsById("CONV-99")).thenReturn(false);
        assertThrows(EntityNotFoundException.class, () -> service.update("CONV-99", conversation));
    }

    @Test
    void testDeleteById_Success() {
        when(repository.existsById("CONV-01")).thenReturn(true);
        doNothing().when(repository).deleteById("CONV-01");

        assertDoesNotThrow(() -> service.deleteById("CONV-01"));
        verify(repository).deleteById("CONV-01");
    }

    @Test
    void testDeleteById_NotFound() {
        when(repository.existsById("CONV-99")).thenReturn(false);
        assertThrows(EntityNotFoundException.class, () -> service.deleteById("CONV-99"));
    }

    @Test
    void testExistsById() {
        when(repository.existsById("CONV-01")).thenReturn(true);
        assertTrue(service.existsById("CONV-01"));

        when(repository.existsById("CONV-02")).thenReturn(false);
        assertFalse(service.existsById("CONV-02"));
    }

    @Test
    void testCountByPatientId() {
        when(repository.findByPatientUserIdOrderByCreatedAtDesc("PAT-01")).thenReturn(List.of(conversation));
        assertEquals(1, service.countByPatientId("PAT-01"));
    }
}
