package com.quan.diabetes.service.masterdata;

import com.quan.diabetes.entity.PatientType;
import com.quan.diabetes.repository.PatientTypeRepository;
import com.quan.diabetes.service.masterdata.impl.PatientTypeServiceImpl;
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
class PatientTypeServiceImplTest {

    @Mock
    private PatientTypeRepository patientTypeRepository;

    @InjectMocks
    private PatientTypeServiceImpl patientTypeService;

    @Test
    void testFindAll() {
        PatientType p1 = new PatientType();
        p1.setPatientTypeId(1);
        PatientType p2 = new PatientType();
        p2.setPatientTypeId(2);
        List<PatientType> mockList = Arrays.asList(p1, p2);

        when(patientTypeRepository.findAll()).thenReturn(mockList);

        List<PatientType> result = patientTypeService.findAll();

        assertEquals(2, result.size());
        assertEquals(1, result.get(0).getPatientTypeId());
        assertEquals(2, result.get(1).getPatientTypeId());
        verify(patientTypeRepository, times(1)).findAll();
    }

    @Test
    void testFindById_Found() {
        PatientType p = new PatientType();
        p.setPatientTypeId(1);

        when(patientTypeRepository.findById(1)).thenReturn(Optional.of(p));

        Optional<PatientType> result = patientTypeService.findById(1);

        assertTrue(result.isPresent());
        assertEquals(1, result.get().getPatientTypeId());
        verify(patientTypeRepository, times(1)).findById(1);
    }

    @Test
    void testFindById_NotFound() {
        when(patientTypeRepository.findById(999)).thenReturn(Optional.empty());

        Optional<PatientType> result = patientTypeService.findById(999);

        assertFalse(result.isPresent());
        verify(patientTypeRepository, times(1)).findById(999);
    }

    @Test
    void testCreate() {
        PatientType input = new PatientType();
        input.setTypeName("Type 1 Diabetes");

        PatientType saved = new PatientType();
        saved.setPatientTypeId(1);
        saved.setTypeName("Type 1 Diabetes");

        when(patientTypeRepository.save(input)).thenReturn(saved);

        PatientType result = patientTypeService.create(input);

        assertNotNull(result);
        assertEquals(1, result.getPatientTypeId());
        verify(patientTypeRepository, times(1)).save(input);
    }

    @Test
    void testUpdate_Success() {
        Integer id = 1;
        PatientType input = new PatientType();
        input.setTypeName("Updated Type");

        PatientType saved = new PatientType();
        saved.setPatientTypeId(id);
        saved.setTypeName("Updated Type");

        when(patientTypeRepository.existsById(id)).thenReturn(true);
        when(patientTypeRepository.save(input)).thenReturn(saved);

        PatientType result = patientTypeService.update(id, input);

        assertNotNull(result);
        assertEquals(id, input.getPatientTypeId());
        assertEquals("Updated Type", result.getTypeName());
        verify(patientTypeRepository, times(1)).existsById(id);
        verify(patientTypeRepository, times(1)).save(input);
    }

    @Test
    void testUpdate_NotFound() {
        Integer id = 999;
        PatientType input = new PatientType();

        when(patientTypeRepository.existsById(id)).thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> patientTypeService.update(id, input));
        assertEquals("PatientType not found with id: 999", exception.getMessage());
        verify(patientTypeRepository, times(1)).existsById(id);
        verify(patientTypeRepository, never()).save(any());
    }

    @Test
    void testDeleteById() {
        Integer id = 1;

        patientTypeService.deleteById(id);

        verify(patientTypeRepository, times(1)).deleteById(id);
    }

    @Test
    void testExistsById() {
        when(patientTypeRepository.existsById(1)).thenReturn(true);

        assertTrue(patientTypeService.existsById(1));
        verify(patientTypeRepository, times(1)).existsById(1);
    }
}
