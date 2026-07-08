package com.quan.diabetes.service.ai;

import com.quan.diabetes.entity.AIReminder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AIReminderService {

    public List<AIReminder> findAll();

    public Optional<AIReminder> findById(Long id);

    public AIReminder create(AIReminder entity);

    public AIReminder update(Long id, AIReminder entity);

    public void deleteById(Long id);

    public boolean existsById(Long id);

    List<AIReminder> getListByIdAndScheduledTimeLessThanEqual(String id, LocalDateTime scheduledTimeIsLessThan);

    boolean existsActiveReminder(String userId, LocalDateTime scheduledTime, String title, Integer timingId);

    List<com.quan.diabetes.entity.Patient> getPatientsWithRemindersToday();

    List<AIReminder> getPatientRemindersToday(String patientId);
}
