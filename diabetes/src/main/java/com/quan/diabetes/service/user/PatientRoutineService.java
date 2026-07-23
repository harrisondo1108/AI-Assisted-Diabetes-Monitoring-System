package com.quan.diabetes.service.user;

import com.quan.diabetes.entity.PatientRoutine;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public interface PatientRoutineService {
    Optional<PatientRoutine> findById(String id);
    PatientRoutine create(PatientRoutine entity);
    PatientRoutine update(String id, PatientRoutine entity);
    void deleteById(String id);
    boolean existsById(String id);
}