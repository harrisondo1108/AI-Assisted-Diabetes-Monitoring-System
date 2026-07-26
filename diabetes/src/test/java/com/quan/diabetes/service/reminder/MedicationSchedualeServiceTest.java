package com.quan.diabetes.service.reminder;

import com.quan.diabetes.dto.reminder.PrescriptionReminderDto;
import com.quan.diabetes.entity.ClinicalExamination;
import com.quan.diabetes.entity.MedicationTiming;
import com.quan.diabetes.entity.Patient;
import com.quan.diabetes.entity.PatientRoutine;
import com.quan.diabetes.entity.Reminder;
import com.quan.diabetes.repository.ClinicalExaminationRepository;
import com.quan.diabetes.repository.MedicationTimingRepository;
import com.quan.diabetes.repository.PatientRoutineRepository;
import com.quan.diabetes.repository.PrescriptionTimingRepository;
import com.quan.diabetes.repository.ReminderRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MedicationSchedualeServiceTest {

    @Mock
    private TemplateMedicationCreationService templateMedicationCreationService;

    @Mock
    private PrescriptionTimingRepository prescriptionTimingRepo;

    @Mock
    private MedicationTimingRepository medicationTimingRepo;

    @Mock
    private ClinicalExaminationRepository clinicalExaminationRepo;

    @Mock
    private PatientRoutineRepository patientRoutineRepo;

    @Mock
    private ReminderRepository reminderRepo;

    @InjectMocks
    private MedicationSchedualeService schedualeService;

    private PrescriptionReminderDto createDto(String timingName, LocalDate startDate, LocalDate endDate) {
        return new PrescriptionReminderDto("p1", "exam1", "Thuốc A", "1 viên", startDate, endDate, "Viên", "Uống", "Trước ăn", timingName, "Kế hoạch", null);
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when clinical examination is not found")
    void generateReminder_ExamNotFound() {
        when(clinicalExaminationRepo.findById("exam1")).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> schedualeService.generateReminder("exam1"));

        assertEquals("Clinical examination not found: exam1", ex.getMessage());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when patient is not found in clinical examination")
    void generateReminder_PatientNotFound() {
        ClinicalExamination exam = new ClinicalExamination();
        exam.setPatient(null);

        when(clinicalExaminationRepo.findById("exam1")).thenReturn(Optional.of(exam));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> schedualeService.generateReminder("exam1"));

        assertEquals("Patient not found for clinical exam: exam1", ex.getMessage());
    }

    @Test
    @DisplayName("Should lock future reminders if existing, fallback routine if not found, and process timings")
    void generateReminder_SuccessFlow_WithExistingRemindersAndFallbackRoutine() {
        Patient patient = new Patient();
        patient.setUserId("p1");
        patient.setFullName("Nguyễn Văn A");

        ClinicalExamination exam = new ClinicalExamination();
        exam.setPatient(patient);

        // Future reminders to lock
        Reminder existingFuture = new Reminder();
        existingFuture.setLockStatus(false);
        List<Reminder> futureReminders = List.of(existingFuture);

        when(clinicalExaminationRepo.findById("exam1")).thenReturn(Optional.of(exam));
        when(reminderRepo.findByPatient_UserIdAndScheduledTimeGreaterThanEqualAndTitleOrderByScheduledTimeAsc(
                eq("p1"), any(LocalDateTime.class), eq(MedicationSchedualeService.MEDICATION_REMINDER_TITLE)))
                .thenReturn(futureReminders);

        // PatientRoutine not found -> fallbacks to new PatientRoutine()
        when(patientRoutineRepo.findById("p1")).thenReturn(Optional.empty());

        // Timings setup
        MedicationTiming timing1 = new MedicationTiming();
        timing1.setTimingID(1);
        timing1.setTimingName("trước sáng");
        List<MedicationTiming> timings = List.of(timing1);
        when(medicationTimingRepo.findMedicationTimingsByClinicalExamId("exam1")).thenReturn(timings);

        // Prescriptions: 1 valid, others testing invalid filter branches
        LocalDate startDate = LocalDate.of(2026, 8, 1);
        LocalDate endDate = LocalDate.of(2026, 8, 2);

        PrescriptionReminderDto validDto = createDto("trước sáng", startDate, endDate);
        PrescriptionReminderDto dtoNullTimingName = createDto(null, startDate, endDate);
        PrescriptionReminderDto dtoWrongTimingName = createDto("trưa", startDate, endDate);
        PrescriptionReminderDto dtoNullStartDate = createDto("trước sáng", null, endDate);
        PrescriptionReminderDto dtoNullEndDate = createDto("trước sáng", startDate, null);
        PrescriptionReminderDto dtoEndDateBeforeStart = createDto("trước sáng", startDate, startDate.minusDays(1));

        List<PrescriptionReminderDto> allDtos = List.of(
                validDto, dtoNullTimingName, dtoWrongTimingName,
                dtoNullStartDate, dtoNullEndDate, dtoEndDateBeforeStart
        );
        when(prescriptionTimingRepo.findDescriptionRemindersByClinicalExamId("exam1")).thenReturn(allDtos);

        when(templateMedicationCreationService.generateGroupReminder(eq("Nguyễn Văn A"), eq("trước sáng"), any()))
                .thenReturn("Nội dung nhắc nhở");

        // Reminder repo exists check: return false
        when(reminderRepo.existsByPatient_UserIdAndScheduledTimeAndTitleAndTiming_TimingIDAndLockStatus(
                eq("p1"), any(LocalDateTime.class), eq(MedicationSchedualeService.MEDICATION_REMINDER_TITLE), eq(1), eq(false)))
                .thenReturn(false);

        schedualeService.generateReminder("exam1");

        // Verify existing future reminder locked
        assertTrue(existingFuture.getLockStatus());
        verify(reminderRepo).saveAll(futureReminders);

        // Verify template generation and save
        verify(templateMedicationCreationService).generateGroupReminder(eq("Nguyễn Văn A"), eq("trước sáng"), any());
        verify(reminderRepo, atLeastOnce()).save(any(Reminder.class));
    }

    @Test
    @DisplayName("Should skip timing when remindersForTiming is empty or segment active prescriptions is empty")
    void generateReminder_SkipEmptyTimingsAndEmptySegments() {
        Patient patient = new Patient();
        patient.setUserId("p1");

        ClinicalExamination exam = new ClinicalExamination();
        exam.setPatient(patient);

        when(clinicalExaminationRepo.findById("exam1")).thenReturn(Optional.of(exam));
        when(reminderRepo.findByPatient_UserIdAndScheduledTimeGreaterThanEqualAndTitleOrderByScheduledTimeAsc(
                any(), any(), any())).thenReturn(null);

        PatientRoutine routine = new PatientRoutine();
        when(patientRoutineRepo.findById("p1")).thenReturn(Optional.of(routine));

        // Timing 1: Has valid prescription for a wide range (Aug 1 to Aug 5)
        MedicationTiming timing1 = new MedicationTiming();
        timing1.setTimingID(1);
        timing1.setTimingName("buổi sáng");

        // Timing 2: Has no valid prescription matching timing name
        MedicationTiming timing2 = new MedicationTiming();
        timing2.setTimingID(2);
        timing2.setTimingName("buổi tối");

        when(medicationTimingRepo.findMedicationTimingsByClinicalExamId("exam1"))
                .thenReturn(List.of(timing1, timing2));

        // Prescription 1: Aug 1 to Aug 2 for "buổi sáng"
        PrescriptionReminderDto dto1 = createDto("buổi sáng", LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 2));

        // Prescription 2: Aug 4 to Aug 5 for "buổi sáng" (Leaves gap on Aug 3!)
        PrescriptionReminderDto dto2 = createDto("buổi sáng", LocalDate.of(2026, 8, 4), LocalDate.of(2026, 8, 5));

        when(prescriptionTimingRepo.findDescriptionRemindersByClinicalExamId("exam1"))
                .thenReturn(List.of(dto1, dto2));

        when(templateMedicationCreationService.generateGroupReminder(any(), any(), any()))
                .thenReturn("Nội dung");

        when(reminderRepo.existsByPatient_UserIdAndScheduledTimeAndTitleAndTiming_TimingIDAndLockStatus(
                any(), any(), any(), anyInt(), anyBoolean())).thenReturn(true); // Already exists so skips save

        schedualeService.generateReminder("exam1");

        // Verify template creation called for segment 1 (Aug 1-3) and segment 3 (Aug 4-6), but skipped for segment 2 (Aug 3-4 gap)
        verify(templateMedicationCreationService, times(2)).generateGroupReminder(any(), any(), any());
    }

    @Test
    @DisplayName("Should cover empty future reminders list and prescription with startInRange true but endInRange false")
    void generateReminder_EmptyFutureRemindersAndPartialSegmentOverlap() {
        Patient patient = new Patient();
        patient.setUserId("p1");

        ClinicalExamination exam = new ClinicalExamination();
        exam.setPatient(patient);

        when(clinicalExaminationRepo.findById("exam1")).thenReturn(Optional.of(exam));
        // Return non-null empty list to cover futureReminders != null && !futureReminders.isEmpty() (false for isEmpty)
        when(reminderRepo.findByPatient_UserIdAndScheduledTimeGreaterThanEqualAndTitleOrderByScheduledTimeAsc(
                any(), any(), any())).thenReturn(Collections.emptyList());

        PatientRoutine routine = new PatientRoutine();
        when(patientRoutineRepo.findById("p1")).thenReturn(Optional.of(routine));

        MedicationTiming timing = new MedicationTiming();
        timing.setTimingID(1);
        timing.setTimingName("buổi sáng");
        when(medicationTimingRepo.findMedicationTimingsByClinicalExamId("exam1"))
                .thenReturn(List.of(timing));

        // Prescription 1: Aug 1 to Aug 5
        PrescriptionReminderDto dto1 = createDto("buổi sáng", LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 5));
        // Prescription 2: Aug 1 to Aug 3 (startInRange is true for segment Aug 1 to Aug 6, but endInRange is false because Aug 3 < Aug 5)
        PrescriptionReminderDto dto2 = createDto("buổi sáng", LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 3));

        when(prescriptionTimingRepo.findDescriptionRemindersByClinicalExamId("exam1"))
                .thenReturn(List.of(dto1, dto2));

        when(templateMedicationCreationService.generateGroupReminder(any(), any(), any()))
                .thenReturn("Nội dung");

        schedualeService.generateReminder("exam1");

        verify(templateMedicationCreationService, atLeastOnce()).generateGroupReminder(any(), any(), any());
    }
}
