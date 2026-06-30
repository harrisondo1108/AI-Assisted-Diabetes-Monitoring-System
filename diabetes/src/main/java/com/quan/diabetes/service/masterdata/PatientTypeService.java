package com.quan.diabetes.service.masterdata;

import com.quan.diabetes.entity.PatientType;
import java.util.List;
import java.util.Optional;

public interface PatientTypeService {
    List<PatientType> findAll();
    Optional<PatientType> findById(Integer id);
    PatientType create(PatientType entity);
    PatientType update(Integer id, PatientType entity);
    void deleteById(Integer id);
    boolean existsById(Integer id);
}