package com.quan.diabetes.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.quan.diabetes.entity.ExamSymptom;
import com.quan.diabetes.entity.ExamSymptomId;
import org.springframework.stereotype.Repository;

@Repository
public interface ExamSymptomRepository extends JpaRepository<ExamSymptom, ExamSymptomId> {
    void deleteById_ClinicalExamId(String clinicalExamId);
}

