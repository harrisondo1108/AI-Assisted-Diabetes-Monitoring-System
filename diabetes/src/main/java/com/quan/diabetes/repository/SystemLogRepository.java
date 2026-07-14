package com.quan.diabetes.repository;

import com.quan.diabetes.entity.SystemLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface SystemLogRepository extends JpaRepository<SystemLog, Integer>, JpaSpecificationExecutor<SystemLog> {
}
