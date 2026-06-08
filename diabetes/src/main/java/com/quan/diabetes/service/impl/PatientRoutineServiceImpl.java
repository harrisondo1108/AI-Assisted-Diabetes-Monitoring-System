package com.quan.diabetes.service.impl;

import com.quan.diabetes.entity.Patient;
import com.quan.diabetes.entity.PatientRoutine;
import com.quan.diabetes.repository.PatientRepository;
import com.quan.diabetes.repository.PatientRoutineRepository;
import com.quan.diabetes.service.PatientRoutineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class PatientRoutineServiceImpl implements PatientRoutineService {


    private PatientRoutineRepository patientRoutineRepository;

    private PatientRepository patientRepository;

    public PatientRoutineServiceImpl(PatientRoutineRepository patientRoutineRepository, PatientRepository patientRepository) {
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
        return patientRoutineRepository.save(entity);
    }

    @Override
    public PatientRoutine update(String id, PatientRoutine entity) {
        if (!patientRoutineRepository.existsById(id)) {
            throw new RuntimeException("PatientRoutine not found with id: " + id);
        }
        Patient patient = patientRepository.findById(id).get();
        entity.setPatient(patient);
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