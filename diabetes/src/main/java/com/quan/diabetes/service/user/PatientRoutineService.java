package com.quan.diabetes.service.user;

import com.quan.diabetes.entity.PatientRoutine;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
@Service
public interface PatientRoutineService {
    List<PatientRoutine> findAll();
    Optional<PatientRoutine> findById(String id);
    PatientRoutine create(PatientRoutine entity);
    PatientRoutine update(String id, PatientRoutine entity);
    void deleteById(String id);
    boolean existsById(String id);
}