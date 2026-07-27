package com.quan.diabetes.service.lab;

import com.quan.diabetes.entity.LabTestCatalog;
import com.quan.diabetes.repository.LabTestCatalogRepository;
import com.quan.diabetes.service.lab.impl.LabTestCatalogServiceImpl;
import com.quan.diabetes.service.systemlog.SystemLogService;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LabTestCatalogServiceImplTest {

    @Mock
    private LabTestCatalogRepository labTestCatalogRepository;

    @Mock
    private SystemLogService systemLogService;

    @InjectMocks
    private LabTestCatalogServiceImpl labTestCatalogService;

    @Test
    void testFindAll() {
        LabTestCatalog test1 = new LabTestCatalog();
        test1.setLabTestId("LT-1");
        LabTestCatalog test2 = new LabTestCatalog();
        test2.setLabTestId("LT-2");
        List<LabTestCatalog> mockList = Arrays.asList(test1, test2);

        when(labTestCatalogRepository.findAll()).thenReturn(mockList);

        List<LabTestCatalog> result = labTestCatalogService.findAll();

        assertEquals(2, result.size());
        assertEquals("LT-1", result.get(0).getLabTestId());
        assertEquals("LT-2", result.get(1).getLabTestId());
        verify(labTestCatalogRepository, times(1)).findAll();
    }

    @Test
    void testFindById_Found() {
        LabTestCatalog test = new LabTestCatalog();
        test.setLabTestId("LT-1");

        when(labTestCatalogRepository.findById("LT-1")).thenReturn(Optional.of(test));

        Optional<LabTestCatalog> result = labTestCatalogService.findById("LT-1");

        assertTrue(result.isPresent());
        assertEquals("LT-1", result.get().getLabTestId());
        verify(labTestCatalogRepository, times(1)).findById("LT-1");
    }

    @Test
    void testFindById_NotFound() {
        when(labTestCatalogRepository.findById("LT-UNKNOWN")).thenReturn(Optional.empty());

        Optional<LabTestCatalog> result = labTestCatalogService.findById("LT-UNKNOWN");

        assertFalse(result.isPresent());
        verify(labTestCatalogRepository, times(1)).findById("LT-UNKNOWN");
    }

    @Test
    void testCreate() {
        LabTestCatalog input = new LabTestCatalog();
        input.setTestName("Glucose");
        input.setLabTestId("LT-GEN");

        LabTestCatalog saved = new LabTestCatalog();
        saved.setLabTestId("LT-GEN");
        saved.setTestName("Glucose");

        when(labTestCatalogRepository.save(input)).thenReturn(saved);

        LabTestCatalog result = labTestCatalogService.create(input);

        assertNotNull(result);
        assertEquals("LT-GEN", result.getLabTestId());
        verify(labTestCatalogRepository, times(1)).save(input);
        verify(systemLogService, times(1)).saveLogWithObject(
                isNull(), eq("CREATE"), eq("LaboratoryTest"), eq("LT-GEN"),
                eq("Thêm xét nghiệm mới"), isNull(), eq(saved), eq("SUCCESS")
        );
    }

    @Test
    void testUpdate_Success() {
        String id = "LT-1";
        LabTestCatalog existing = new LabTestCatalog();
        existing.setLabTestId(id);
        existing.setTestName("Glucose");
        existing.setUnit("mmol/L");
        existing.setRoomId(1);
        existing.setStatus(true);

        LabTestCatalog input = new LabTestCatalog();
        input.setLabTestId(id);
        input.setTestName("Glucose Updated");
        input.setUnit("mg/dL");
        input.setRoomId(1);
        input.setStatus(true);

        when(labTestCatalogRepository.findById(id)).thenReturn(Optional.of(existing));
        when(labTestCatalogRepository.save(input)).thenReturn(input);

        LabTestCatalog result = labTestCatalogService.update(id, input);

        assertNotNull(result);
        assertEquals("Glucose Updated", result.getTestName());
        verify(labTestCatalogRepository, times(1)).findById(id);
        verify(labTestCatalogRepository, times(1)).save(input);
        verify(systemLogService, times(1)).saveLogWithObject(
                isNull(), eq("UPDATE"), eq("LaboratoryTest"), eq(id),
                eq("Cập nhật xét nghiệm"), any(LabTestCatalog.class), eq(input), eq("SUCCESS")
        );
    }

    @Test
    void testUpdate_NotFound() {
        String id = "LT-UNKNOWN";
        LabTestCatalog input = new LabTestCatalog();

        when(labTestCatalogRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> labTestCatalogService.update(id, input));
        verify(labTestCatalogRepository, times(1)).findById(id);
        verify(labTestCatalogRepository, never()).save(any());
        verify(systemLogService, never()).saveLogWithObject(any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void testDeleteById_Success() {
        String id = "LT-1";
        LabTestCatalog existing = new LabTestCatalog();
        existing.setLabTestId(id);

        when(labTestCatalogRepository.findById(id)).thenReturn(Optional.of(existing));

        labTestCatalogService.deleteById(id);

        verify(labTestCatalogRepository, times(1)).findById(id);
        verify(labTestCatalogRepository, times(1)).deleteById(id);
        verify(systemLogService, times(1)).saveLogWithObject(
                isNull(), eq("DELETE"), eq("LaboratoryTest"), eq(id),
                eq("Xóa xét nghiệm"), eq(existing), isNull(), eq("SUCCESS")
        );
    }

    @Test
    void testDeleteById_NotFound() {
        String id = "LT-UNKNOWN";

        when(labTestCatalogRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> labTestCatalogService.deleteById(id));
        verify(labTestCatalogRepository, times(1)).findById(id);
        verify(labTestCatalogRepository, never()).deleteById(anyString());
        verify(systemLogService, never()).saveLogWithObject(any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void testExistsById() {
        when(labTestCatalogRepository.existsById("LT-1")).thenReturn(true);

        assertTrue(labTestCatalogService.existsById("LT-1"));
        verify(labTestCatalogRepository, times(1)).existsById("LT-1");
    }

    @Test
    void testExistsByTestName() {
        when(labTestCatalogRepository.existsByTestName("Glucose")).thenReturn(true);

        assertTrue(labTestCatalogService.existsByTestName("Glucose"));
        verify(labTestCatalogRepository, times(1)).existsByTestName("Glucose");
    }

    @Test
    void testExistsByTestNameAndLabTestIdNot() {
        when(labTestCatalogRepository.existsByTestNameAndLabTestIdNot("Glucose", "LT-1")).thenReturn(true);

        assertTrue(labTestCatalogService.existsByTestNameAndLabTestIdNot("Glucose", "LT-1"));
        verify(labTestCatalogRepository, times(1)).existsByTestNameAndLabTestIdNot("Glucose", "LT-1");
    }

    @Test
    void testGenerateLabTestId() {
        String id = labTestCatalogService.generateLabTestId();
        assertNotNull(id);
        assertTrue(id.startsWith("LT-"));
        assertEquals(11, id.length()); // "LT-" + 8 char UUID substring
    }

    @Test
    void testSearchByKeywordAndStatus() {
        LabTestCatalog t1 = new LabTestCatalog();
        t1.setTestName("Glucose Test");
        t1.setUnit("mmol/L");
        t1.setStatus(true);

        LabTestCatalog t2 = new LabTestCatalog();
        t2.setTestName("HbA1c");
        t2.setUnit("percent");
        t2.setStatus(false);

        LabTestCatalog t3 = new LabTestCatalog();
        t3.setTestName("Cholesterol");
        t3.setUnit("mg/dL");
        t3.setStatus(true);

        List<LabTestCatalog> allTests = Arrays.asList(t1, t2, t3);
        when(labTestCatalogRepository.findAll()).thenReturn(allTests);

        // Null status, matching keyword
        List<LabTestCatalog> result1 = labTestCatalogService.searchByKeywordAndStatus("glu", null);
        assertEquals(1, result1.size());
        assertEquals("Glucose Test", result1.get(0).getTestName());

        List<LabTestCatalog> result2 = labTestCatalogService.searchByKeywordAndStatus("HbA", null);
        assertEquals(1, result2.size());
        assertEquals("HbA1c", result2.get(0).getTestName());

        // Matching status, matching keyword
        List<LabTestCatalog> result3 = labTestCatalogService.searchByKeywordAndStatus("glu", true);
        assertEquals(1, result3.size());
        assertEquals("Glucose Test", result3.get(0).getTestName());

        // Matching status, keyword not matching
        List<LabTestCatalog> result4 = labTestCatalogService.searchByKeywordAndStatus("glu", false);
        assertEquals(0, result4.size());

        // Unit matching
        List<LabTestCatalog> result5 = labTestCatalogService.searchByKeywordAndStatus("percent", null);
        assertEquals(1, result5.size());
        assertEquals("HbA1c", result5.get(0).getTestName());
    }
}
