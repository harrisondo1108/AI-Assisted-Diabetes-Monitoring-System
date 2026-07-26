package com.quan.diabetes.service.user;

import com.quan.diabetes.entity.Patient;
import com.quan.diabetes.entity.PatientRoutine;
import com.quan.diabetes.repository.PatientRepository;
import com.quan.diabetes.repository.PatientRoutineRepository;
import com.quan.diabetes.service.reminder.MedicationRescheduleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {PatientRoutineServiceImpl.class})
class PatientRoutineServiceImplTest {

    @MockitoBean
    private PatientRoutineRepository patientRoutineRepository;

    @MockitoBean
    private PatientRepository patientRepository;

    @MockitoBean
    private MedicationRescheduleService medicationRescheduleService;

    @Autowired
    private PatientRoutineServiceImpl patientRoutineService;

    private PatientRoutine sampleRoutine;
    private Patient samplePatient;

    @BeforeEach
    void setUp() {
        samplePatient = new Patient();
        samplePatient.setUserId("PAT001");
        samplePatient.setFullName("Nguyen Van A");

        sampleRoutine = new PatientRoutine();
        sampleRoutine.setUserId("PAT001");
        sampleRoutine.setWakeUpTime(java.time.LocalTime.of(6, 0));
        sampleRoutine.setSleepTime(java.time.LocalTime.of(22, 0));
    }

    @Test
    void testFindById_Found() {
        when(patientRoutineRepository.findById("PAT001")).thenReturn(Optional.of(sampleRoutine));

        Optional<PatientRoutine> result = patientRoutineService.findById("PAT001");

        assertTrue(result.isPresent());
        assertEquals("PAT001", result.get().getUserId());
        verify(patientRoutineRepository, times(1)).findById("PAT001");
    }

    @Test
    void testFindById_NotFound() {
        when(patientRoutineRepository.findById("UNKNOWN")).thenReturn(Optional.empty());

        Optional<PatientRoutine> result = patientRoutineService.findById("UNKNOWN");

        assertFalse(result.isPresent());
    }

    @Test
    void testCreate_NullEntity() {
        RuntimeException ex = assertThrows(RuntimeException.class, () -> patientRoutineService.create(null));
        assertEquals("PatientRoutine UserID must not be null.", ex.getMessage());
    }

    @Test
    void testCreate_NullUserId() {
        sampleRoutine.setUserId(null);
        RuntimeException ex = assertThrows(RuntimeException.class, () -> patientRoutineService.create(sampleRoutine));
        assertEquals("PatientRoutine UserID must not be null.", ex.getMessage());
    }

    @Test
    void testCreate_BlankUserId() {
        sampleRoutine.setUserId("   ");
        RuntimeException ex = assertThrows(RuntimeException.class, () -> patientRoutineService.create(sampleRoutine));
        assertEquals("PatientRoutine UserID must not be null.", ex.getMessage());
    }

    @Test
    void testCreate_PatientNotFound() {
        when(patientRepository.existsById("PAT001")).thenReturn(false);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> patientRoutineService.create(sampleRoutine));
        assertEquals("Patient not found with id: PAT001", ex.getMessage());
        verify(patientRoutineRepository, never()).save(any());
    }

    @Test
    void testCreate_Success() {
        when(patientRepository.existsById("PAT001")).thenReturn(true);
        when(patientRoutineRepository.save(any(PatientRoutine.class))).thenReturn(sampleRoutine);

        PatientRoutine created = patientRoutineService.create(sampleRoutine);

        assertNotNull(created);
        assertEquals("PAT001", created.getUserId());
        verify(patientRoutineRepository, times(1)).save(sampleRoutine);
    }

    @Test
    void testUpdate_NullId() {
        RuntimeException ex = assertThrows(RuntimeException.class, () -> patientRoutineService.update(null, sampleRoutine));
        assertEquals("PatientRoutine id must not be null.", ex.getMessage());
    }

    @Test
    void testUpdate_BlankId() {
        RuntimeException ex = assertThrows(RuntimeException.class, () -> patientRoutineService.update("", sampleRoutine));
        assertEquals("PatientRoutine id must not be null.", ex.getMessage());
    }

    @Test
    void testUpdate_RoutineNotFound() {
        when(patientRoutineRepository.existsById("PAT001")).thenReturn(false);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> patientRoutineService.update("PAT001", sampleRoutine));
        assertEquals("PatientRoutine not found with id: PAT001", ex.getMessage());
    }

    @Test
    void testUpdate_PatientNotFound() {
        when(patientRoutineRepository.existsById("PAT001")).thenReturn(true);
        when(patientRepository.findById("PAT001")).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> patientRoutineService.update("PAT001", sampleRoutine));
        assertEquals("Patient not found with id: PAT001", ex.getMessage());
        verify(patientRoutineRepository, never()).save(any());
    }

    @Test
    void testUpdate_Success() {
        when(patientRoutineRepository.existsById("PAT001")).thenReturn(true);
        when(patientRepository.findById("PAT001")).thenReturn(Optional.of(samplePatient));
        when(patientRoutineRepository.save(any(PatientRoutine.class))).thenReturn(sampleRoutine);
        doNothing().when(medicationRescheduleService).rescheduleFutureMedicationReminders(eq("PAT001"), any(PatientRoutine.class));

        PatientRoutine updated = patientRoutineService.update("PAT001", sampleRoutine);

        assertNotNull(updated);
        assertEquals("PAT001", updated.getUserId());
        assertEquals(samplePatient, updated.getPatient());
        verify(patientRoutineRepository, times(1)).save(sampleRoutine);
        verify(medicationRescheduleService, times(1)).rescheduleFutureMedicationReminders("PAT001", sampleRoutine);
    }

    @Test
    void testDeleteById() {
        doNothing().when(patientRoutineRepository).deleteById("PAT001");

        assertDoesNotThrow(() -> patientRoutineService.deleteById("PAT001"));

        verify(patientRoutineRepository, times(1)).deleteById("PAT001");
    }

    @Test
    void testExistsById() {
        when(patientRoutineRepository.existsById("PAT001")).thenReturn(true);
        when(patientRoutineRepository.existsById("UNKNOWN")).thenReturn(false);

        assertTrue(patientRoutineService.existsById("PAT001"));
        assertFalse(patientRoutineService.existsById("UNKNOWN"));
    }
}
