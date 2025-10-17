package com.qanunqapisi.service;

import java.util.List;
import java.util.UUID;

import com.qanunqapisi.dto.request.test.SubmitTestRequest;
import com.qanunqapisi.dto.response.test.TestAttemptResponse;
import com.qanunqapisi.dto.response.test.TestResultResponse;

/**
 * Service interface for test attempt and submission operations.
 * Handles starting tests, submitting answers, and retrieving attempt results.
 */
public interface TestAttemptService {
    /**
     * Starts a new test attempt for the authenticated user.
     * Creates an IN_PROGRESS attempt record.
     *
     * @param testId the ID of the test to start
     * @return the created test attempt details
     * @throws NoSuchElementException if test or user not found
     * @throws IllegalStateException if test is not published or user lacks access
     */
    TestAttemptResponse startTest(UUID testId);

    /**
     * Submits answers for a test attempt and calculates the score.
     * Marks the attempt as COMPLETED and scores all answers.
     *
     * @param testId the ID of the test being submitted
     * @param request the submission request containing all answers
     * @return the test results with scores and correct answers
     * @throws NoSuchElementException if test, user, or attempt not found
     */
    TestResultResponse submitTest(UUID testId, SubmitTestRequest request);

    /**
     * Retrieves all test attempts by the authenticated user for a specific test.
     *
     * @param testId the ID of the test
     * @return list of all attempts for the test by the user
     * @throws NoSuchElementException if test or user not found
     */
    List<TestAttemptResponse> getTestAttempts(UUID testId);

    /**
     * Retrieves detailed results of a completed test attempt.
     * Shows questions, user answers, correct answers, and scores.
     *
     * @param attemptId the ID of the test attempt
     * @return the detailed test results
     * @throws NoSuchElementException if attempt not found
     * @throws IllegalStateException if attempt is not completed
     */
    TestResultResponse getAttemptResults(UUID attemptId);
}
