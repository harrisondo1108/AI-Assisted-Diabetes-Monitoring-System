package com.quan.diabetes.service.medication;

import com.quan.diabetes.entity.MedicationTiming;
import com.quan.diabetes.repository.MedicationTimingRepository;
import com.quan.diabetes.service.medication.impl.MedicationTimingServiceImpl;
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
class MedicationTimingServiceImplTest {

    @Mock
    private MedicationTimingRepository repository;

    @InjectMocks
    private MedicationTimingServiceImpl service;

    private MedicationTiming timing;

    @BeforeEach
    void setUp() {
        timing = new MedicationTiming();
        timing.setTimingID(1);
        timing.setTimingName("Sáng");
    }

    @Test
    void testSave() {
        when(repository.save(timing)).thenReturn(timing);
        assertEquals(timing, service.save(timing));
        verify(repository).save(timing);
    }

    @Test
    void testCreate() {
        when(repository.save(timing)).thenReturn(timing);
        assertEquals(timing, service.create(timing));
        verify(repository).save(timing);
    }

    @Test
    void testUpdate_Direct() {
        when(repository.save(timing)).thenReturn(timing);
        assertEquals(timing, service.update(timing));
        verify(repository).save(timing);
    }

    @Test
    void testUpdateWithId_EntityNotFound() {
        when(repository.existsById(99)).thenReturn(false);
        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class, () -> service.update(99, timing));
        assertEquals("MedicationTiming not found with id: 99", ex.getMessage());
    }

    @Test
    void testUpdateWithId_Success() {
        when(repository.existsById(1)).thenReturn(true);
        when(repository.save(timing)).thenReturn(timing);
        assertEquals(timing, service.update(1, timing));
        verify(repository).save(timing);
    }

    @Test
    void testDeleteById_EntityNotFound() {
        when(repository.existsById(99)).thenReturn(false);
        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class, () -> service.deleteById(99));
        assertEquals("MedicationTiming not found with id: 99", ex.getMessage());
    }

    @Test
    void testDeleteById_Success() {
        when(repository.existsById(1)).thenReturn(true);
        service.deleteById(1);
        verify(repository).deleteById(1);
    }

    @Test
    void testFindById() {
        when(repository.findById(1)).thenReturn(Optional.of(timing));
        Optional<MedicationTiming> result = service.findById(1);
        assertTrue(result.isPresent());
        assertEquals(timing, result.get());
    }

    @Test
    void testFindAll() {
        when(repository.findAll()).thenReturn(List.of(timing));
        assertEquals(List.of(timing), service.findAll());
    }

    @Test
    void testExistsByTimingName() {
        when(repository.existsByTimingName("Sáng")).thenReturn(true);
        assertTrue(service.existsByTimingName("Sáng"));

        when(repository.existsByTimingName("Tối")).thenReturn(false);
        assertFalse(service.existsByTimingName("Tối"));
    }

    @Test
    void testExistsByTimingNameAndTimingIdNot() {
        when(repository.existsByTimingNameAndTimingIDNot("Sáng", 2)).thenReturn(true);
        assertTrue(service.existsByTimingNameAndTimingIdNot("Sáng", 2));

        when(repository.existsByTimingNameAndTimingIDNot("Tối", 2)).thenReturn(false);
        assertFalse(service.existsByTimingNameAndTimingIdNot("Tối", 2));
    }

    @Test
    void testSearchByKeyword_NullOrEmpty() {
        when(repository.findAll()).thenReturn(List.of(timing));

        assertEquals(List.of(timing), service.searchByKeyword(null));
        assertEquals(List.of(timing), service.searchByKeyword("   "));
        verify(repository, times(2)).findAll();
    }

    @Test
    void testSearchByKeyword_NonEmpty_AllBranches() {
        MedicationTiming t1 = new MedicationTiming();
        t1.setTimingName("Sáng sớm"); // matches "sáng"

        MedicationTiming t2 = new MedicationTiming();
        t2.setTimingName("Tối muộn"); // does not match "sáng"

        when(repository.findAll()).thenReturn(List.of(t1, t2));

        List<MedicationTiming> result = service.searchByKeyword("sáng");
        assertEquals(1, result.size());
        assertTrue(result.contains(t1));
    }
}
