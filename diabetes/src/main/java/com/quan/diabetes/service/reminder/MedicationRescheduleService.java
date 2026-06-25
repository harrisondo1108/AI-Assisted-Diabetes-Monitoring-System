package com.quan.diabetes.service.reminder;

import com.quan.diabetes.entity.AIReminder;
import com.quan.diabetes.entity.PatientRoutine;
import com.quan.diabetes.repository.AIReminderRepository;
import com.quan.diabetes.util.ReminderTimeCalculator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class MedicationRescheduleService {

    private final AIReminderRepository aiReminderRepository;

    public MedicationRescheduleService(AIReminderRepository aiReminderRepository) {
        this.aiReminderRepository = aiReminderRepository;
    }

    @Transactional
    public void rescheduleFutureMedicationReminders(
            String patientId,
            PatientRoutine newRoutine
    ) {
        if(!patientId.equals(newRoutine.getPatient().getUserId())){
            throw new IllegalArgumentException("patientId does not match the patient in the new routine");
        }
        LocalDateTime changedAt = LocalDateTime.now();
        List<AIReminder> futureReminders =
                aiReminderRepository.findByPatient_UserIdAndScheduledTimeGreaterThanEqualAndTitleOrderByScheduledTimeAsc(
                        patientId,
                        changedAt,
                        MedicationSchedualeService.MEDICATION_REMINDER_TITLE);

        if (futureReminders.isEmpty()) {
            return;
        }

        for (AIReminder reminder : futureReminders) {
            if (Boolean.FALSE.equals(reminder.getStatus())) {
                continue; // Skip locked reminders
            }
            if (reminder.getTiming() == null || reminder.getTiming().getTimingName() == null) {
                continue;
            }
            LocalDate reminderDate = reminder.getScheduledTime().toLocalDate();
            LocalTime newReminderTime =
                    ReminderTimeCalculator.calculateReminderTime(reminder.getTiming().getTimingName(), newRoutine);
            reminder.setScheduledTime(LocalDateTime.of(reminderDate, newReminderTime));
            aiReminderRepository.save(reminder);
        }
    }
}
