package com.qanunqapisi.service.impl;

import com.qanunqapisi.dto.response.admin.DashboardStatsResponse;
import com.qanunqapisi.repository.TestAttemptRepository;
import com.qanunqapisi.repository.TestRepository;
import com.qanunqapisi.repository.UserRepository;
import com.qanunqapisi.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {
    private final TestRepository testRepository;
    private final UserRepository userRepository;
    private final TestAttemptRepository testAttemptRepository;

    @Override
    public DashboardStatsResponse getStats() {
        long totalTests = testRepository.count();
        long draftTests = testRepository.countByStatus("DRAFT");
        long publishedTests = testRepository.countByStatus("PUBLISHED");
        long totalUsers = userRepository.count();
        long premiumUsers = userRepository.countByIsPremium(true);
        long totalAttempts = testAttemptRepository.count();

        return new DashboardStatsResponse(
            totalTests,
            draftTests,
            publishedTests,
            totalUsers,
            premiumUsers,
            totalAttempts
        );
    }
}
