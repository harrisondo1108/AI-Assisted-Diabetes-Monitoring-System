package com.quan.diabetes.service.exam;

import com.quan.diabetes.entity.SymptomsCatalog;
import com.quan.diabetes.repository.SymptomsCatalogRepository;
import com.quan.diabetes.service.exam.impl.SymptomsCatalogServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SymptomsCatalogServiceImplTest {

    @Mock
    private SymptomsCatalogRepository repository;

    @InjectMocks
    private SymptomsCatalogServiceImpl symptomsCatalogService;

    @Test
    @DisplayName("findAll - Should return paged symptoms")
    void findAll_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        SymptomsCatalog s = new SymptomsCatalog();
        when(repository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(s)));

        Page<SymptomsCatalog> result = symptomsCatalogService.findAll(pageable);
        assertEquals(1, result.getContent().size());
    }

    @Test
    @DisplayName("findByStatus - Should filter by status or return all when status is null")
    void findByStatus_Test() {
        Pageable pageable = PageRequest.of(0, 10);
        SymptomsCatalog s = new SymptomsCatalog();

        when(repository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(s)));
        Page<SymptomsCatalog> resultNull = symptomsCatalogService.findByStatus(null, pageable);
        assertEquals(1, resultNull.getContent().size());

        when(repository.findByStatus(true, pageable)).thenReturn(new PageImpl<>(List.of(s)));
        Page<SymptomsCatalog> resultTrue = symptomsCatalogService.findByStatus(true, pageable);
        assertEquals(1, resultTrue.getContent().size());
    }

    @Test
    @DisplayName("searchByKeywordAndStatus - Null or empty keyword calls findByStatus")
    void searchByKeywordAndStatus_EmptyKeyword() {
        Pageable pageable = PageRequest.of(0, 10);
        SymptomsCatalog s = new SymptomsCatalog();
        when(repository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(s)));

        Page<SymptomsCatalog> result = symptomsCatalogService.searchByKeywordAndStatus("", null, pageable);
        assertEquals(1, result.getContent().size());
    }

    @Test
    @DisplayName("searchByKeywordAndStatus - Valid keyword filters list with sublist pagination")
    void searchByKeywordAndStatus_WithKeyword() {
        Pageable pageable = PageRequest.of(0, 2);

        SymptomsCatalog s1 = new SymptomsCatalog();
        s1.setSymptomId("SYM0001");
        s1.setSymptomName("Khát nước");
        s1.setStatus(true);

        SymptomsCatalog s2 = new SymptomsCatalog();
        s2.setSymptomId("SYM0002");
        s2.setSymptomName("Mệt mỏi");
        s2.setStatus(true);

        SymptomsCatalog s3 = new SymptomsCatalog();
        s3.setSymptomId("SYM0003");
        s3.setSymptomName("Khát nước nhiều");
        s3.setStatus(false);

        when(repository.findAll()).thenReturn(List.of(s1, s2, s3));

        // Filter keyword "Khát" with status true
        Page<SymptomsCatalog> page = symptomsCatalogService.searchByKeywordAndStatus("Khát", true, pageable);
        assertEquals(1, page.getContent().size());
        assertEquals("SYM0001", page.getContent().get(0).getSymptomId());

        // Test offset out of bounds
        Pageable outOfBounds = PageRequest.of(5, 2);
        Page<SymptomsCatalog> emptyPage = symptomsCatalogService.searchByKeywordAndStatus("Khát", null, outOfBounds);
        assertTrue(emptyPage.getContent().isEmpty());
    }

    @Test
    @DisplayName("findById - Should return Optional symptom")
    void findById_Success() {
        SymptomsCatalog s = new SymptomsCatalog();
        when(repository.findById("SYM0001")).thenReturn(Optional.of(s));

        Optional<SymptomsCatalog> result = symptomsCatalogService.findById("SYM0001");
        assertTrue(result.isPresent());
    }

    @Test
    @DisplayName("create - Invalid name or duplicate name should throw IllegalArgumentException")
    void create_ValidationErrors() {
        // Null name
        SymptomsCatalog sNull = new SymptomsCatalog();
        assertThrows(IllegalArgumentException.class, () -> symptomsCatalogService.create(sNull));

        // Special characters name
        SymptomsCatalog sSpecial = new SymptomsCatalog();
        sSpecial.setSymptomName("Triệu chứng <invalid>");
        assertThrows(IllegalArgumentException.class, () -> symptomsCatalogService.create(sSpecial));

        // Duplicate name
        SymptomsCatalog sDup = new SymptomsCatalog();
        sDup.setSymptomName("Khát nước");
        when(repository.existsById(anyString())).thenReturn(false);
        when(repository.existsBySymptomNameIgnoreCase("Khát nước")).thenReturn(true);
        assertThrows(IllegalArgumentException.class, () -> symptomsCatalogService.create(sDup));
    }

    @Test
    @DisplayName("create - Valid symptom should set ID and default status true")
    void create_Success() {
        SymptomsCatalog s = new SymptomsCatalog();
        s.setSymptomName("Tiểu nhiều");
        s.setStatus(null);

        when(repository.existsById(anyString())).thenReturn(false);
        when(repository.existsBySymptomNameIgnoreCase("Tiểu nhiều")).thenReturn(false);
        when(repository.save(any(SymptomsCatalog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SymptomsCatalog created = symptomsCatalogService.create(s);
        assertNotNull(created.getSymptomId());
        assertTrue(created.getSymptomId().startsWith("SYM"));
        assertTrue(created.getStatus());
    }

    @Test
    @DisplayName("update - Not found should throw EntityNotFoundException")
    void update_NotFound() {
        SymptomsCatalog s = new SymptomsCatalog();
        s.setSymptomName("Mắt mờ");
        when(repository.findById("SYM9999")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> symptomsCatalogService.update("SYM9999", s));
    }

    @Test
    @DisplayName("update - Duplicate name should throw IllegalArgumentException")
    void update_DuplicateName() {
        SymptomsCatalog existing = new SymptomsCatalog();
        existing.setSymptomId("SYM0001");
        existing.setSymptomName("Khát nước");

        SymptomsCatalog updateDto = new SymptomsCatalog();
        updateDto.setSymptomName("Tiểu nhiều");

        when(repository.findById("SYM0001")).thenReturn(Optional.of(existing));
        when(repository.existsBySymptomNameIgnoreCase("Tiểu nhiều")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> symptomsCatalogService.update("SYM0001", updateDto));
    }

    @Test
    @DisplayName("update - Valid update should save new name")
    void update_Success() {
        SymptomsCatalog existing = new SymptomsCatalog();
        existing.setSymptomId("SYM0001");
        existing.setSymptomName("Khát nước");

        SymptomsCatalog updateDto = new SymptomsCatalog();
        updateDto.setSymptomName("Tiểu đêm");

        when(repository.findById("SYM0001")).thenReturn(Optional.of(existing));
        when(repository.existsBySymptomNameIgnoreCase("Tiểu đêm")).thenReturn(false);
        when(repository.save(any(SymptomsCatalog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SymptomsCatalog updated = symptomsCatalogService.update("SYM0001", updateDto);
        assertEquals("Tiểu đêm", updated.getSymptomName());
    }

    @Test
    @DisplayName("softDelete - Should set status false")
    void softDelete_Success() {
        SymptomsCatalog s = new SymptomsCatalog();
        s.setStatus(true);
        when(repository.findById("SYM0001")).thenReturn(Optional.of(s));

        symptomsCatalogService.softDelete("SYM0001");
        assertFalse(s.getStatus());
        verify(repository, times(1)).save(s);
    }

    @Test
    @DisplayName("restore - Should set status true")
    void restore_Success() {
        SymptomsCatalog s = new SymptomsCatalog();
        s.setStatus(false);
        when(repository.findById("SYM0001")).thenReturn(Optional.of(s));

        symptomsCatalogService.restore("SYM0001");
        assertTrue(s.getStatus());
        verify(repository, times(1)).save(s);
    }

    @Test
    @DisplayName("delete - Should remove entity")
    void delete_Success() {
        SymptomsCatalog s = new SymptomsCatalog();
        when(repository.findById("SYM0001")).thenReturn(Optional.of(s));

        symptomsCatalogService.delete("SYM0001");
        verify(repository, times(1)).delete(s);
    }

    @Test
    @DisplayName("getSummaryStats - Should compute and return stats map")
    void getSummaryStats_Success() {
        when(repository.count()).thenReturn(10L);
        when(repository.findByStatus(eq(true), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(new SymptomsCatalog(), new SymptomsCatalog())));
        when(repository.findByStatus(eq(false), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(new SymptomsCatalog())));

        Map<String, Object> stats = symptomsCatalogService.getSummaryStats();
        assertEquals(10L, stats.get("totalSymptoms"));
        assertEquals(2L, stats.get("activeSymptoms"));
        assertEquals(1L, stats.get("clockedSymptoms"));
    }
}
