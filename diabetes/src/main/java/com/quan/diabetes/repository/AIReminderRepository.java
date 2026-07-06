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
    boolean existsByPatient_UserIdAndScheduledTimeAndTitleAndTiming_TimingIDAndLockStatus(
            String userId,
            LocalDateTime scheduledTime,
            String title,
            Integer timingId,
            Boolean lockStatus
    );

    List<AIReminder> findByPatient_UserIdAndScheduledTimeGreaterThanEqualAndTitleOrderByScheduledTimeAsc(
            String userId,
            LocalDateTime scheduledTime,
            String title
    );
    
    List<AIReminder> findByPatient_UserIdAndTitle(String userId, String title);
    
    List<AIReminder> findByClinicalExamination_ClinicalExamId(String clinicalExamId);

    @Query("""
            SELECT reminder
            FROM AIReminder reminder
            WHERE reminder.patient.userId = :patientUserId
              AND reminder.scheduledTime <= :scheduledTimeIsLessThan
              AND (reminder.lockStatus IS NULL OR reminder.lockStatus = false)
            ORDER BY reminder.scheduledTime DESC
            """)
    List<AIReminder> findByPatient_UserIdAndScheduledTimeLessThanEqualOrderByScheduledTimeDesc(
            @Param("patientUserId") String patientUserId,
            @Param("scheduledTimeIsLessThan") LocalDateTime scheduledTimeIsLessThan
    );

    @Query("""
            SELECT DISTINCT r.patient
            FROM AIReminder r
            WHERE r.scheduledTime >= :startOfDay 
              AND r.scheduledTime <= :endOfDay
              AND (r.lockStatus IS NULL OR r.lockStatus = false)
            """)
    List<com.quan.diabetes.entity.Patient> findPatientsWithRemindersBetween(
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay
    );

    @Query("""
            SELECT r
            FROM AIReminder r
            WHERE r.patient.userId = :patientId
              AND r.scheduledTime >= :startOfDay 
              AND r.scheduledTime <= :endOfDay
              AND (r.lockStatus IS NULL OR r.lockStatus = false)
            ORDER BY r.scheduledTime ASC
            """)
    List<AIReminder> findActiveRemindersByPatientAndDateRange(
            @Param("patientId") String patientId,
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay
    );

    @Query("""
            SELECT reminder
            FROM AIReminder reminder
            WHERE reminder.scheduledTime <= :now
              AND (reminder.isSent = false OR reminder.isSent IS NULL)
              AND (reminder.lockStatus IS NULL OR reminder.lockStatus = false)
            ORDER BY reminder.scheduledTime ASC
            """)
    List<AIReminder> findDueUnsentReminders(@Param("now") LocalDateTime now);
}
