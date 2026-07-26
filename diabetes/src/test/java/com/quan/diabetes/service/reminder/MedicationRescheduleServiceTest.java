package com.quan.diabetes.service.reminder;

import com.quan.diabetes.entity.MedicationTiming;
import com.quan.diabetes.entity.Patient;
import com.quan.diabetes.entity.PatientRoutine;
import com.quan.diabetes.entity.Reminder;
import com.quan.diabetes.repository.ReminderRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MedicationRescheduleServiceTest {

    @Mock
    private ReminderRepository reminderRepository;

    @InjectMocks
    private MedicationRescheduleService rescheduleService;

    @Test
    @DisplayName("Should throw IllegalArgumentException when patientId does not match newRoutine patient")
    void rescheduleFutureMedicationReminders_PatientIdMismatch() {
        Patient patient = new Patient();
        patient.setUserId("patientA");

        PatientRoutine newRoutine = new PatientRoutine();
        newRoutine.setPatient(patient);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> rescheduleService.rescheduleFutureMedicationReminders("patientB", newRoutine));

        assertEquals("patientId does not match the patient in the new routine", ex.getMessage());
        verify(reminderRepository, never()).findByPatient_UserIdAndScheduledTimeGreaterThanEqualAndTitleOrderByScheduledTimeAsc(any(), any(), any());
    }

    @Test
    @DisplayName("Should return early when no future reminders exist")
    void rescheduleFutureMedicationReminders_NoFutureReminders() {
        Patient patient = new Patient();
        patient.setUserId("patientA");

        PatientRoutine newRoutine = new PatientRoutine();
        newRoutine.setPatient(patient);

        when(reminderRepository.findByPatient_UserIdAndScheduledTimeGreaterThanEqualAndTitleOrderByScheduledTimeAsc(
                eq("patientA"), any(LocalDateTime.class), eq(MedicationSchedualeService.MEDICATION_REMINDER_TITLE)))
                .thenReturn(Collections.emptyList());

        rescheduleService.rescheduleFutureMedicationReminders("patientA", newRoutine);

        verify(reminderRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should process future reminders and skip invalid timing objects")
    void rescheduleFutureMedicationReminders_ProcessReminders() {
        Patient patient = new Patient();
        patient.setUserId("patientA");

        PatientRoutine newRoutine = new PatientRoutine();
        newRoutine.setPatient(patient);
        newRoutine.setBreakfastTime(LocalTime.of(8, 0));

        // Reminder 1: timing is null
        Reminder r1 = new Reminder();
        r1.setTiming(null);
        r1.setScheduledTime(LocalDateTime.of(2026, 8, 1, 7, 0));

        // Reminder 2: timing is non-null but timingName is null
        Reminder r2 = new Reminder();
        MedicationTiming timing2 = new MedicationTiming();
        timing2.setTimingName(null);
        r2.setTiming(timing2);
        r2.setScheduledTime(LocalDateTime.of(2026, 8, 1, 7, 0));

        // Reminder 3: valid timing and timingName
        Reminder r3 = new Reminder();
        MedicationTiming timing3 = new MedicationTiming();
        timing3.setTimingName("trước sáng");
        r3.setTiming(timing3);
        r3.setScheduledTime(LocalDateTime.of(2026, 8, 1, 7, 0));

        List<Reminder> reminders = List.of(r1, r2, r3);

        when(reminderRepository.findByPatient_UserIdAndScheduledTimeGreaterThanEqualAndTitleOrderByScheduledTimeAsc(
                eq("patientA"), any(LocalDateTime.class), eq(MedicationSchedualeService.MEDICATION_REMINDER_TITLE)))
                .thenReturn(reminders);

        rescheduleService.rescheduleFutureMedicationReminders("patientA", newRoutine);

        // Only r3 should be saved with updated time (07:30 = 08:00 - 30m)
        assertEquals(LocalDateTime.of(2026, 8, 1, 7, 30), r3.getScheduledTime());
        verify(reminderRepository, times(1)).save(r3);
        verify(reminderRepository, never()).save(r1);
        verify(reminderRepository, never()).save(r2);
    }
}
