package com.quan.diabetes.service.user;

import com.quan.diabetes.entity.Patient;
import com.quan.diabetes.repository.PatientRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {PatientServiceImpl.class})
class PatientServiceImplTest {

    @MockitoBean
    private PatientRepository patientRepository;

    @Autowired
    private PatientServiceImpl patientService;

    private Patient samplePatient;

    @BeforeEach
    void setUp() {
        samplePatient = new Patient();
        samplePatient.setUserId("PAT001");
        samplePatient.setFullName("Nguyen Van A");
        samplePatient.setPhoneNumber("0987654321");
    }

    @Test
    void testFindAll() {
        when(patientRepository.findAll()).thenReturn(Collections.singletonList(samplePatient));

        List<Patient> result = patientService.findAll();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("PAT001", result.get(0).getUserId());
        verify(patientRepository, times(1)).findAll();
    }

    @Test
    void testFindById_Found() {
        when(patientRepository.findById("PAT001")).thenReturn(Optional.of(samplePatient));

        Optional<Patient> result = patientService.findById("PAT001");

        assertTrue(result.isPresent());
        assertEquals("PAT001", result.get().getUserId());
        verify(patientRepository, times(1)).findById("PAT001");
    }

    @Test
    void testFindById_NotFound() {
        when(patientRepository.findById("UNKNOWN")).thenReturn(Optional.empty());

        Optional<Patient> result = patientService.findById("UNKNOWN");

        assertFalse(result.isPresent());
        verify(patientRepository, times(1)).findById("UNKNOWN");
    }

    @Test
    void testCreate() {
        when(patientRepository.save(any(Patient.class))).thenReturn(samplePatient);

        Patient created = patientService.create(samplePatient);

        assertNotNull(created);
        assertEquals("PAT001", created.getUserId());
        verify(patientRepository, times(1)).save(samplePatient);
    }

    @Test
    void testUpdate_Success() {
        when(patientRepository.existsById("PAT001")).thenReturn(true);
        when(patientRepository.save(any(Patient.class))).thenReturn(samplePatient);

        Patient updated = patientService.update("PAT001", samplePatient);

        assertNotNull(updated);
        assertEquals("PAT001", updated.getUserId());
        verify(patientRepository, times(1)).existsById("PAT001");
        verify(patientRepository, times(1)).save(samplePatient);
    }

    @Test
    void testUpdate_NotFound() {
        when(patientRepository.existsById("UNKNOWN")).thenReturn(false);

        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class, () -> {
            patientService.update("UNKNOWN", samplePatient);
        });

        assertEquals("Patient not found with id: UNKNOWN", ex.getMessage());
        verify(patientRepository, never()).save(any(Patient.class));
    }

    @Test
    void testExistsById() {
        when(patientRepository.existsById("PAT001")).thenReturn(true);
        when(patientRepository.existsById("UNKNOWN")).thenReturn(false);

        assertTrue(patientService.existsById("PAT001"));
        assertFalse(patientService.existsById("UNKNOWN"));
    }
}
