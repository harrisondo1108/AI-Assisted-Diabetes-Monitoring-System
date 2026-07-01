package com.quan.diabetes.repository;

import com.quan.diabetes.entity.IndicatorThreshold;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IndicatorThresholdRepository extends JpaRepository<IndicatorThreshold, Integer> {
    List<IndicatorThreshold> findByLabTest_LabTestId(String labTestId);
    Optional<IndicatorThreshold> findByLabTest_LabTestIdAndPatientType_PatientTypeId(String labTestId, Integer patientTypeId);
}
