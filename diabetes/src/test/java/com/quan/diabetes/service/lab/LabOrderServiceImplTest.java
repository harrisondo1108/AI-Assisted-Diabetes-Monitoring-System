package com.quan.diabetes.service.lab;

import com.quan.diabetes.entity.LabOrder;
import com.quan.diabetes.repository.LabOrderRepository;
import com.quan.diabetes.service.lab.impl.LabOrderServiceImpl;
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
class LabOrderServiceImplTest {

    @Mock
    private LabOrderRepository labOrderRepository;

    @InjectMocks
    private LabOrderServiceImpl labOrderService;

    @Test
    void testFindAll() {
        LabOrder o1 = new LabOrder();
        o1.setLabOrderId("LBO-1");
        LabOrder o2 = new LabOrder();
        o2.setLabOrderId("LBO-2");
        List<LabOrder> mockList = Arrays.asList(o1, o2);

        when(labOrderRepository.findAll()).thenReturn(mockList);

        List<LabOrder> result = labOrderService.findAll();

        assertEquals(2, result.size());
        assertEquals("LBO-1", result.get(0).getLabOrderId());
        verify(labOrderRepository).findAll();
    }

    @Test
    void testFindById_Found() {
        LabOrder o = new LabOrder();
        o.setLabOrderId("LBO-1");

        when(labOrderRepository.findById("LBO-1")).thenReturn(Optional.of(o));

        Optional<LabOrder> result = labOrderService.findById("LBO-1");

        assertTrue(result.isPresent());
        assertEquals("LBO-1", result.get().getLabOrderId());
        verify(labOrderRepository).findById("LBO-1");
    }

    @Test
    void testFindById_NotFound() {
        when(labOrderRepository.findById("LBO-UNKNOWN")).thenReturn(Optional.empty());

        Optional<LabOrder> result = labOrderService.findById("LBO-UNKNOWN");

        assertFalse(result.isPresent());
    }

    @Test
    void testCreate() {
        LabOrder input = new LabOrder();
        input.setLabOrderId("LBO-1");

        when(labOrderRepository.save(input)).thenReturn(input);

        LabOrder result = labOrderService.create(input);

        assertNotNull(result);
        assertEquals("LBO-1", result.getLabOrderId());
        verify(labOrderRepository).save(input);
    }

    @Test
    void testUpdate_Success() {
        String id = "LBO-1";
        LabOrder input = new LabOrder();
        input.setLabOrderId(id);

        when(labOrderRepository.existsById(id)).thenReturn(true);
        when(labOrderRepository.save(input)).thenReturn(input);

        LabOrder result = labOrderService.update(id, input);

        assertNotNull(result);
        verify(labOrderRepository).existsById(id);
        verify(labOrderRepository).save(input);
    }

    @Test
    void testUpdate_NotFound() {
        String id = "LBO-UNKNOWN";
        LabOrder input = new LabOrder();

        when(labOrderRepository.existsById(id)).thenReturn(false);

        assertThrows(EntityNotFoundException.class, () -> labOrderService.update(id, input));
        verify(labOrderRepository, never()).save(any());
    }

    @Test
    void testDeleteById_Success() {
        String id = "LBO-1";

        when(labOrderRepository.existsById(id)).thenReturn(true);

        labOrderService.deleteById(id);

        verify(labOrderRepository).existsById(id);
        verify(labOrderRepository).deleteById(id);
    }

    @Test
    void testDeleteById_NotFound() {
        String id = "LBO-UNKNOWN";

        when(labOrderRepository.existsById(id)).thenReturn(false);

        assertThrows(EntityNotFoundException.class, () -> labOrderService.deleteById(id));
        verify(labOrderRepository, never()).deleteById(anyString());
    }

    @Test
    void testExistsById() {
        when(labOrderRepository.existsById("LBO-1")).thenReturn(true);

        assertTrue(labOrderService.existsById("LBO-1"));
        verify(labOrderRepository).existsById("LBO-1");
    }
}
