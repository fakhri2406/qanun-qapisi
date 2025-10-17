package com.qanunqapisi.service;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import com.qanunqapisi.dto.request.test.CreateTestRequest;
import com.qanunqapisi.dto.request.test.UpdateTestRequest;
import com.qanunqapisi.dto.response.test.TestDetailResponse;
import com.qanunqapisi.dto.response.test.TestResponse;

/**
 * Service interface for test management operations.
 * Handles test creation, updates, publishing, and question image management.
 */
public interface TestService {
    /**
     * Creates a new test with optional questions and answers.
     *
     * @param request the test creation request containing test details
     * @return the created test details
     * @throws NoSuchElementException if authenticated user not found
     */
    TestDetailResponse createTest(CreateTestRequest request);

    /**
     * Updates an existing test and its questions.
     *
     * @param testId the ID of the test to update
     * @param request the test update request containing new details
     * @return the updated test details
     * @throws NoSuchElementException if test not found
     */
    TestDetailResponse updateTest(UUID testId, UpdateTestRequest request);

    /**
     * Deletes a test and all associated questions, answers, and attempts.
     *
     * @param testId the ID of the test to delete
     * @throws NoSuchElementException if test not found
     */
    void deleteTest(UUID testId);

    /**
     * Publishes a test, making it available to users.
     *
     * @param testId the ID of the test to publish
     * @return the published test details
     * @throws NoSuchElementException if test not found
     * @throws IllegalStateException if test is already published or has no questions
     */
    TestDetailResponse publishTest(UUID testId);

    /**
     * Retrieves detailed information about a specific test (admin view).
     *
     * @param testId the ID of the test to retrieve
     * @return the test details including all questions and answers
     * @throws NoSuchElementException if test not found
     */
    TestDetailResponse getTest(UUID testId);

    /**
     * Lists tests with optional filtering by status and premium flag (admin view).
     *
     * @param status optional filter by test status (DRAFT, PUBLISHED)
     * @param isPremium optional filter by premium flag
     * @param pageable pagination information
     * @return page of tests matching the filters
     */
    Page<TestResponse> listTests(String status, Boolean isPremium, Pageable pageable);

    /**
     * Lists published tests available to the authenticated user.
     * Filters premium tests based on user's subscription status.
     *
     * @param pageable pagination information
     * @return page of published tests accessible to the user
     * @throws NoSuchElementException if authenticated user not found
     */
    Page<TestResponse> listPublishedTestsForUser(Pageable pageable);

    /**
     * Retrieves a test for the authenticated user (customer view).
     * Validates user's access to premium tests.
     *
     * @param testId the ID of the test to retrieve
     * @return the test details
     * @throws NoSuchElementException if test or user not found
     * @throws IllegalStateException if user doesn't have access to premium test
     */
    TestDetailResponse getTestForUser(UUID testId);

    /**
     * Recalculates total possible score for a test based on its questions.
     *
     * @param testId the ID of the test to recalculate
     * @throws NoSuchElementException if test not found
     */
    void recalculateTestScores(UUID testId);

    /**
     * Uploads an image for a specific question.
     *
     * @param questionId the ID of the question
     * @param file the image file to upload
     * @return the URL of the uploaded image
     * @throws NoSuchElementException if question not found
     * @throws ImageUploadException if image upload fails
     */
    String uploadQuestionImage(UUID questionId, MultipartFile file);

    /**
     * Deletes the image associated with a question.
     *
     * @param questionId the ID of the question
     * @throws NoSuchElementException if question not found
     * @throws ImageUploadException if image deletion fails
     */
    void deleteQuestionImage(UUID questionId);
}
