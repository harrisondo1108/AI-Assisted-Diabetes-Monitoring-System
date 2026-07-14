package com.quan.diabetes.service.systemlog.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quan.diabetes.entity.SystemLog;
import com.quan.diabetes.entity.User;
import com.quan.diabetes.repository.SystemLogRepository;
import com.quan.diabetes.repository.UserRepository;
import com.quan.diabetes.service.systemlog.SystemLogService;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class SystemLogServiceImpl implements SystemLogService {

    private static final Logger logger = LoggerFactory.getLogger(SystemLogServiceImpl.class);

    @Autowired
    private SystemLogRepository systemLogRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void saveLog(String accountId, String action, String entityName, String entityId,
            String description, String oldValue, String newValue, String status) {
        try {
            SystemLog log = new SystemLog();

            if (accountId != null) {
                User user = userRepository.findById(accountId).orElse(null);
                log.setAccount(user);
            } else {
                org.springframework.security.core.Authentication authentication = org.springframework.security.core.context.SecurityContextHolder
                        .getContext().getAuthentication();
                if (authentication != null && authentication.isAuthenticated()
                        && !authentication.getName().equals("anonymousUser")) {
                    String phone = authentication.getName();
                    User user = userRepository.findByPhoneNumber(phone).orElse(null);
                    log.setAccount(user);
                }
            }

            log.setAction(action);
            log.setEntityName(entityName);
            log.setEntityId(entityId);
            log.setDescription(description);
            log.setOldValue(oldValue);
            log.setNewValue(newValue);
            log.setStatus(status);
            log.setCreatedAt(LocalDateTime.now());

            systemLogRepository.save(log);
        } catch (Exception e) {
            // Log the exception but don't prevent the main transaction from committing
            // if we consider logging failure non-critical, or let it propagate if it is
            // critical.
            // As per requirement: "nếu việc ghi log thất bại thì cần cân nhắc xử lý theo
            // yêu cầu của hệ thống"
            // We'll throw it to rollback the transaction as they are in the same
            // transaction by default.
            logger.error("Error while saving system log", e);
            throw e;
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void saveLogWithObject(String accountId, String action, String entityName, String entityId,
            String description, Object oldValueObj, Object newValueObj, String status) {
        String oldValue = null;
        String newValue = null;

        try {
            if (oldValueObj != null) {
                oldValue = objectMapper.writeValueAsString(oldValueObj);
            }
            if (newValueObj != null) {
                newValue = objectMapper.writeValueAsString(newValueObj);
            }
        } catch (JsonProcessingException e) {
            logger.error("Error parsing log object to JSON", e);
        }

        saveLog(accountId, action, entityName, entityId, description, oldValue, newValue, status);
    }

    @Override
    public org.springframework.data.domain.Page<SystemLog> getLogs(String keyword, String role, String action,
            java.time.LocalDate fromDate, java.time.LocalDate toDate,
            org.springframework.data.domain.Pageable pageable) {
        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
            throw new IllegalArgumentException("Từ ngày không được lớn hơn Đến ngày.");
        }
        if (keyword == null || keyword.trim().isEmpty()) {
            return systemLogRepository.findAll(com.quan.diabetes.repository.specification.SystemLogSpecification
                    .filterLogs(null, role, action, fromDate, toDate), pageable);
        }

        List<SystemLog> allLogs = systemLogRepository.findAll(
                com.quan.diabetes.repository.specification.SystemLogSpecification.filterLogs(null, role, action,
                        fromDate, toDate),
                org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC,
                        "createdAt"));

        List<SystemLog> filteredLogs = allLogs.stream().filter(log -> {
            boolean matchesAccountId = log.getAccount() != null
                    && com.quan.diabetes.util.SearchUtil.matches(log.getAccount().getUserId(), keyword);
            boolean matchesPhone = log.getAccount() != null
                    && com.quan.diabetes.util.SearchUtil.matches(log.getAccount().getPhoneNumber(), keyword);
            boolean matchesProfileName = log.getAccount() != null && log.getAccount().getProfile() != null
                    && com.quan.diabetes.util.SearchUtil.matches(log.getAccount().getProfile().getFullName(), keyword);
            boolean matchesPatientName = log.getAccount() != null && log.getAccount().getPatient() != null
                    && com.quan.diabetes.util.SearchUtil.matches(log.getAccount().getPatient().getFullName(), keyword);
            return matchesAccountId || matchesPhone || matchesProfileName || matchesPatientName;
        }).collect(java.util.stream.Collectors.toList());

        int total = filteredLogs.size();
        int from = (int) pageable.getOffset();
        if (from >= total) {
            return new org.springframework.data.domain.PageImpl<>(new java.util.ArrayList<>(), pageable, total);
        }
        int to = Math.min(from + pageable.getPageSize(), total);
        return new org.springframework.data.domain.PageImpl<>(filteredLogs.subList(from, to), pageable, total);
    }

    @Override
    public java.util.Optional<SystemLog> findById(Integer id) {
        return systemLogRepository.findById(id);
    }
}
