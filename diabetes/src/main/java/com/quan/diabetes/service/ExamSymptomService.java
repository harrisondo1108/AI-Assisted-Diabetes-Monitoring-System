package com.quan.diabetes.service;

import com.quan.diabetes.entity.ExamSymptom;
import com.quan.diabetes.entity.ExamSymptomId;
import java.util.List;
import java.util.Optional;

public interface ExamSymptomService {

    public List<ExamSymptom> findAll();

    public Optional<ExamSymptom> findById(ExamSymptomId id);

    public ExamSymptom create(ExamSymptom entity);

    public ExamSymptom update(ExamSymptomId id, ExamSymptom entity);

    public void deleteById(ExamSymptomId id);

    public boolean existsById(ExamSymptomId id);
}
