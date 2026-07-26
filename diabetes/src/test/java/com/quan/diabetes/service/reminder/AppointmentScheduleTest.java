package com.quan.diabetes.service.reminder;

import com.quan.diabetes.entity.ClinicalExamination;
import com.quan.diabetes.entity.Patient;
import com.quan.diabetes.entity.Profile;
import com.quan.diabetes.entity.Reminder;
import com.quan.diabetes.entity.User;
import com.quan.diabetes.repository.ClinicalExaminationRepository;
import com.quan.diabetes.repository.ReminderRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppointmentScheduleTest {

    @Mock
    private ReminderRepository reminderRepo;

    @Mock
    private ClinicalExaminationRepository clinicalExaminationRepo;

    @InjectMocks
    private AppointmentSchedule appointmentSchedule;

    @Test
    @DisplayName("Should return immediately when ClinicalExamination is not found")
    void generateAppointmentReminder_ExamNotFound() {
        when(clinicalExaminationRepo.findById("exam1")).thenReturn(Optional.empty());

        appointmentSchedule.generateAppointmentReminder("exam1");

        verify(reminderRepo, never()).findByPatient_UserIdAndTitle(any(), any());
        verify(reminderRepo, never()).save(any());
    }

    @Test
    @DisplayName("Should return immediately when Patient in ClinicalExamination is null")
    void generateAppointmentReminder_PatientNull() {
        ClinicalExamination exam = new ClinicalExamination();
        exam.setPatient(null);
        when(clinicalExaminationRepo.findById("exam1")).thenReturn(Optional.of(exam));

        appointmentSchedule.generateAppointmentReminder("exam1");

        verify(reminderRepo, never()).findByPatient_UserIdAndTitle(any(), any());
        verify(reminderRepo, never()).save(any());
    }

    @Test
    @DisplayName("Should lock existing reminders and skip creating new reminder if nextAppointment is null")
    void generateAppointmentReminder_NoNextAppointment_LocksExistingReminders() {
        Patient patient = new Patient();
        patient.setUserId("patient1");

        ClinicalExamination exam = new ClinicalExamination();
        exam.setPatient(patient);
        exam.setNextAppointment(null);

        Reminder existing1 = new Reminder();
        existing1.setLockStatus(false);
        Reminder existing2 = new Reminder();
        existing2.setLockStatus(false);
        List<Reminder> existingList = List.of(existing1, existing2);

        when(clinicalExaminationRepo.findById("exam1")).thenReturn(Optional.of(exam));
        when(reminderRepo.findByPatient_UserIdAndTitle("patient1", AppointmentSchedule.APPOINTMENT_REMINDER_TITLE))
                .thenReturn(existingList);

        appointmentSchedule.generateAppointmentReminder("exam1");

        assertTrue(existing1.getLockStatus());
        assertTrue(existing2.getLockStatus());
        verify(reminderRepo).saveAll(existingList);
        verify(reminderRepo, never()).save(any());
    }

    @Test
    @DisplayName("Should generate reminders when doctor profile has full name")
    void generateAppointmentReminder_NextAppointment_WithDoctorFullName() {
        Patient patient = new Patient();
        patient.setUserId("patient1");

        User doctor = new User();
        doctor.setUserId("doc1");
        Profile profile = new Profile();
        profile.setFullName("Trần Văn Bác Sĩ");
        doctor.setProfile(profile);

        ClinicalExamination exam = new ClinicalExamination();
        exam.setPatient(patient);
        exam.setDoctor(doctor);
        LocalDateTime nextAppt = LocalDateTime.of(2026, 8, 1, 9, 30);
        exam.setNextAppointment(nextAppt);

        when(clinicalExaminationRepo.findById("exam1")).thenReturn(Optional.of(exam));
        when(reminderRepo.findByPatient_UserIdAndTitle("patient1", AppointmentSchedule.APPOINTMENT_REMINDER_TITLE))
                .thenReturn(null);

        appointmentSchedule.generateAppointmentReminder("exam1");

        verify(reminderRepo, never()).saveAll(any());

        ArgumentCaptor<Reminder> reminderCaptor = ArgumentCaptor.forClass(Reminder.class);
        verify(reminderRepo, times(2)).save(reminderCaptor.capture());

        List<Reminder> savedReminders = reminderCaptor.getAllValues();
        assertEquals(2, savedReminders.size());

        Reminder dayBefore = savedReminders.get(0);
        assertEquals(AppointmentSchedule.APPOINTMENT_REMINDER_TITLE, dayBefore.getTitle());
        assertTrue(dayBefore.getMessage().contains("BS. Trần Văn Bác Sĩ"));
        assertEquals(LocalDateTime.of(2026, 7, 31, 7, 0, 0, 0), dayBefore.getScheduledTime());
        assertFalse(dayBefore.getLockStatus());
        assertFalse(dayBefore.getIsRead());

        Reminder onDay = savedReminders.get(1);
        assertEquals(AppointmentSchedule.APPOINTMENT_REMINDER_TITLE, onDay.getTitle());
        assertTrue(onDay.getMessage().contains("BS. Trần Văn Bác Sĩ"));
        assertEquals(LocalDateTime.of(2026, 8, 1, 7, 0, 0, 0), onDay.getScheduledTime());
        assertFalse(onDay.getLockStatus());
        assertFalse(onDay.getIsRead());
    }

    @Test
    @DisplayName("Should fallback doctor name to doctor userId when profile is null or fullName is null")
    void generateAppointmentReminder_NextAppointment_WithDoctorUserIdFallback() {
        Patient patient = new Patient();
        patient.setUserId("patient1");

        // Case A: Doctor profile is null
        User doctor1 = new User();
        doctor1.setUserId("DOC_USER_123");
        doctor1.setProfile(null);

        ClinicalExamination exam1 = new ClinicalExamination();
        exam1.setPatient(patient);
        exam1.setDoctor(doctor1);
        exam1.setNextAppointment(LocalDateTime.of(2026, 8, 1, 9, 30));

        when(clinicalExaminationRepo.findById("exam1")).thenReturn(Optional.of(exam1));
        when(reminderRepo.findByPatient_UserIdAndTitle(any(), any())).thenReturn(Collections.emptyList());

        appointmentSchedule.generateAppointmentReminder("exam1");

        // Case B: Doctor profile is not null, but fullName is null
        User doctor2 = new User();
        doctor2.setUserId("DOC_USER_456");
        Profile emptyProfile = new Profile();
        emptyProfile.setFullName(null);
        doctor2.setProfile(emptyProfile);

        ClinicalExamination exam2 = new ClinicalExamination();
        exam2.setPatient(patient);
        exam2.setDoctor(doctor2);
        exam2.setNextAppointment(LocalDateTime.of(2026, 8, 1, 9, 30));

        when(clinicalExaminationRepo.findById("exam2")).thenReturn(Optional.of(exam2));

        appointmentSchedule.generateAppointmentReminder("exam2");

        ArgumentCaptor<Reminder> reminderCaptor = ArgumentCaptor.forClass(Reminder.class);
        verify(reminderRepo, times(4)).save(reminderCaptor.capture());

        List<Reminder> savedReminders = reminderCaptor.getAllValues();
        assertTrue(savedReminders.get(0).getMessage().contains("BS. DOC_USER_123"));
        assertTrue(savedReminders.get(2).getMessage().contains("BS. DOC_USER_456"));
    }

    @Test
    @DisplayName("Should fallback doctor name to 'bác sĩ' when doctor is null")
    void generateAppointmentReminder_NextAppointment_WithDoctorNull() {
        Patient patient = new Patient();
        patient.setUserId("patient1");

        ClinicalExamination exam = new ClinicalExamination();
        exam.setPatient(patient);
        exam.setDoctor(null);
        exam.setNextAppointment(LocalDateTime.of(2026, 8, 1, 9, 30));

        when(clinicalExaminationRepo.findById("exam1")).thenReturn(Optional.of(exam));
        when(reminderRepo.findByPatient_UserIdAndTitle(any(), any())).thenReturn(Collections.emptyList());

        appointmentSchedule.generateAppointmentReminder("exam1");

        ArgumentCaptor<Reminder> reminderCaptor = ArgumentCaptor.forClass(Reminder.class);
        verify(reminderRepo, times(2)).save(reminderCaptor.capture());

        List<Reminder> savedReminders = reminderCaptor.getAllValues();
        assertTrue(savedReminders.get(0).getMessage().contains("BS. bác sĩ"));
    }
}
