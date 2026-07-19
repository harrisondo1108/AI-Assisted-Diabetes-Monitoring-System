package com.quan.diabetes.monitoring.service;

import com.quan.diabetes.monitoring.dto.AiPatientAccessLogDto;
import com.quan.diabetes.monitoring.entity.AiPatientAccessLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.LocalDateTime;

public interface AiMonitoringService {
    AiPatientAccessLog logPatientAccess(AiPatientAccessLog log);
    Page<AiPatientAccessLogDto> getPatientAccessLogs(String patientId, String dataType, LocalDateTime fromDate, LocalDateTime toDate, Pageable pageable);
    Double getAverageLatencyMs();
    Long countDistinctPatientsToday();
    boolean isAiEnabled();
    void setAiEnabled(boolean enabled);
}
