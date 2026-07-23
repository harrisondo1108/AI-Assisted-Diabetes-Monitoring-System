package com.quan.diabetes.service.ai.impl;

import com.quan.diabetes.entity.Reminder;
import com.quan.diabetes.repository.ReminderRepository;
import com.quan.diabetes.service.ai.ReminderService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ReminderServiceImpl implements ReminderService {

    private final ReminderRepository reminderRepository;

    public ReminderServiceImpl(ReminderRepository reminderRepository) {
        this.reminderRepository = reminderRepository;
    }

    @Override
    public List<Reminder> getListByIdAndScheduledTimeLessThanEqual(String id, LocalDateTime scheduledTimeIsLessThan) {
        return reminderRepository.findByPatient_UserIdAndScheduledTimeLessThanEqualOrderByScheduledTimeDesc(id, scheduledTimeIsLessThan);
    }

    @Override
    public List<Reminder> findAll() {
        return reminderRepository.findAll();
    }

    @Override
    public Optional<Reminder> findById(Long id) {
        return reminderRepository.findById(id);
    }

    @Override
    public Reminder create(Reminder entity) {
        return reminderRepository.save(entity);
    }

    @Override
    public Reminder update(Long id, Reminder entity) {
        if (!reminderRepository.existsById(id)) {
            throw new EntityNotFoundException("Reminder not found with id: " + id);
        }
        return reminderRepository.save(entity);
    }

    @Override
    public void deleteById(Long id) {
        if (!reminderRepository.existsById(id)) {
            throw new EntityNotFoundException("Reminder not found with id: " + id);
        }
        reminderRepository.deleteById(id);
    }

    @Override
    public boolean existsById(Long id) {
        return reminderRepository.existsById(id);
    }

    @Override
    public List<com.quan.diabetes.entity.Patient> getPatientsWithRemindersToday() {
        LocalDateTime startOfDay = java.time.LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = java.time.LocalDate.now().atTime(java.time.LocalTime.MAX);
        return reminderRepository.findPatientsWithRemindersBetween(startOfDay, endOfDay);
    }

    @Override
    public List<Reminder> getPatientRemindersToday(String patientId) {
        LocalDateTime startOfDay = java.time.LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = java.time.LocalDate.now().atTime(java.time.LocalTime.MAX);
        return reminderRepository.findActiveRemindersByPatientAndDateRange(patientId, startOfDay, endOfDay);
    }
}
