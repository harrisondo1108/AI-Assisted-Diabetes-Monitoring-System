package com.quan.diabetes.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.quan.diabetes.entity.AIReminder;
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
}
