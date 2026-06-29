package com.quan.diabetes.service.reminder;

import com.quan.diabetes.entity.AIReminder;
import com.quan.diabetes.repository.AIReminderRepository;
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
    private AIReminderRepository aiReminderRepository;

    @Autowired
    private EmailService emailService;

    @Scheduled(fixedRate = 60_000)
    @Transactional
    public void scanDueReminders() {
        // Vô hiệu hóa do các cột IsSent, Status đã bị xóa trong database mới.
    }

    private void sendReminder(AIReminder reminder) {
        System.out.println("Sending AI reminder: " + reminder.getAiReminderId()
                + " - " + reminder.getTitle()
                + " - " + reminder.getMessage());
    }
}
