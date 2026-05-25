package com.quan.diabetes.service;

import com.quan.diabetes.entity.Patient;
import java.util.List;
import java.util.Optional;

public interface PatientService {

    public List<Patient> findAll();

    public Optional<Patient> findById(String id);

    public Patient create(Patient entity);

    public Patient update(String id, Patient entity);

    public void deleteById(String id);

    public boolean existsById(String id);
}
