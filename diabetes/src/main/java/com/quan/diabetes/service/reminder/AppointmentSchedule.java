package com.quan.diabetes.service.reminder;

import com.quan.diabetes.entity.AIReminder;
import com.quan.diabetes.entity.ClinicalExamination;
import com.quan.diabetes.entity.User;
import com.quan.diabetes.repository.AIReminderRepository;
import com.quan.diabetes.repository.ClinicalExaminationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class AppointmentSchedule {

    public static final String APPOINTMENT_REMINDER_TITLE = "Xác nhận lịch tái khám";

    @Autowired
    private AIReminderRepository aiReminderRepo;

    @Autowired
    private ClinicalExaminationRepository clinicalExaminationRepo;

    @Transactional
    public void generateAppointmentReminder(String clinicalExamId) {
        ClinicalExamination clinicalExamination = clinicalExaminationRepo.findById(clinicalExamId).orElse(null);
        if (clinicalExamination == null || clinicalExamination.getPatient() == null) {
            return;
        }

        // Lock any existing appointment reminders for this examination
        List<AIReminder> existingReminders = aiReminderRepo.findByPatient_UserIdAndTitle(clinicalExamination.getPatient().getUserId(), APPOINTMENT_REMINDER_TITLE);
        if (existingReminders != null && !existingReminders.isEmpty()) {
            for (AIReminder r : existingReminders) {
                r.setLockStatus(true);
            }
            aiReminderRepo.saveAll(existingReminders);
        }

        LocalDateTime nextAppointment = clinicalExamination.getNextAppointment();
        if (nextAppointment != null) {
            String doctorName = "bác sĩ";
            if (clinicalExamination.getDoctor() != null) {
                User doctorUser = clinicalExamination.getDoctor();
                if (doctorUser.getProfile() != null && doctorUser.getProfile().getFullName() != null) {
                    doctorName = doctorUser.getProfile().getFullName();
                } else {
                    doctorName = doctorUser.getUserId();
                }
            }

            String formattedTime = nextAppointment.format(DateTimeFormatter.ofPattern("HH:mm"));
            String formattedDate = nextAppointment.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

            // Reminder 1: 1 day before next appointment at 7:00 AM
            LocalDateTime timeDayBefore = nextAppointment.minusDays(1).withHour(7).withMinute(0).withSecond(0).withNano(0);
            AIReminder reminderDayBefore = new AIReminder();
            reminderDayBefore.setTitle(APPOINTMENT_REMINDER_TITLE);
            reminderDayBefore.setMessage("Xin chào quý bệnh nhân, vào ngày mai (ngày " + formattedDate + "), bạn có lịch hẹn tái khám với BS. " + doctorName + " vào lúc " + formattedTime + ". Việc tái khám đúng lịch là rất quan trọng để bác sĩ có thể theo dõi sát sao tiến trình điều trị và kiểm soát chỉ số đường huyết tốt nhất cho bạn. Kính chúc bạn luôn nhiều sức khỏe và bình an!");
            reminderDayBefore.setScheduledTime(timeDayBefore);
            reminderDayBefore.setPatient(clinicalExamination.getPatient());
            reminderDayBefore.setIsRead(false);
            reminderDayBefore.setClinicalExamination(clinicalExamination);
            reminderDayBefore.setLockStatus(false);
            aiReminderRepo.save(reminderDayBefore);

            // Reminder 2: On the day of next appointment at 7:00 AM
            LocalDateTime timeOnDay = nextAppointment.withHour(7).withMinute(0).withSecond(0).withNano(0);
            AIReminder reminderOnDay = new AIReminder();
            reminderOnDay.setTitle(APPOINTMENT_REMINDER_TITLE);
            reminderOnDay.setMessage("Xin chào quý bệnh nhân, hôm nay (ngày " + formattedDate + "), bạn có lịch hẹn tái khám với BS. " + doctorName + " vào lúc " + formattedTime + ". Việc tái khám đúng lịch là rất quan trọng để bác sĩ có thể theo dõi sát sao tiến trình điều trị và kiểm soát chỉ số đường huyết tốt nhất cho bạn. Kính chúc bạn luôn nhiều sức khỏe và bình an!");
            reminderOnDay.setScheduledTime(timeOnDay);
            reminderOnDay.setPatient(clinicalExamination.getPatient());
            reminderOnDay.setIsRead(false);
            reminderOnDay.setClinicalExamination(clinicalExamination);
            reminderOnDay.setLockStatus(false);
            aiReminderRepo.save(reminderOnDay);
        }
    }
}
