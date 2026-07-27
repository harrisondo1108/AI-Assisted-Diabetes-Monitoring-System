package com.quan.diabetes.service.reminder;

import com.quan.diabetes.entity.Reminder;
import com.quan.diabetes.entity.ClinicalExamination;
import com.quan.diabetes.entity.User;
import com.quan.diabetes.repository.ReminderRepository;
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
    private ReminderRepository reminderRepo;

    @Autowired
    private ClinicalExaminationRepository clinicalExaminationRepo;

    @Transactional
    public void generateAppointmentReminder(String clinicalExamId) {
        ClinicalExamination clinicalExamination = clinicalExaminationRepo.findById(clinicalExamId).orElse(null);
        if (clinicalExamination == null || clinicalExamination.getPatient() == null) {
            return;
        }

        // Lock any FUTURE appointment reminders for this patient (scheduledTime >= now)
        LocalDateTime now = LocalDateTime.now();
        List<Reminder> existingReminders = reminderRepo
                .findByPatient_UserIdAndTitle(clinicalExamination.getPatient().getUserId(), APPOINTMENT_REMINDER_TITLE);
        if (existingReminders != null && !existingReminders.isEmpty()) {
            for (Reminder r : existingReminders) {
                // Chỉ lock các reminder có thời gian gửi LỚN HƠN thời điểm tạo reminder mới
                if (r.getScheduledTime() != null && !r.getScheduledTime().isBefore(now)) {
                    r.setLockStatus(true);
                }
            }
            reminderRepo.saveAll(existingReminders);
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
            String formattedDate = nextAppointment.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

            // Reminder 1: 1 day before next appointment at 7:00 AM
            LocalDateTime timeDayBefore = nextAppointment.minusDays(1).withHour(7).withMinute(0).withSecond(0)
                    .withNano(0);
            Reminder reminderDayBefore = new Reminder();
            reminderDayBefore.setTitle(APPOINTMENT_REMINDER_TITLE);
            reminderDayBefore.setMessage("Xin chào quý bệnh nhân, vào ngày mai (ngày " + formattedDate
                    + "), bạn có lịch hẹn tái khám với BS. " + doctorName
                    + ". Việc tái khám đúng lịch là rất quan trọng để bác sĩ có thể theo dõi sát sao tiến trình điều trị và kiểm soát chỉ số đường huyết tốt nhất cho bạn. Kính chúc bạn luôn nhiều sức khỏe và bình an!");
            reminderDayBefore.setScheduledTime(timeDayBefore);
            reminderDayBefore.setPatient(clinicalExamination.getPatient());
            reminderDayBefore.setIsRead(false);
            reminderDayBefore.setClinicalExamination(clinicalExamination);
            reminderDayBefore.setLockStatus(false);
            reminderRepo.save(reminderDayBefore);

            // Reminder 2: On the day of next appointment at 7:00 AM
            LocalDateTime timeOnDay = nextAppointment.withHour(7).withMinute(0).withSecond(0).withNano(0);
            Reminder reminderOnDay = new Reminder();
            reminderOnDay.setTitle(APPOINTMENT_REMINDER_TITLE);
            reminderOnDay.setMessage("Xin chào quý bệnh nhân, hôm nay (ngày " + formattedDate
                    + "), bạn có lịch hẹn tái khám với BS. " + doctorName
                    + ". Việc tái khám đúng lịch là rất quan trọng để bác sĩ có thể theo dõi sát sao tiến trình điều trị và kiểm soát chỉ số đường huyết tốt nhất cho bạn. Kính chúc bạn luôn nhiều sức khỏe và bình an!");
            reminderOnDay.setScheduledTime(timeOnDay);
            reminderOnDay.setPatient(clinicalExamination.getPatient());
            reminderOnDay.setIsRead(false);
            reminderOnDay.setClinicalExamination(clinicalExamination);
            reminderOnDay.setLockStatus(false);
            reminderRepo.save(reminderOnDay);
        }
    }
}
