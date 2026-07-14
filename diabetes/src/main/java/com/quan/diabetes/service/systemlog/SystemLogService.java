package com.quan.diabetes.service.systemlog;

import com.quan.diabetes.entity.SystemLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.Optional;

public interface SystemLogService {
    void saveLog(String accountId, String action, String entityName, String entityId,
                 String description, String oldValue, String newValue, String status);

    void saveLogWithObject(String accountId, String action, String entityName, String entityId,
                           String description, Object oldValue, Object newValue, String status);

    Page<SystemLog> getLogs(String keyword, String role, String action, LocalDate fromDate, LocalDate toDate, Pageable pageable);

    Optional<SystemLog> findById(Integer id);
}
