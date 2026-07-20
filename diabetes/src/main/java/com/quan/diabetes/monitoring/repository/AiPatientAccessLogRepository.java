package com.quan.diabetes.monitoring.repository;

import com.quan.diabetes.monitoring.entity.AiPatientAccessLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface AiPatientAccessLogRepository extends JpaRepository<AiPatientAccessLog, Long>, JpaSpecificationExecutor<AiPatientAccessLog> {

    @org.springframework.data.jpa.repository.Query("SELECT COUNT(DISTINCT a.patientId) FROM AiPatientAccessLog a WHERE a.accessedAt >= :startOfDay AND a.patientId != 'UNKNOWN'")
    Long countDistinctPatientsToday(@org.springframework.data.repository.query.Param("startOfDay") java.time.LocalDateTime startOfDay);

    @org.springframework.data.jpa.repository.Query("SELECT AVG(a.latencyMs) FROM AiPatientAccessLog a WHERE a.latencyMs IS NOT NULL")
    Double getAverageLatencyMs();
}
