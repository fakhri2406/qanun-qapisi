package com.qanunqapisi.service;

import com.qanunqapisi.dto.response.admin.DashboardStatsResponse;

/**
 * Service interface for admin dashboard statistics.
 * Provides aggregated data about users, tests, and attempts.
 */
public interface DashboardService {
    /**
     * Retrieves comprehensive dashboard statistics.
     * Includes counts of total users, verified users, premium users,
     * draft and published tests, and total test attempts.
     *
     * @return the dashboard statistics
     */
    DashboardStatsResponse getStats();
}
