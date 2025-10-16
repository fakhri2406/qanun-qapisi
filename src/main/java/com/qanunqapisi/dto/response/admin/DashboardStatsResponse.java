package com.qanunqapisi.dto.response.admin;

public record DashboardStatsResponse(
    long totalTests,
    long draftTests,
    long publishedTests,
    long totalUsers,
    long premiumUsers,
    long totalAttempts
) {
}
