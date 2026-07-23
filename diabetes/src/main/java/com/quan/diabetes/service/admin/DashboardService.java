package com.quan.diabetes.service.admin;

import com.quan.diabetes.dto.admin.DashboardStatsDTO;

public interface DashboardService {
    /**
     * Retrieves aggregated statistics for the admin dashboard.
     * @return DashboardStatsDTO containing all necessary counts and metrics.
     */
    DashboardStatsDTO getDashboardStats();
}
