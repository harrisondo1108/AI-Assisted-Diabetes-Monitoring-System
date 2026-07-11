package com.quan.diabetes.service.reminder;

import com.quan.diabetes.entity.Reminder;
import com.quan.diabetes.repository.ReminderRepository;
import com.quan.diabetes.service.notification.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReminderScheduledTask {
    @Autowired
    private ReminderRepository reminderRepository;

    @Autowired
    private EmailService emailService;

    @Scheduled(fixedRate = 60_000)
    @Transactional
    public void scanDueReminders() {
        List<Reminder> reminders = reminderRepository.findDueUnsentReminders(LocalDateTime.now());

        for (Reminder reminder : reminders) {
            sendReminder(reminder);
            String patientEmail = null;
            if (reminder.getPatient() != null) {
                patientEmail = reminder.getPatient().getEmail();
            }
            if (patientEmail != null && !patientEmail.trim().isEmpty()) {
                emailService.sendSimpleEmail(patientEmail.trim(), reminder.getTitle(), reminder.getMessage());
            } else {
                System.out.println("Warning: Patient " + (reminder.getPatient() != null ? reminder.getPatient().getUserId() : "null") + " has no email configured, fallback to default recipient");
                emailService.sendSimpleEmail("lequan13112005@gmail.com", reminder.getTitle(), reminder.getMessage());
            }
            reminder.setIsSent(true);
        }

        reminderRepository.saveAll(reminders);
    }

    private void sendReminder(Reminder reminder) {
        System.out.println("Sending AI reminder: " + reminder.getReminderId()
                + " - " + reminder.getTitle()
                + " - " + reminder.getMessage());
    }
}
