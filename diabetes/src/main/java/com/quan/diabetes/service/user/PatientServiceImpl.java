package com.quan.diabetes.service.user;

import com.quan.diabetes.entity.Patient;
import com.quan.diabetes.repository.PatientRepository;
import com.quan.diabetes.service.user.PatientService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PatientServiceImpl implements PatientService {

    private final PatientRepository patientRepository;

    public PatientServiceImpl(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    @Override
    public List<Patient> findAll() {
        return patientRepository.findAll();
    }

    @Override
    public Optional<Patient> findById(String id) {
        return patientRepository.findById(id);
    }

    @Override
    public Patient create(Patient entity) {
        return patientRepository.save(entity);
    }

    @Override
    public Patient update(String id, Patient entity) {
        if (!patientRepository.existsById(id)) {
            throw new EntityNotFoundException("Patient not found with id: " + id);
        }
        return patientRepository.save(entity);
    }

    @Override
    public boolean existsById(String id) {
        return patientRepository.existsById(id);
    }
}
