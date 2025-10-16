package com.qanunqapisi.service;

import com.qanunqapisi.dto.request.test.SubmitTestRequest;
import com.qanunqapisi.dto.response.test.TestAttemptResponse;
import com.qanunqapisi.dto.response.test.TestResultResponse;

import java.util.List;
import java.util.UUID;

public interface TestAttemptService {
    TestAttemptResponse startTest(UUID testId);
    
    TestResultResponse submitTest(UUID testId, SubmitTestRequest request);
    
    List<TestAttemptResponse> getTestAttempts(UUID testId);
    
    TestResultResponse getAttemptResults(UUID attemptId);
}
