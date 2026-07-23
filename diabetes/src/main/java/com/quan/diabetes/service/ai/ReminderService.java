package com.quan.diabetes.service.ai;

import com.quan.diabetes.entity.Reminder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReminderService {

    public List<Reminder> findAll();

    public Optional<Reminder> findById(Long id);

    public Reminder create(Reminder entity);

    public Reminder update(Long id, Reminder entity);

    public void deleteById(Long id);

    public boolean existsById(Long id);

    List<Reminder> getListByIdAndScheduledTimeLessThanEqual(String id, LocalDateTime scheduledTimeIsLessThan);

    List<com.quan.diabetes.entity.Patient> getPatientsWithRemindersToday();

    List<Reminder> getPatientRemindersToday(String patientId);
}
