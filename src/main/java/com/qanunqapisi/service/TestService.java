package com.qanunqapisi.service;

import com.qanunqapisi.dto.request.test.CreateTestRequest;
import com.qanunqapisi.dto.request.test.UpdateTestRequest;
import com.qanunqapisi.dto.response.test.TestDetailResponse;
import com.qanunqapisi.dto.response.test.TestResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface TestService {
    TestDetailResponse createTest(CreateTestRequest request);

    TestDetailResponse updateTest(UUID testId, UpdateTestRequest request);

    void deleteTest(UUID testId);

    TestDetailResponse publishTest(UUID testId);

    TestDetailResponse getTest(UUID testId);

    Page<TestResponse> listTests(String status, Boolean isPremium, Pageable pageable);

    Page<TestResponse> listPublishedTestsForUser(Pageable pageable);

    TestDetailResponse getTestForUser(UUID testId);

    void recalculateTestScores(UUID testId);

    String uploadQuestionImage(UUID questionId, MultipartFile file);

    void deleteQuestionImage(UUID questionId);
}
