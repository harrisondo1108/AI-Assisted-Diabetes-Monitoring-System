package com.quan.diabetes.service.impl;

import com.quan.diabetes.entity.PatientRoutine;
import com.quan.diabetes.repository.PatientRepository;
import com.quan.diabetes.repository.PatientRoutineRepository;
import com.quan.diabetes.service.PatientRoutineService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PatientRoutineServiceImpl implements PatientRoutineService {

    private final PatientRoutineRepository patientRoutineRepository;
    private final PatientRepository patientRepository;

    public PatientRoutineServiceImpl(PatientRoutineRepository patientRoutineRepository,
                                     PatientRepository patientRepository) {
        this.patientRoutineRepository = patientRoutineRepository;
        this.patientRepository = patientRepository;
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
    public PatientRoutine update(String id, PatientRoutine entity) {
        if (id == null || id.isBlank()) {
            throw new RuntimeException("PatientRoutine id must not be null.");
        }

        if (!patientRoutineRepository.existsById(id)) {
            throw new RuntimeException("PatientRoutine not found with id: " + id);
        }

        if (!patientRepository.existsById(id)) {
            throw new RuntimeException("Patient not found with id: " + id);
        }

        entity.setUserId(id);

        return patientRoutineRepository.save(entity);
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