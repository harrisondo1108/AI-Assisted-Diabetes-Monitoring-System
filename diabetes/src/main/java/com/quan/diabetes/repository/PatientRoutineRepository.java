package com.quan.diabetes.repository;

import com.quan.diabetes.entity.PatientRoutine;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PatientRoutineRepository extends JpaRepository<PatientRoutine, String> {
}
