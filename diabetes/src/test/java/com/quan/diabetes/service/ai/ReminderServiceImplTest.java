package com.quan.diabetes.service.ai;

import com.quan.diabetes.entity.Patient;
import com.quan.diabetes.entity.Reminder;
import com.quan.diabetes.repository.ReminderRepository;
import com.quan.diabetes.service.ai.impl.ReminderServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReminderServiceImplTest {

    @Mock
    private ReminderRepository repository;

    @InjectMocks
    private ReminderServiceImpl service;

    private Reminder reminder;

    @BeforeEach
    void setUp() {
        reminder = new Reminder();
        reminder.setReminderId(1L);
        reminder.setTitle("Uong thuoc");
    }

    @Test
    void testGetListByIdAndScheduledTimeLessThanEqual() {
        LocalDateTime time = LocalDateTime.now();
        when(repository.findByPatient_UserIdAndScheduledTimeLessThanEqualOrderByScheduledTimeDesc("PAT-01", time)).thenReturn(List.of(reminder));

        List<Reminder> result = service.getListByIdAndScheduledTimeLessThanEqual("PAT-01", time);
        assertEquals(1, result.size());
    }

    @Test
    void testFindAll() {
        when(repository.findAll()).thenReturn(List.of(reminder));
        List<Reminder> result = service.findAll();
        assertEquals(1, result.size());
    }

    @Test
    void testFindById() {
        when(repository.findById(1L)).thenReturn(Optional.of(reminder));
        Optional<Reminder> result = service.findById(1L);
        assertTrue(result.isPresent());

        when(repository.findById(2L)).thenReturn(Optional.empty());
        assertFalse(service.findById(2L).isPresent());
    }

    @Test
    void testCreate() {
        when(repository.save(reminder)).thenReturn(reminder);
        Reminder result = service.create(reminder);
        assertNotNull(result);
    }

    @Test
    void testUpdate_Success() {
        when(repository.existsById(1L)).thenReturn(true);
        when(repository.save(reminder)).thenReturn(reminder);

        Reminder result = service.update(1L, reminder);
        assertNotNull(result);
    }

    @Test
    void testUpdate_NotFound() {
        when(repository.existsById(99L)).thenReturn(false);
        assertThrows(EntityNotFoundException.class, () -> service.update(99L, reminder));
    }

    @Test
    void testDeleteById_Success() {
        when(repository.existsById(1L)).thenReturn(true);
        doNothing().when(repository).deleteById(1L);

        assertDoesNotThrow(() -> service.deleteById(1L));
        verify(repository).deleteById(1L);
    }

    @Test
    void testDeleteById_NotFound() {
        when(repository.existsById(99L)).thenReturn(false);
        assertThrows(EntityNotFoundException.class, () -> service.deleteById(99L));
    }

    @Test
    void testExistsById() {
        when(repository.existsById(1L)).thenReturn(true);
        assertTrue(service.existsById(1L));

        when(repository.existsById(2L)).thenReturn(false);
        assertFalse(service.existsById(2L));
    }

    @Test
    void testExistsActiveReminder() {
        LocalDateTime time = LocalDateTime.now();
        when(repository.existsByPatient_UserIdAndScheduledTimeAndTitleAndTiming_TimingIDAndLockStatus(
                "PAT-01", time, "Uong thuoc", 2, false
        )).thenReturn(true);

        assertTrue(service.existsActiveReminder("PAT-01", time, "Uong thuoc", 2));
    }

    @Test
    void testGetPatientsWithRemindersToday() {
        Patient patient = new Patient();
        when(repository.findPatientsWithRemindersBetween(any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(List.of(patient));

        List<Patient> result = service.getPatientsWithRemindersToday();
        assertEquals(1, result.size());
    }

    @Test
    void testGetPatientRemindersToday() {
        when(repository.findActiveRemindersByPatientAndDateRange(eq("PAT-01"), any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(List.of(reminder));

        List<Reminder> result = service.getPatientRemindersToday("PAT-01");
        assertEquals(1, result.size());
    }
}
