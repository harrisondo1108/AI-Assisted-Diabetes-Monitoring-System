package com.quan.diabetes.repository;

import com.quan.diabetes.entity.PatientType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PatientTypeRepository extends JpaRepository<PatientType, Integer> {
}
