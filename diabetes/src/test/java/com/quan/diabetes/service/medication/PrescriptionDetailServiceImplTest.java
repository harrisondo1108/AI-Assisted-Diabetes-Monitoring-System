package com.quan.diabetes.service.medication;

import com.quan.diabetes.entity.PrescriptionDetail;
import com.quan.diabetes.repository.PrescriptionDetailRepository;
import com.quan.diabetes.service.medication.impl.PrescriptionDetailServiceImpl;
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
class PrescriptionDetailServiceImplTest {

    @Mock
    private PrescriptionDetailRepository repository;

    @InjectMocks
    private PrescriptionDetailServiceImpl service;

    private PrescriptionDetail detail;

    @BeforeEach
    void setUp() {
        detail = new PrescriptionDetail();
        detail.setPrescriptionDetailId("PD-01");
    }

    @Test
    void testFindAll() {
        when(repository.findAll()).thenReturn(List.of(detail));
        assertEquals(List.of(detail), service.findAll());
        verify(repository).findAll();
    }

    @Test
    void testFindById() {
        when(repository.findById("PD-01")).thenReturn(Optional.of(detail));
        Optional<PrescriptionDetail> result = service.findById("PD-01");
        assertTrue(result.isPresent());
        assertEquals(detail, result.get());
    }

    @Test
    void testCreate() {
        when(repository.save(detail)).thenReturn(detail);
        assertEquals(detail, service.create(detail));
        verify(repository).save(detail);
    }

    @Test
    void testUpdate_EntityNotFound() {
        when(repository.existsById("PD-99")).thenReturn(false);
        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class, () -> service.update("PD-99", detail));
        assertEquals("PrescriptionDetail not found with id: PD-99", ex.getMessage());
    }

    @Test
    void testUpdate_Success() {
        when(repository.existsById("PD-01")).thenReturn(true);
        when(repository.save(detail)).thenReturn(detail);
        assertEquals(detail, service.update("PD-01", detail));
        verify(repository).save(detail);
    }

    @Test
    void testDeleteById_EntityNotFound() {
        when(repository.existsById("PD-99")).thenReturn(false);
        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class, () -> service.deleteById("PD-99"));
        assertEquals("PrescriptionDetail not found with id: PD-99", ex.getMessage());
    }

    @Test
    void testDeleteById_Success() {
        when(repository.existsById("PD-01")).thenReturn(true);
        service.deleteById("PD-01");
        verify(repository).deleteById("PD-01");
    }

    @Test
    void testExistsById() {
        when(repository.existsById("PD-01")).thenReturn(true);
        assertTrue(service.existsById("PD-01"));

        when(repository.existsById("PD-99")).thenReturn(false);
        assertFalse(service.existsById("PD-99"));
    }
}
