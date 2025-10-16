package com.qanunqapisi.service.impl;

import com.qanunqapisi.domain.*;
import com.qanunqapisi.domain.enums.AttemptStatus;
import com.qanunqapisi.dto.request.test.SubmitAnswerRequest;
import com.qanunqapisi.dto.request.test.SubmitTestRequest;
import com.qanunqapisi.dto.response.test.*;
import com.qanunqapisi.repository.*;
import com.qanunqapisi.service.TestAttemptService;
import com.qanunqapisi.util.ErrorMessages;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.qanunqapisi.util.ErrorMessages.*;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class TestAttemptServiceImpl implements TestAttemptService {
    private final TestRepository testRepository;
    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;
    private final UserRepository userRepository;
    private final TestAttemptRepository testAttemptRepository;
    private final UserAnswerRepository userAnswerRepository;
    private final RoleRepository roleRepository;

    @Override
    public TestAttemptResponse startTest(UUID testId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userRepository.findByEmail(auth.getName())
            .orElseThrow(() -> new NoSuchElementException(USER_NOT_FOUND));

        Test test = testRepository.findById(testId)
            .orElseThrow(() -> new NoSuchElementException(TEST_NOT_FOUND));

        if (!"PUBLISHED".equals(test.getStatus())) {
            throw new IllegalStateException(TEST_NOT_PUBLISHED);
        }

        // Check premium access
        Role role = roleRepository.findById(user.getRoleId())
            .orElseThrow(() -> new NoSuchElementException(ROLE_NOT_FOUND));
        
        if (test.getIsPremium() && !user.getIsPremium() && !role.getTitle().equals("ADMIN")) {
            throw new IllegalStateException(CANNOT_START_PREMIUM_TEST);
        }

        // Create new attempt
        TestAttempt attempt = TestAttempt.builder()
            .userId(user.getId())
            .testId(testId)
            .totalScore(0)
            .maxPossibleScore(test.getTotalPossibleScore())
            .status(AttemptStatus.IN_PROGRESS)
            .startedAt(LocalDateTime.now())
            .build();

        testAttemptRepository.save(attempt);

        return new TestAttemptResponse(
            attempt.getId(),
            test.getId(),
            test.getTitle(),
            attempt.getTotalScore(),
            attempt.getMaxPossibleScore(),
            attempt.getStatus(),
            attempt.getStartedAt(),
            attempt.getSubmittedAt()
        );
    }

    @Override
    public TestResultResponse submitTest(UUID testId, @Valid SubmitTestRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userRepository.findByEmail(auth.getName())
            .orElseThrow(() -> new NoSuchElementException(USER_NOT_FOUND));

        Test test = testRepository.findById(testId)
            .orElseThrow(() -> new NoSuchElementException(TEST_NOT_FOUND));

        // Find in-progress attempt
        TestAttempt attempt = testAttemptRepository
            .findByUserIdAndTestIdAndStatus(user.getId(), testId, AttemptStatus.IN_PROGRESS)
            .orElseThrow(() -> new NoSuchElementException(ATTEMPT_NOT_FOUND));

        List<Question> questions = questionRepository.findByTestIdOrderByOrderIndex(testId);
        Map<UUID, SubmitAnswerRequest> answerMap = request.answers().stream()
            .collect(Collectors.toMap(SubmitAnswerRequest::questionId, a -> a));

        List<QuestionResultResponse> questionResults = new ArrayList<>();
        int totalScore = 0;

        for (Question question : questions) {
            SubmitAnswerRequest userAnswer = answerMap.get(question.getId());
            QuestionResultResponse result = scoreQuestion(question, userAnswer, attempt.getId());
            questionResults.add(result);
            totalScore += result.scoreEarned();
        }

        // Update attempt
        attempt.setTotalScore(totalScore);
        attempt.setStatus(AttemptStatus.COMPLETED);
        attempt.setSubmittedAt(LocalDateTime.now());
        testAttemptRepository.save(attempt);

        return new TestResultResponse(
            attempt.getId(),
            test.getId(),
            test.getTitle(),
            totalScore,
            test.getTotalPossibleScore(),
            attempt.getStartedAt(),
            attempt.getSubmittedAt(),
            questionResults
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<TestAttemptResponse> getTestAttempts(UUID testId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userRepository.findByEmail(auth.getName())
            .orElseThrow(() -> new NoSuchElementException(USER_NOT_FOUND));

        Test test = testRepository.findById(testId)
            .orElseThrow(() -> new NoSuchElementException(TEST_NOT_FOUND));

        List<TestAttempt> attempts = testAttemptRepository.findByUserIdAndTestId(user.getId(), testId);

        return attempts.stream()
            .map(attempt -> new TestAttemptResponse(
                attempt.getId(),
                test.getId(),
                test.getTitle(),
                attempt.getTotalScore(),
                attempt.getMaxPossibleScore(),
                attempt.getStatus(),
                attempt.getStartedAt(),
                attempt.getSubmittedAt()
            ))
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public TestResultResponse getAttemptResults(UUID attemptId) {
        TestAttempt attempt = testAttemptRepository.findById(attemptId)
            .orElseThrow(() -> new NoSuchElementException(ATTEMPT_NOT_FOUND));

        if (attempt.getStatus() != AttemptStatus.COMPLETED) {
            throw new IllegalStateException(ATTEMPT_NOT_IN_PROGRESS);
        }

        Test test = testRepository.findById(attempt.getTestId())
            .orElseThrow(() -> new NoSuchElementException(TEST_NOT_FOUND));

        List<UserAnswer> userAnswers = userAnswerRepository.findByTestAttemptId(attemptId);
        Map<UUID, UserAnswer> answerMap = userAnswers.stream()
            .collect(Collectors.toMap(UserAnswer::getQuestionId, a -> a));

        List<Question> questions = questionRepository.findByTestIdOrderByOrderIndex(test.getId());
        List<QuestionResultResponse> questionResults = new ArrayList<>();

        for (Question question : questions) {
            UserAnswer userAnswer = answerMap.get(question.getId());
            questionResults.add(buildQuestionResult(question, userAnswer));
        }

        return new TestResultResponse(
            attempt.getId(),
            test.getId(),
            test.getTitle(),
            attempt.getTotalScore(),
            attempt.getMaxPossibleScore(),
            attempt.getStartedAt(),
            attempt.getSubmittedAt(),
            questionResults
        );
    }

    private QuestionResultResponse scoreQuestion(Question question, SubmitAnswerRequest userAnswer, UUID attemptId) {
        boolean isCorrect = false;
        int scoreEarned = 0;
        List<UUID> selectedAnswerIds = null;
        String openTextAnswer = null;

        if ("CLOSED_SINGLE".equals(question.getQuestionType())) {
            selectedAnswerIds = userAnswer != null ? userAnswer.selectedAnswerIds() : null;
            if (selectedAnswerIds != null && selectedAnswerIds.size() == 1) {
                List<Answer> correctAnswers = answerRepository.findByQuestionIdAndIsCorrect(question.getId(), true);
                isCorrect = correctAnswers.size() == 1 && correctAnswers.get(0).getId().equals(selectedAnswerIds.get(0));
            }
        } else if ("CLOSED_MULTIPLE".equals(question.getQuestionType())) {
            selectedAnswerIds = userAnswer != null ? userAnswer.selectedAnswerIds() : null;
            if (selectedAnswerIds != null && !selectedAnswerIds.isEmpty()) {
                List<Answer> correctAnswers = answerRepository.findByQuestionIdAndIsCorrect(question.getId(), true);
                Set<UUID> correctIds = correctAnswers.stream().map(Answer::getId).collect(Collectors.toSet());
                Set<UUID> selectedIds = new HashSet<>(selectedAnswerIds);
                isCorrect = correctIds.equals(selectedIds);
            }
        } else if ("OPEN_TEXT".equals(question.getQuestionType())) {
            openTextAnswer = userAnswer != null ? userAnswer.openTextAnswer() : null;
            if (openTextAnswer != null) {
                String normalized = normalizeText(openTextAnswer);
                isCorrect = normalized.equals(question.getCorrectAnswer());
            }
        }

        if (isCorrect) {
            scoreEarned = question.getScore();
        }

        // Save user answer
        UserAnswer answer = UserAnswer.builder()
            .testAttemptId(attemptId)
            .questionId(question.getId())
            .selectedAnswerIds(selectedAnswerIds)
            .openTextAnswer(openTextAnswer)
            .isCorrect(isCorrect)
            .scoreEarned(scoreEarned)
            .answeredAt(LocalDateTime.now())
            .build();
        userAnswerRepository.save(answer);

        return buildQuestionResult(question, answer);
    }

    private QuestionResultResponse buildQuestionResult(Question question, UserAnswer userAnswer) {
        List<Answer> allAnswers = answerRepository.findByQuestionIdOrderByOrderIndex(question.getId());
        List<AnswerResponse> answerResponses = allAnswers.stream()
            .map(a -> new AnswerResponse(a.getId(), a.getAnswerText(), a.getIsCorrect(), a.getOrderIndex()))
            .collect(Collectors.toList());

        List<UUID> correctAnswerIds = null;
        if ("CLOSED_SINGLE".equals(question.getQuestionType()) || 
            "CLOSED_MULTIPLE".equals(question.getQuestionType())) {
            correctAnswerIds = allAnswers.stream()
                .filter(Answer::getIsCorrect)
                .map(Answer::getId)
                .collect(Collectors.toList());
        }

        List<UUID> selectedAnswerIds = userAnswer != null ? userAnswer.getSelectedAnswerIds() : null;

        return new QuestionResultResponse(
            question.getId(),
            question.getQuestionType(),
            question.getQuestionText(),
            question.getImageUrl(),
            question.getScore(),
            userAnswer != null ? userAnswer.getIsCorrect() : false,
            userAnswer != null ? userAnswer.getScoreEarned() : 0,
            selectedAnswerIds,
            userAnswer != null ? userAnswer.getOpenTextAnswer() : null,
            correctAnswerIds,
            question.getCorrectAnswer(),
            answerResponses
        );
    }

    private String normalizeText(String text) {
        if (text == null) return "";
        return text.toLowerCase().trim();
    }
}

