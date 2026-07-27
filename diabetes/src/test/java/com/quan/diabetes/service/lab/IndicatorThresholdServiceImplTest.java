package com.quan.diabetes.service.lab;

import com.quan.diabetes.entity.IndicatorThreshold;
import com.quan.diabetes.repository.IndicatorThresholdRepository;
import com.quan.diabetes.service.lab.impl.IndicatorThresholdServiceImpl;
import com.quan.diabetes.service.systemlog.SystemLogService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IndicatorThresholdServiceImplTest {

    @Mock
    private IndicatorThresholdRepository indicatorThresholdRepository;

    @Mock
    private SystemLogService systemLogService;

    @InjectMocks
    private IndicatorThresholdServiceImpl indicatorThresholdService;

    @Test
    void testFindAll() {
        IndicatorThreshold t1 = new IndicatorThreshold();
        t1.setThresholdId(1);
        IndicatorThreshold t2 = new IndicatorThreshold();
        t2.setThresholdId(2);
        List<IndicatorThreshold> mockList = Arrays.asList(t1, t2);

        when(indicatorThresholdRepository.findAll()).thenReturn(mockList);

        List<IndicatorThreshold> result = indicatorThresholdService.findAll();

        assertEquals(2, result.size());
        assertEquals(1, result.get(0).getThresholdId());
        assertEquals(2, result.get(1).getThresholdId());
        verify(indicatorThresholdRepository, times(1)).findAll();
    }

    @Test
    void testFindById_Found() {
        IndicatorThreshold t = new IndicatorThreshold();
        t.setThresholdId(1);

        when(indicatorThresholdRepository.findById(1)).thenReturn(Optional.of(t));

        Optional<IndicatorThreshold> result = indicatorThresholdService.findById(1);

        assertTrue(result.isPresent());
        assertEquals(1, result.get().getThresholdId());
        verify(indicatorThresholdRepository, times(1)).findById(1);
    }

    @Test
    void testFindById_NotFound() {
        when(indicatorThresholdRepository.findById(999)).thenReturn(Optional.empty());

        Optional<IndicatorThreshold> result = indicatorThresholdService.findById(999);

        assertFalse(result.isPresent());
        verify(indicatorThresholdRepository, times(1)).findById(999);
    }

    @Test
    void testCreate() {
        IndicatorThreshold input = new IndicatorThreshold();
        input.setMinValue(new BigDecimal("3.5"));
        input.setMaxValue(new BigDecimal("5.5"));

        IndicatorThreshold saved = new IndicatorThreshold();
        saved.setThresholdId(10);
        saved.setMinValue(new BigDecimal("3.5"));
        saved.setMaxValue(new BigDecimal("5.5"));

        when(indicatorThresholdRepository.save(input)).thenReturn(saved);

        IndicatorThreshold result = indicatorThresholdService.create(input);

        assertNotNull(result);
        assertEquals(10, result.getThresholdId());
        verify(indicatorThresholdRepository, times(1)).save(input);
        verify(systemLogService, times(1)).saveLogWithObject(
                isNull(), eq("CREATE"), eq("Threshold"), eq("10"),
                eq("Thêm ngưỡng chỉ số mới"), isNull(), eq(saved), eq("SUCCESS")
        );
    }

    @Test
    void testUpdate_Success() {
        Integer id = 10;
        IndicatorThreshold existing = new IndicatorThreshold();
        existing.setThresholdId(id);
        existing.setMinValue(new BigDecimal("3.5"));
        existing.setMaxValue(new BigDecimal("5.5"));

        IndicatorThreshold input = new IndicatorThreshold();
        input.setMinValue(new BigDecimal("4.0"));
        input.setMaxValue(new BigDecimal("6.0"));

        when(indicatorThresholdRepository.existsById(id)).thenReturn(true);
        when(indicatorThresholdRepository.findById(id)).thenReturn(Optional.of(existing));
        when(indicatorThresholdRepository.save(input)).thenReturn(input);

        IndicatorThreshold result = indicatorThresholdService.update(id, input);

        assertNotNull(result);
        assertEquals(id, result.getThresholdId());
        assertEquals(new BigDecimal("4.0"), result.getMinValue());
        verify(indicatorThresholdRepository, times(1)).existsById(id);
        verify(indicatorThresholdRepository, times(1)).findById(id);
        verify(indicatorThresholdRepository, times(1)).save(input);
        verify(systemLogService, times(1)).saveLogWithObject(
                isNull(), eq("UPDATE"), eq("Threshold"), eq("10"),
                eq("Cập nhật ngưỡng chỉ số"), any(IndicatorThreshold.class), eq(input), eq("SUCCESS")
        );
    }

    @Test
    void testUpdate_NotFound() {
        Integer id = 999;
        IndicatorThreshold input = new IndicatorThreshold();

        when(indicatorThresholdRepository.existsById(id)).thenReturn(false);

        assertThrows(RuntimeException.class, () -> indicatorThresholdService.update(id, input));
        verify(indicatorThresholdRepository, times(1)).existsById(id);
        verify(indicatorThresholdRepository, never()).findById(any());
        verify(indicatorThresholdRepository, never()).save(any());
        verify(systemLogService, never()).saveLogWithObject(any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void testDeleteById_Success() {
        Integer id = 10;
        IndicatorThreshold existing = new IndicatorThreshold();
        existing.setThresholdId(id);

        when(indicatorThresholdRepository.findById(id)).thenReturn(Optional.of(existing));

        indicatorThresholdService.deleteById(id);

        verify(indicatorThresholdRepository, times(1)).findById(id);
        verify(indicatorThresholdRepository, times(1)).deleteById(id);
        verify(systemLogService, times(1)).saveLogWithObject(
                isNull(), eq("DELETE"), eq("Threshold"), eq("10"),
                eq("Xóa ngưỡng chỉ số"), eq(existing), isNull(), eq("SUCCESS")
        );
    }

    @Test
    void testExistsById() {
        when(indicatorThresholdRepository.existsById(10)).thenReturn(true);

        assertTrue(indicatorThresholdService.existsById(10));
        verify(indicatorThresholdRepository, times(1)).existsById(10);
    }
}
