package com.quan.diabetes.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.quan.diabetes.entity.Reminder;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReminderRepository extends JpaRepository<Reminder, Long> {
    boolean existsByPatient_UserIdAndScheduledTimeAndTitleAndTiming_TimingIDAndLockStatus(
            String userId,
            LocalDateTime scheduledTime,
            String title,
            Integer timingId,
            Boolean lockStatus
    );

    List<Reminder> findByPatient_UserIdAndScheduledTimeGreaterThanEqualAndTitleOrderByScheduledTimeAsc(
            String userId,
            LocalDateTime scheduledTime,
            String title
    );
    
    List<Reminder> findByPatient_UserIdAndTitle(String userId, String title);
    
    List<Reminder> findByClinicalExamination_ClinicalExamId(String clinicalExamId);

    @Query("""
            SELECT reminder
            FROM Reminder reminder
            WHERE reminder.patient.userId = :patientUserId
              AND reminder.scheduledTime <= :scheduledTimeIsLessThan
              AND (reminder.lockStatus IS NULL OR reminder.lockStatus = false)
            ORDER BY reminder.scheduledTime DESC
            """)
    List<Reminder> findByPatient_UserIdAndScheduledTimeLessThanEqualOrderByScheduledTimeDesc(
            @Param("patientUserId") String patientUserId,
            @Param("scheduledTimeIsLessThan") LocalDateTime scheduledTimeIsLessThan
    );

    @Query("""
            SELECT DISTINCT r.patient
            FROM Reminder r
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
            FROM Reminder r
            WHERE r.patient.userId = :patientId
              AND r.scheduledTime >= :startOfDay 
              AND r.scheduledTime <= :endOfDay
              AND (r.lockStatus IS NULL OR r.lockStatus = false)
            ORDER BY r.scheduledTime ASC
            """)
    List<Reminder> findActiveRemindersByPatientAndDateRange(
            @Param("patientId") String patientId,
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay
    );

    List<Reminder> findByScheduledTimeBetween(LocalDateTime start, LocalDateTime end);

    @Query("""
            SELECT reminder
            FROM Reminder reminder
            WHERE reminder.scheduledTime <= :now
              AND (reminder.isSent = false OR reminder.isSent IS NULL)
              AND (reminder.lockStatus IS NULL OR reminder.lockStatus = false)
            ORDER BY reminder.scheduledTime ASC
            """)
    List<Reminder> findDueUnsentReminders(@Param("now") LocalDateTime now);

    List<Reminder> findTop10ByClinicalExamination_Doctor_UserIdOrderByScheduledTimeDesc(String doctorId);
}
