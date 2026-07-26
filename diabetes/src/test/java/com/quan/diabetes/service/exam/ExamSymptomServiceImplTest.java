package com.quan.diabetes.service.exam;

import com.quan.diabetes.entity.ExamSymptom;
import com.quan.diabetes.entity.ExamSymptomId;
import com.quan.diabetes.repository.ExamSymptomRepository;
import com.quan.diabetes.service.exam.impl.ExamSymptomServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
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
class ExamSymptomServiceImplTest {

    @Mock
    private ExamSymptomRepository examSymptomRepository;

    @InjectMocks
    private ExamSymptomServiceImpl examSymptomService;

    @Test
    @DisplayName("findAll - Should return list of all ExamSymptoms")
    void findAll_Success() {
        ExamSymptom es = new ExamSymptom();
        when(examSymptomRepository.findAll()).thenReturn(List.of(es));

        List<ExamSymptom> result = examSymptomService.findAll();
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("findById - Should return Optional ExamSymptom")
    void findById_Success() {
        ExamSymptomId id = new ExamSymptomId("EX01", "SYM01");
        ExamSymptom es = new ExamSymptom();
        when(examSymptomRepository.findById(id)).thenReturn(Optional.of(es));

        Optional<ExamSymptom> result = examSymptomService.findById(id);
        assertTrue(result.isPresent());
        assertEquals(es, result.get());
    }

    @Test
    @DisplayName("create - Should save and return entity")
    void create_Success() {
        ExamSymptom es = new ExamSymptom();
        when(examSymptomRepository.save(es)).thenReturn(es);

        ExamSymptom result = examSymptomService.create(es);
        assertEquals(es, result);
    }

    @Test
    @DisplayName("update - Should save when exists, throw EntityNotFoundException when not")
    void update_Test() {
        ExamSymptomId id = new ExamSymptomId("EX01", "SYM01");
        ExamSymptom es = new ExamSymptom();

        when(examSymptomRepository.existsById(id)).thenReturn(true);
        when(examSymptomRepository.save(es)).thenReturn(es);
        ExamSymptom updated = examSymptomService.update(id, es);
        assertEquals(es, updated);

        when(examSymptomRepository.existsById(id)).thenReturn(false);
        assertThrows(EntityNotFoundException.class, () -> examSymptomService.update(id, es));
    }

    @Test
    @DisplayName("deleteById - Should delete when exists, throw EntityNotFoundException when not")
    void deleteById_Test() {
        ExamSymptomId id = new ExamSymptomId("EX01", "SYM01");

        when(examSymptomRepository.existsById(id)).thenReturn(true);
        examSymptomService.deleteById(id);
        verify(examSymptomRepository, times(1)).deleteById(id);

        when(examSymptomRepository.existsById(id)).thenReturn(false);
        assertThrows(EntityNotFoundException.class, () -> examSymptomService.deleteById(id));
    }

    @Test
    @DisplayName("existsById - Should return repository result")
    void existsById_Success() {
        ExamSymptomId id = new ExamSymptomId("EX01", "SYM01");
        when(examSymptomRepository.existsById(id)).thenReturn(true);

        assertTrue(examSymptomService.existsById(id));
    }
}
