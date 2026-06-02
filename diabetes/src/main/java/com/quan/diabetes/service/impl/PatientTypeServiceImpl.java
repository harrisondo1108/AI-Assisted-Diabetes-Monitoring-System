package com.quan.diabetes.service.impl;

import com.quan.diabetes.entity.PatientType;
import com.quan.diabetes.repository.PatientTypeRepository;
import com.quan.diabetes.service.PatientTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class PatientTypeServiceImpl implements PatientTypeService {

    @Autowired
    private PatientTypeRepository patientTypeRepository;

    @Override
    public List<PatientType> findAll() {
        return patientTypeRepository.findAll();
    }

    @Override
    public Optional<PatientType> findById(Integer id) {
        return patientTypeRepository.findById(id);
    }

    @Override
    public PatientType create(PatientType entity) {
        return patientTypeRepository.save(entity);
    }

    @Override
    public PatientType update(Integer id, PatientType entity) {
        if (!patientTypeRepository.existsById(id)) {
            throw new RuntimeException("PatientType not found with id: " + id);
        }
        entity.setPatientTypeId(id);
        return patientTypeRepository.save(entity);
    }

    @Override
    public void deleteById(Integer id) {
        patientTypeRepository.deleteById(id);
    }

    @Override
    public boolean existsById(Integer id) {
        return patientTypeRepository.existsById(id);
    }
}