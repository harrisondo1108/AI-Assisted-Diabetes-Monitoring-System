package com.quan.diabetes.monitoring.service.impl;

import com.quan.diabetes.monitoring.dto.AiPatientAccessLogDto;
import com.quan.diabetes.monitoring.entity.AiPatientAccessLog;
import com.quan.diabetes.monitoring.repository.AiPatientAccessLogRepository;
import com.quan.diabetes.monitoring.service.AiMonitoringService;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class AiMonitoringServiceImpl implements AiMonitoringService {

    @Autowired
    private AiPatientAccessLogRepository aiPatientAccessLogRepository;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public AiPatientAccessLog logPatientAccess(AiPatientAccessLog log) {
        return aiPatientAccessLogRepository.save(log);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AiPatientAccessLogDto> getPatientAccessLogs(String patientId, String dataType, LocalDateTime fromDate, LocalDateTime toDate, Pageable pageable) {
        Specification<AiPatientAccessLog> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (patientId != null && !patientId.trim().isEmpty()) {
                predicates.add(cb.like(root.get("patientId"), "%" + patientId.trim() + "%"));
            }
            if (dataType != null && !dataType.trim().isEmpty()) {
                predicates.add(cb.equal(root.get("dataType"), dataType.trim()));
            }
            if (fromDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("accessedAt"), fromDate));
            }
            if (toDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("accessedAt"), toDate));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<AiPatientAccessLog> page = aiPatientAccessLogRepository.findAll(spec, pageable);

        return page.map(log -> new AiPatientAccessLogDto(
                log.getId(),
                log.getQueryLogId(),
                log.getPatientId(),
                log.getDataType(),
                log.getAccessedAt(),
                null
        ));
    }

    @Override
    @Transactional(readOnly = true)
    public Double getAverageLatencyMs() {
        return 0.0;
    }

    @Override
    @Transactional(readOnly = true)
    public Long countDistinctPatientsToday() {
        Long count = aiPatientAccessLogRepository.countDistinctPatientsToday(LocalDate.now().atStartOfDay());
        return count != null ? count : 0L;
    }

    private final AtomicBoolean aiEnabled = new AtomicBoolean(true);

    @Override
    public boolean isAiEnabled() {
        return aiEnabled.get();
    }

    @Override
    public void setAiEnabled(boolean enabled) {
        this.aiEnabled.set(enabled);
    }
}
