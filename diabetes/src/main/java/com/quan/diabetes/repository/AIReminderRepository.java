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
    boolean existsByPatient_UserIdAndScheduledTimeAndTitleAndTiming_TimingID(
            String userId,
            LocalDateTime scheduledTime,
            String title,
            Integer timingId
    );

    List<AIReminder> findByPatient_UserIdAndScheduledTimeGreaterThanEqualAndTitleOrderByScheduledTimeAsc(
            String userId,
            LocalDateTime scheduledTime,
            String title
    );
    
    List<AIReminder> findByPatient_UserIdAndTitle(String userId, String title);

    @Query("""
            SELECT reminder
            FROM AIReminder reminder
            WHERE reminder.patient.userId = :patientUserId
              AND reminder.scheduledTime <= :scheduledTimeIsLessThan
            ORDER BY reminder.scheduledTime DESC
            """)
    List<AIReminder> findByPatient_UserIdAndScheduledTimeLessThanEqualOrderByScheduledTimeDesc(
            @Param("patientUserId") String patientUserId,
            @Param("scheduledTimeIsLessThan") LocalDateTime scheduledTimeIsLessThan
    );
}
