package com.quan.diabetes.service.reminder;

import com.quan.diabetes.dto.reminder.PrescriptionReminderDto;
import com.quan.diabetes.entity.AIReminder;
import com.quan.diabetes.entity.ClinicalExamination;
import com.quan.diabetes.entity.MedicationTiming;
import com.quan.diabetes.entity.Patient;
import com.quan.diabetes.entity.PatientRoutine;
import com.quan.diabetes.repository.AIReminderRepository;
import com.quan.diabetes.repository.ClinicalExaminationRepository;
import com.quan.diabetes.repository.MedicationTimingRepository;
import com.quan.diabetes.repository.PatientRoutineRepository;
import com.quan.diabetes.repository.PrescriptionTimingRepository;
import com.quan.diabetes.util.ReminderTimeCalculator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class MedicationSchedualeService {

    public static final String MEDICATION_REMINDER_TITLE = "Nhắc nhở lịch sử dụng thuốc";


    @Autowired
    private TemplateMedicationCreationService templateMedicationCreationService;

    @Autowired
    private PrescriptionTimingRepository prescriptionTimingRepo;

    @Autowired
    private MedicationTimingRepository medicationTimingRepo;

    @Autowired
    private ClinicalExaminationRepository clinicalExaminationRepo;

    @Autowired
    private PatientRoutineRepository patientRoutineRepo;

    @Autowired
    private AIReminderRepository aiReminderRepo;

    @Transactional
    public void generateReminder(String clinicalExamId) {
        ClinicalExamination clinicalExamination = clinicalExaminationRepo.findById(clinicalExamId)
                .orElseThrow(() -> new IllegalArgumentException("Clinical examination not found: " + clinicalExamId));
        Patient patient = clinicalExamination.getPatient();
        if (patient == null) {
            throw new IllegalArgumentException("Patient not found for clinical exam: " + clinicalExamId);
        }

        // Before generating new reminders, lock all future medication reminders for this patient
        LocalDateTime now = LocalDateTime.now();
        List<AIReminder> futureReminders = aiReminderRepo.findByPatient_UserIdAndScheduledTimeGreaterThanEqualAndTitleOrderByScheduledTimeAsc(
                patient.getUserId(),
                now,
                MEDICATION_REMINDER_TITLE
        );
        if (futureReminders != null && !futureReminders.isEmpty()) {
            for (AIReminder r : futureReminders) {
                r.setLockStatus(true);
            }
            aiReminderRepo.saveAll(futureReminders);
        }

        String name = patient.getFullName();
        PatientRoutine patientRoutine = patientRoutineRepo.findById(patient.getUserId()).orElse(new PatientRoutine());
        List<PrescriptionReminderDto> medicineList =
                prescriptionTimingRepo.findDescriptionRemindersByClinicalExamId(clinicalExamId);
        List<MedicationTiming> timings =
                medicationTimingRepo.findMedicationTimingsByClinicalExamId(clinicalExamId);

        for (MedicationTiming timing : timings) {
            String time = timing.getTimingName();
            List<PrescriptionReminderDto> remindersForTiming = new ArrayList<>();
            Set<LocalDate> dateSet = new HashSet<>();

            for (PrescriptionReminderDto prescription : medicineList) {
                if (isValidPrescriptionForTiming(prescription, time)) {
                    dateSet.add(prescription.getStartDate());
                    dateSet.add(prescription.getEndDate().plusDays(1));
                    remindersForTiming.add(prescription);
                }
            }

            if (remindersForTiming.isEmpty()) {
                continue;
            }
            List<LocalDate> dateList = new ArrayList<>(dateSet);
            Collections.sort(dateList);
            for(LocalDate date : dateList) {
                System.out.println(date);
            }

            for (int i = 0; i < dateList.size() - 1; i++) {
                LocalDate startDate = dateList.get(i);
                LocalDate endDateExclusive = dateList.get(i + 1);
                List<PrescriptionReminderDto> reminderForDate =
                        findPrescriptionsActiveForWholeSegment(remindersForTiming, startDate, endDateExclusive);

                if (reminderForDate.isEmpty()) {
                    continue;
                }
//                for (PrescriptionReminderDto prescription : reminderForDate) {
//                    System.out.println(prescription);
//                }
                String segmentReminder = getContentFromTemplate(name, time, reminderForDate);
                LocalTime timeForSegment = ReminderTimeCalculator.calculateReminderTime(time, patientRoutine);
                saveRemindersForDateRange(patient, timing, segmentReminder, startDate, endDateExclusive, timeForSegment, clinicalExamination);
            }
        }
    }

    private String getContentFromTemplate(String name, String time, List<PrescriptionReminderDto> reminderForDate) {
        return templateMedicationCreationService.generateGroupReminder(name, time, reminderForDate);
    }

    private boolean isValidPrescriptionForTiming(PrescriptionReminderDto prescription, String time) {
        return prescription.getTimingName() != null
                && prescription.getTimingName().equals(time)
                && prescription.getStartDate() != null
                && prescription.getEndDate() != null
                && !prescription.getEndDate().isBefore(prescription.getStartDate());
    }

    private List<PrescriptionReminderDto> findPrescriptionsActiveForWholeSegment(
            List<PrescriptionReminderDto> prescriptions,
            LocalDate startDate,
            LocalDate endDateExclusive
    ) {
        List<PrescriptionReminderDto> activePrescriptions = new ArrayList<>();
        LocalDate lastDateInSegment = endDateExclusive.minusDays(1);

        for (PrescriptionReminderDto prescription : prescriptions) {
            boolean startInRange = !startDate.isBefore(prescription.getStartDate())
                    && !startDate.isAfter(prescription.getEndDate());
            boolean endInRange = !lastDateInSegment.isBefore(prescription.getStartDate())
                    && !lastDateInSegment.isAfter(prescription.getEndDate());

            if (startInRange && endInRange) {
                activePrescriptions.add(prescription);
            }
        }

        return activePrescriptions;
    }

    private void saveRemindersForDateRange(
            Patient patient,
            MedicationTiming timing,
            String message,
            LocalDate startDate,
            LocalDate endDateExclusive,
            LocalTime reminderTime,
            ClinicalExamination clinicalExamination
    ) {
        LocalDate currentDate = startDate;
        while (currentDate.isBefore(endDateExclusive)) {
            LocalDateTime reminderDateTime = LocalDateTime.of(currentDate, reminderTime);

            if (!aiReminderRepo.existsByPatient_UserIdAndScheduledTimeAndTitleAndTiming_TimingIDAndLockStatus(
                    patient.getUserId(),
                    reminderDateTime,
                    MEDICATION_REMINDER_TITLE,
                    timing.getTimingID(),
                    false)) {
                AIReminder reminder = new AIReminder();
                reminder.setTitle(MEDICATION_REMINDER_TITLE);
                reminder.setMessage(message);
                reminder.setScheduledTime(reminderDateTime);
                reminder.setPatient(patient);
                reminder.setTiming(timing);
                reminder.setIsRead(false);
                reminder.setClinicalExamination(clinicalExamination);
                reminder.setLockStatus(false);

                aiReminderRepo.save(reminder);
            }

            currentDate = currentDate.plusDays(1);
        }
    }
}
