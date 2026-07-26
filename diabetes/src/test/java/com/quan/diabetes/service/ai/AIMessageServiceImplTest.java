package com.quan.diabetes.service.ai;

import com.quan.diabetes.entity.AIConversation;
import com.quan.diabetes.entity.AIMessage;
import com.quan.diabetes.repository.AIMessageRepository;
import com.quan.diabetes.service.ai.impl.AIMessageServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AIMessageServiceImplTest {

    @Mock
    private AIMessageRepository repository;

    @InjectMocks
    private AIMessageServiceImpl service;

    private AIMessage message;

    @BeforeEach
    void setUp() {
        message = new AIMessage();
        message.setAiMessageId(1L);
        message.setSender("Patient");
        message.setContent("Hello AI");
    }

    @Test
    void testFindAll() {
        when(repository.findAll()).thenReturn(List.of(message));
        List<AIMessage> result = service.findAll();
        assertEquals(1, result.size());
    }

    @Test
    void testFindById() {
        when(repository.findById(1L)).thenReturn(Optional.of(message));
        Optional<AIMessage> result = service.findById(1L);
        assertTrue(result.isPresent());

        when(repository.findById(2L)).thenReturn(Optional.empty());
        assertFalse(service.findById(2L).isPresent());
    }

    @Test
    void testFindByConversationId() {
        when(repository.findByConversationIdOrderByTimeAsc("CONV-01")).thenReturn(List.of(message));
        List<AIMessage> result = service.findByConversationId("CONV-01");
        assertEquals(1, result.size());
    }

    @Test
    void testFindByConversationIdAndSender() {
        when(repository.findByConversationIdAndSender("CONV-01", "Patient")).thenReturn(List.of(message));
        List<AIMessage> result = service.findByConversationIdAndSender("CONV-01", "Patient");
        assertEquals(1, result.size());
    }

    @Test
    void testCountByConversationId() {
        when(repository.countByConversationId("CONV-01")).thenReturn(5L);
        assertEquals(5L, service.countByConversationId("CONV-01"));
    }

    @Test
    void testCreate() {
        when(repository.save(message)).thenReturn(message);
        AIMessage result = service.create(message);
        assertNotNull(result);
    }

    @Test
    void testUpdate_Success() {
        when(repository.existsById(1L)).thenReturn(true);
        when(repository.save(message)).thenReturn(message);

        AIMessage result = service.update(1L, message);
        assertNotNull(result);
        assertEquals(1L, result.getAiMessageId());
    }

    @Test
    void testUpdate_NotFound() {
        when(repository.existsById(99L)).thenReturn(false);
        assertThrows(EntityNotFoundException.class, () -> service.update(99L, message));
    }

    @Test
    void testDeleteById_Success() {
        when(repository.existsById(1L)).thenReturn(true);
        doNothing().when(repository).deleteById(1L);

        assertDoesNotThrow(() -> service.deleteById(1L));
        verify(repository).deleteById(1L);
    }

    @Test
    void testDeleteById_NotFound() {
        when(repository.existsById(99L)).thenReturn(false);
        assertThrows(EntityNotFoundException.class, () -> service.deleteById(99L));
    }

    @Test
    void testDeleteByConversationId_Success() {
        when(repository.findByConversationIdOrderByTimeAsc("CONV-01")).thenReturn(List.of(message));
        doNothing().when(repository).deleteAll(List.of(message));

        assertDoesNotThrow(() -> service.deleteByConversationId("CONV-01"));
        verify(repository).deleteAll(List.of(message));
    }

    @Test
    void testDeleteByConversationId_Empty() {
        when(repository.findByConversationIdOrderByTimeAsc("CONV-01")).thenReturn(new ArrayList<>());
        assertDoesNotThrow(() -> service.deleteByConversationId("CONV-01"));
        verify(repository, never()).deleteAll(anyList());
    }

    @Test
    void testExistsById() {
        when(repository.existsById(1L)).thenReturn(true);
        assertTrue(service.existsById(1L));

        when(repository.existsById(2L)).thenReturn(false);
        assertFalse(service.existsById(2L));
    }

    @Test
    void testSearchByContent() {
        when(repository.searchByContent("hello")).thenReturn(List.of(message));
        List<AIMessage> result = service.searchByContent("hello");
        assertEquals(1, result.size());
    }

    @Test
    void testGetFormattedConversationHistory() {
        AIMessage m1 = new AIMessage();
        m1.setSender("Patient");
        m1.setContent("Binh thuong");

        AIMessage m2 = new AIMessage();
        m2.setSender("Assistant");
        m2.setContent("Chào bạn!");

        when(repository.findTop20ByAiConversation_AiConversationIdOrderByTimeDesc("CONV-01")).thenReturn(List.of(m2, m1));

        String result = service.getFormattedConversationHistory("CONV-01", 2);
        assertTrue(result.contains("Bệnh nhân: Binh thuong"));
        assertTrue(result.contains("Bạn: Chào bạn!"));
    }
}
