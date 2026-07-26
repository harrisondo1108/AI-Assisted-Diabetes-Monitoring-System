package com.quan.diabetes.service.medication;

import com.quan.diabetes.entity.PrescriptionTiming;
import com.quan.diabetes.repository.PrescriptionTimingRepository;
import com.quan.diabetes.service.medication.impl.PrescriptionTimingServiceImpl;
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
class PrescriptionTimingServiceImplTest {

    @Mock
    private PrescriptionTimingRepository repository;

    @InjectMocks
    private PrescriptionTimingServiceImpl service;

    private PrescriptionTiming timing;

    @BeforeEach
    void setUp() {
        timing = new PrescriptionTiming();
    }

    @Test
    void testSave() {
        when(repository.save(timing)).thenReturn(timing);
        assertEquals(timing, service.save(timing));
        verify(repository).save(timing);
    }

    @Test
    void testUpdate() {
        when(repository.save(timing)).thenReturn(timing);
        assertEquals(timing, service.update(timing));
        verify(repository).save(timing);
    }

    @Test
    void testDeleteById() {
        service.deleteById(1L);
        verify(repository).deleteById(1L);
    }

    @Test
    void testFindById() {
        when(repository.findById(1L)).thenReturn(Optional.of(timing));
        assertEquals(timing, service.findById(1L));

        when(repository.findById(2L)).thenReturn(Optional.empty());
        assertNull(service.findById(2L));
    }

    @Test
    void testFindAll() {
        when(repository.findAll()).thenReturn(List.of(timing));
        assertEquals(List.of(timing), service.findAll());
    }
}
