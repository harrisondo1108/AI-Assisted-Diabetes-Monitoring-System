package com.quan.diabetes.service.reminder;

import com.quan.diabetes.entity.Patient;
import com.quan.diabetes.entity.Reminder;
import com.quan.diabetes.repository.ReminderRepository;
import com.quan.diabetes.service.notification.EmailService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReminderScheduledTaskTest {

    @Mock
    private ReminderRepository reminderRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private ReminderScheduledTask scheduledTask;

    @Test
    @DisplayName("Should process empty due reminders list")
    void scanDueReminders_EmptyList() {
        when(reminderRepository.findDueUnsentReminders(any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());

        scheduledTask.scanDueReminders();

        verify(emailService, never()).sendSimpleEmail(any(), any(), any());
        verify(reminderRepository).saveAll(Collections.emptyList());
    }

    @Test
    @DisplayName("Should send email to patient when patient email exists and is non-empty")
    void scanDueReminders_WithValidPatientEmail() {
        Patient patient = new Patient();
        patient.setEmail("  patient@example.com  ");

        Reminder r = new Reminder();
        r.setReminderId(101L);
        r.setTitle("Tiêu đề 1");
        r.setMessage("Nội dung 1");
        r.setPatient(patient);
        r.setIsSent(false);

        List<Reminder> reminders = List.of(r);

        when(reminderRepository.findDueUnsentReminders(any(LocalDateTime.class)))
                .thenReturn(reminders);

        scheduledTask.scanDueReminders();

        verify(emailService).sendSimpleEmail("patient@example.com", "Tiêu đề 1", "Nội dung 1");
        assertTrue(r.getIsSent());
        verify(reminderRepository).saveAll(reminders);
    }

    @Test
    @DisplayName("Should fallback to default admin email when patient is null")
    void scanDueReminders_WithNullPatient() {
        Reminder r = new Reminder();
        r.setReminderId(102L);
        r.setTitle("Tiêu đề 2");
        r.setMessage("Nội dung 2");
        r.setPatient(null);
        r.setIsSent(false);

        List<Reminder> reminders = List.of(r);

        when(reminderRepository.findDueUnsentReminders(any(LocalDateTime.class)))
                .thenReturn(reminders);

        scheduledTask.scanDueReminders();

        verify(emailService).sendSimpleEmail("lequan13112005@gmail.com", "Tiêu đề 2", "Nội dung 2");
        assertTrue(r.getIsSent());
        verify(reminderRepository).saveAll(reminders);
    }

    @Test
    @DisplayName("Should fallback to default admin email when patient email is null or blank")
    void scanDueReminders_WithNullOrBlankPatientEmail() {
        // Case A: Patient email is null
        Patient p1 = new Patient();
        p1.setEmail(null);
        p1.setPhoneNumber("0987654321");
        Reminder r1 = new Reminder();
        r1.setReminderId(201L);
        r1.setTitle("Title A");
        r1.setMessage("Msg A");
        r1.setPatient(p1);

        // Case B: Patient email is blank whitespace
        Patient p2 = new Patient();
        p2.setEmail("   ");
        p2.setPhoneNumber("0123456789");
        Reminder r2 = new Reminder();
        r2.setReminderId(202L);
        r2.setTitle("Title B");
        r2.setMessage("Msg B");
        r2.setPatient(p2);

        List<Reminder> reminders = List.of(r1, r2);

        when(reminderRepository.findDueUnsentReminders(any(LocalDateTime.class)))
                .thenReturn(reminders);

        scheduledTask.scanDueReminders();

        verify(emailService).sendSimpleEmail("lequan13112005@gmail.com", "Title A", "Msg A");
        verify(emailService).sendSimpleEmail("lequan13112005@gmail.com", "Title B", "Msg B");
        assertTrue(r1.getIsSent());
        assertTrue(r2.getIsSent());
        verify(reminderRepository).saveAll(reminders);
    }
}
