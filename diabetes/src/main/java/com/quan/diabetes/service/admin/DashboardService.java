package com.quan.diabetes.service.admin;

import com.quan.diabetes.dto.admin.DashboardStatsDTO;

public interface DashboardService {
    /**
     * Retrieves aggregated statistics for the admin dashboard.
     * @return DashboardStatsDTO containing all necessary counts and metrics.
     */
    DashboardStatsDTO getDashboardStats();

    // Returns a list of 7 integers representing counts for the last 7 days
    // (index 0 = startDate (today - 6), index 6 = today)
    java.util.List<Integer> getConversationCountsLast7Days();

    java.util.List<Integer> getReminderCountsLast7Days();
}
