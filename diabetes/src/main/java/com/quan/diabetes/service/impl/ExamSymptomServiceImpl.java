package com.quan.diabetes.service.impl;

import com.quan.diabetes.entity.ExamSymptom;
import com.quan.diabetes.entity.ExamSymptomId;
import com.quan.diabetes.repository.ExamSymptomRepository;
import com.quan.diabetes.service.ExamSymptomService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ExamSymptomServiceImpl implements ExamSymptomService {

    private final ExamSymptomRepository examSymptomRepository;

    public ExamSymptomServiceImpl(ExamSymptomRepository examSymptomRepository) {
        this.examSymptomRepository = examSymptomRepository;
    }

    @Override
    public List<ExamSymptom> findAll() {
        return examSymptomRepository.findAll();
    }

    @Override
    public Optional<ExamSymptom> findById(ExamSymptomId id) {
        return examSymptomRepository.findById(id);
    }

    @Override
    public ExamSymptom create(ExamSymptom entity) {
        return examSymptomRepository.save(entity);
    }

    @Override
    public ExamSymptom update(ExamSymptomId id, ExamSymptom entity) {
        if (!examSymptomRepository.existsById(id)) {
            throw new EntityNotFoundException("ExamSymptom not found with id: " + id);
        }
        return examSymptomRepository.save(entity);
    }

    @Override
    public void deleteById(ExamSymptomId id) {
        if (!examSymptomRepository.existsById(id)) {
            throw new EntityNotFoundException("ExamSymptom not found with id: " + id);
        }
        examSymptomRepository.deleteById(id);
    }

    @Override
    public boolean existsById(ExamSymptomId id) {
        return examSymptomRepository.existsById(id);
    }
}
