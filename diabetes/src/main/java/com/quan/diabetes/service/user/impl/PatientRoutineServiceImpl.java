package com.quan.diabetes.service.impl;

import com.quan.diabetes.entity.Patient;
import com.quan.diabetes.entity.PatientRoutine;
import com.quan.diabetes.repository.PatientRepository;
import com.quan.diabetes.repository.PatientRoutineRepository;
import com.quan.diabetes.service.reminder.ReminderRescheduleService;
import com.quan.diabetes.service.user.PatientRoutineService;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class PatientRoutineServiceImpl implements PatientRoutineService {


    private final PatientRoutineRepository patientRoutineRepository;

    private final PatientRepository patientRepository;

    private final ReminderRescheduleService reminderRescheduleService;

    public PatientRoutineServiceImpl(
            PatientRoutineRepository patientRoutineRepository,
            PatientRepository patientRepository,
            ReminderRescheduleService reminderRescheduleService
    ) {
        this.patientRoutineRepository = patientRoutineRepository;
        this.patientRepository = patientRepository;
        this.reminderRescheduleService = reminderRescheduleService;
    }

    @Override
    public List<PatientRoutine> findAll() {
        return patientRoutineRepository.findAll();
    }

    @Override
    public Optional<PatientRoutine> findById(String id) {
        return patientRoutineRepository.findById(id);
    }

    @Override
    public PatientRoutine create(PatientRoutine entity) {
        if (entity == null || entity.getUserId() == null || entity.getUserId().isBlank()) {
            throw new RuntimeException("PatientRoutine UserID must not be null.");
        }

        if (!patientRepository.existsById(entity.getUserId())) {
            throw new RuntimeException("Patient not found with id: " + entity.getUserId());
        }

        return patientRoutineRepository.save(entity);
    }

    @Override
    @Transactional
    public PatientRoutine update(String id, PatientRoutine entity) {
        if (id == null || id.isBlank()) {
            throw new RuntimeException("PatientRoutine id must not be null.");
        }

        if (!patientRoutineRepository.existsById(id)) {
            throw new RuntimeException("PatientRoutine not found with id: " + id);
        }
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Patient not found with id: " + id));

        entity.setUserId(id);
        entity.setPatient(patient);
        PatientRoutine savedRoutine = patientRoutineRepository.save(entity);
        reminderRescheduleService.rescheduleFutureMedicationReminders(id, savedRoutine);

        return savedRoutine;
    }

    @Override
    public void deleteById(String id) {
        patientRoutineRepository.deleteById(id);
    }

    @Override
    public boolean existsById(String id) {
        return patientRoutineRepository.existsById(id);
    }
}