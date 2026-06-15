package com.quan.diabetes.repository;

import com.quan.diabetes.entity.PatientRoutine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PatientRoutineRepository extends JpaRepository<PatientRoutine, String> {

	// Tìm PatientRoutine theo patientId (UserID)
	Optional<PatientRoutine> findByUserId(String userId);

	// Hỗ trợ tìm bằng thuộc tính liên kết Patient nếu cần
	Optional<PatientRoutine> findByPatient_UserId(String userId);
}
