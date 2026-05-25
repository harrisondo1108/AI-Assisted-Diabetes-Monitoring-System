package com.quan.diabetes.service.impl;

import com.quan.diabetes.entity.ClinicalExamination;
import com.quan.diabetes.repository.ClinicalExaminationRepository;
import com.quan.diabetes.service.ClinicalExaminationService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ClinicalExaminationServiceImpl implements ClinicalExaminationService {

    private final ClinicalExaminationRepository clinicalExaminationRepository;

    public ClinicalExaminationServiceImpl(ClinicalExaminationRepository clinicalExaminationRepository) {
        this.clinicalExaminationRepository = clinicalExaminationRepository;
    }

    @Override
    public List<ClinicalExamination> findAll() {
        return clinicalExaminationRepository.findAll();
    }

    @Override
    public Optional<ClinicalExamination> findById(String id) {
        return clinicalExaminationRepository.findById(id);
    }

    @Override
    public ClinicalExamination create(ClinicalExamination entity) {
        return clinicalExaminationRepository.save(entity);
    }

    @Override
    public ClinicalExamination update(String id, ClinicalExamination entity) {
        if (!clinicalExaminationRepository.existsById(id)) {
            throw new EntityNotFoundException("ClinicalExamination not found with id: " + id);
        }
        return clinicalExaminationRepository.save(entity);
    }

    @Override
    public void deleteById(String id) {
        if (!clinicalExaminationRepository.existsById(id)) {
            throw new EntityNotFoundException("ClinicalExamination not found with id: " + id);
        }
        clinicalExaminationRepository.deleteById(id);
    }

    @Override
    public boolean existsById(String id) {
        return clinicalExaminationRepository.existsById(id);
    }
}
