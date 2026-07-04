package com.quan.diabetes.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.quan.diabetes.entity.ExamSymptom;
import com.quan.diabetes.entity.ExamSymptomId;
import org.springframework.stereotype.Repository;

@Repository
public interface ExamSymptomRepository extends JpaRepository<ExamSymptom, ExamSymptomId> {
    void deleteById_ClinicalExamId(String clinicalExamId);

    @org.springframework.data.jpa.repository.Query("SELECT sc.symptomName FROM ExamSymptom es JOIN es.symptom sc WHERE es.id.clinicalExamId = :clinicalExamId")
    java.util.List<String> findSymptomNamesByClinicalExamId(@org.springframework.data.repository.query.Param("clinicalExamId") String clinicalExamId);
}

