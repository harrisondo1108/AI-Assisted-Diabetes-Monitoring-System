package com.quan.diabetes.repository;

import com.quan.diabetes.entity.PatientType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PatientTypeRepository extends JpaRepository<PatientType, Integer> {
}
