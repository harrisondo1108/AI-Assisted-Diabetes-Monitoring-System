package com.quan.diabetes.service.lab;

import com.quan.diabetes.entity.LabResult;
import com.quan.diabetes.repository.LabResultRepository;
import com.quan.diabetes.service.lab.impl.LabResultServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LabResultServiceImplTest {

    @Mock
    private LabResultRepository labResultRepository;

    @InjectMocks
    private LabResultServiceImpl labResultService;

    @Test
    void testFindAll() {
        LabResult r1 = new LabResult();
        r1.setLabResultId("LBR-1");
        LabResult r2 = new LabResult();
        r2.setLabResultId("LBR-2");
        List<LabResult> mockList = Arrays.asList(r1, r2);

        when(labResultRepository.findAll()).thenReturn(mockList);

        List<LabResult> result = labResultService.findAll();

        assertEquals(2, result.size());
        assertEquals("LBR-1", result.get(0).getLabResultId());
        verify(labResultRepository).findAll();
    }

    @Test
    void testFindById_Found() {
        LabResult r = new LabResult();
        r.setLabResultId("LBR-1");

        when(labResultRepository.findById("LBR-1")).thenReturn(Optional.of(r));

        Optional<LabResult> result = labResultService.findById("LBR-1");

        assertTrue(result.isPresent());
        assertEquals("LBR-1", result.get().getLabResultId());
        verify(labResultRepository).findById("LBR-1");
    }

    @Test
    void testFindById_NotFound() {
        when(labResultRepository.findById("LBR-UNKNOWN")).thenReturn(Optional.empty());

        Optional<LabResult> result = labResultService.findById("LBR-UNKNOWN");

        assertFalse(result.isPresent());
    }

    @Test
    void testCreate() {
        LabResult input = new LabResult();
        input.setLabResultId("LBR-1");

        when(labResultRepository.save(input)).thenReturn(input);

        LabResult result = labResultService.create(input);

        assertNotNull(result);
        assertEquals("LBR-1", result.getLabResultId());
        verify(labResultRepository).save(input);
    }

    @Test
    void testUpdate_Success() {
        String id = "LBR-1";
        LabResult input = new LabResult();
        input.setLabResultId(id);

        when(labResultRepository.existsById(id)).thenReturn(true);
        when(labResultRepository.save(input)).thenReturn(input);

        LabResult result = labResultService.update(id, input);

        assertNotNull(result);
        verify(labResultRepository).existsById(id);
        verify(labResultRepository).save(input);
    }

    @Test
    void testUpdate_NotFound() {
        String id = "LBR-UNKNOWN";
        LabResult input = new LabResult();

        when(labResultRepository.existsById(id)).thenReturn(false);

        assertThrows(EntityNotFoundException.class, () -> labResultService.update(id, input));
        verify(labResultRepository, never()).save(any());
    }

    @Test
    void testDeleteById_Success() {
        String id = "LBR-1";

        when(labResultRepository.existsById(id)).thenReturn(true);

        labResultService.deleteById(id);

        verify(labResultRepository).existsById(id);
        verify(labResultRepository).deleteById(id);
    }

    @Test
    void testDeleteById_NotFound() {
        String id = "LBR-UNKNOWN";

        when(labResultRepository.existsById(id)).thenReturn(false);

        assertThrows(EntityNotFoundException.class, () -> labResultService.deleteById(id));
        verify(labResultRepository, never()).deleteById(anyString());
    }

    @Test
    void testExistsById() {
        when(labResultRepository.existsById("LBR-1")).thenReturn(true);

        assertTrue(labResultService.existsById("LBR-1"));
        verify(labResultRepository).existsById("LBR-1");
    }
}
