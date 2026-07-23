package com.quan.diabetes.service.medication;

import com.quan.diabetes.entity.Prescription;
import com.quan.diabetes.repository.PrescriptionRepository;
import com.quan.diabetes.service.medication.impl.PrescriptionServiceImpl;
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
class PrescriptionServiceImplTest {

    @Mock
    private PrescriptionRepository repository;

    @InjectMocks
    private PrescriptionServiceImpl service;

    private Prescription prescription;

    @BeforeEach
    void setUp() {
        prescription = new Prescription();
        prescription.setPrescriptionId("PR-01");
    }

    @Test
    void testFindAll() {
        when(repository.findAll()).thenReturn(List.of(prescription));
        assertEquals(List.of(prescription), service.findAll());
        verify(repository).findAll();
    }

    @Test
    void testFindById() {
        when(repository.findById("PR-01")).thenReturn(Optional.of(prescription));
        Optional<Prescription> result = service.findById("PR-01");
        assertTrue(result.isPresent());
        assertEquals(prescription, result.get());
    }

    @Test
    void testCreate() {
        when(repository.save(prescription)).thenReturn(prescription);
        assertEquals(prescription, service.create(prescription));
        verify(repository).save(prescription);
    }

    @Test
    void testUpdate_EntityNotFound() {
        when(repository.existsById("PR-99")).thenReturn(false);
        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class, () -> service.update("PR-99", prescription));
        assertEquals("Prescription not found with id: PR-99", ex.getMessage());
    }

    @Test
    void testUpdate_Success() {
        when(repository.existsById("PR-01")).thenReturn(true);
        when(repository.save(prescription)).thenReturn(prescription);
        assertEquals(prescription, service.update("PR-01", prescription));
        verify(repository).save(prescription);
    }

    @Test
    void testDeleteById_EntityNotFound() {
        when(repository.existsById("PR-99")).thenReturn(false);
        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class, () -> service.deleteById("PR-99"));
        assertEquals("Prescription not found with id: PR-99", ex.getMessage());
    }

    @Test
    void testDeleteById_Success() {
        when(repository.existsById("PR-01")).thenReturn(true);
        service.deleteById("PR-01");
        verify(repository).deleteById("PR-01");
    }

    @Test
    void testExistsById() {
        when(repository.existsById("PR-01")).thenReturn(true);
        assertTrue(service.existsById("PR-01"));

        when(repository.existsById("PR-99")).thenReturn(false);
        assertFalse(service.existsById("PR-99"));
    }
}
