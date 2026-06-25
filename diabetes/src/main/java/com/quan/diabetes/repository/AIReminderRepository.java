package com.quan.diabetes.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.quan.diabetes.entity.AIReminder;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AIReminderRepository extends JpaRepository<AIReminder, Long> {
    boolean existsByPatient_UserIdAndScheduledTimeAndTitleAndTiming_TimingIDAndStatus(
            String userId,
            LocalDateTime scheduledTime,
            String title,
            Integer timingId,
            boolean status
    );

    List<AIReminder> findByPatient_UserIdAndScheduledTimeGreaterThanEqualAndTitleOrderByScheduledTimeAsc(
            String userId,
            LocalDateTime scheduledTime,
            String title
    );
    List<AIReminder> findByClinicalExamination_ClinicalExamId(String clinicalExamId);

    @Query("""
            SELECT reminder
            FROM AIReminder reminder
            WHERE reminder.patient.userId = :patientUserId
              AND reminder.scheduledTime <= :scheduledTimeIsLessThan
              AND (reminder.status = true OR reminder.status IS NULL)
            ORDER BY reminder.scheduledTime DESC
            """)
    List<AIReminder> findByPatient_UserIdAndScheduledTimeLessThanEqualOrderByScheduledTimeDesc(
            @Param("patientUserId") String patientUserId,
            @Param("scheduledTimeIsLessThan") LocalDateTime scheduledTimeIsLessThan
    );

    @Query("""
            SELECT reminder
            FROM AIReminder reminder
            WHERE reminder.scheduledTime <= :now
              AND (reminder.isSent = false OR reminder.isSent IS NULL)
              AND (reminder.status = true OR reminder.status IS NULL)
            ORDER BY reminder.scheduledTime ASC
            """)
    List<AIReminder> findDueUnsentReminders(@Param("now") LocalDateTime now);
}
