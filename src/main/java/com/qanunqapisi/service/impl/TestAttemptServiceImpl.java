package com.qanunqapisi.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.qanunqapisi.domain.Answer;
import com.qanunqapisi.domain.Question;
import com.qanunqapisi.domain.Role;
import com.qanunqapisi.domain.Test;
import com.qanunqapisi.domain.TestAttempt;
import com.qanunqapisi.domain.User;
import com.qanunqapisi.domain.UserAnswer;
import com.qanunqapisi.dto.request.test.SubmitAnswerRequest;
import com.qanunqapisi.dto.request.test.SubmitTestRequest;
import com.qanunqapisi.dto.response.test.AnswerResponse;
import com.qanunqapisi.dto.response.test.QuestionResultResponse;
import com.qanunqapisi.dto.response.test.TestAttemptResponse;
import com.qanunqapisi.dto.response.test.TestResultResponse;
import com.qanunqapisi.repository.AnswerRepository;
import com.qanunqapisi.repository.QuestionRepository;
import com.qanunqapisi.repository.RoleRepository;
import com.qanunqapisi.repository.TestAttemptRepository;
import com.qanunqapisi.repository.TestRepository;
import com.qanunqapisi.repository.UserAnswerRepository;
import com.qanunqapisi.repository.UserRepository;
import com.qanunqapisi.service.TestAttemptService;
import static com.qanunqapisi.util.ErrorMessages.ATTEMPT_NOT_FOUND;
import static com.qanunqapisi.util.ErrorMessages.ATTEMPT_NOT_IN_PROGRESS;
import static com.qanunqapisi.util.ErrorMessages.CANNOT_START_PREMIUM_TEST;
import static com.qanunqapisi.util.ErrorMessages.ROLE_NOT_FOUND;
import static com.qanunqapisi.util.ErrorMessages.TEST_NOT_FOUND;
import static com.qanunqapisi.util.ErrorMessages.TEST_NOT_PUBLISHED;
import static com.qanunqapisi.util.ErrorMessages.USER_NOT_FOUND;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class TestAttemptServiceImpl implements TestAttemptService {
    private static final String IN_PROGRESS = "IN_PROGRESS";
    private static final String COMPLETED = "COMPLETED";

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

        if (Boolean.TRUE.equals(test.getIsPremium()) && !Boolean.TRUE.equals(user.getIsPremium()) && !"ADMIN".equals(role.getTitle())) {
            throw new IllegalStateException(CANNOT_START_PREMIUM_TEST);
        }

        // Create new attempt
        TestAttempt attempt = TestAttempt.builder()
            .userId(user.getId())
            .testId(testId)
            .totalScore(0)
            .maxPossibleScore(test.getTotalPossibleScore())
            .status(IN_PROGRESS)
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
            .findByUserIdAndTestIdAndStatus(user.getId(), testId, IN_PROGRESS)
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
        attempt.setStatus(COMPLETED);
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
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public TestResultResponse getAttemptResults(UUID attemptId) {
        TestAttempt attempt = testAttemptRepository.findById(attemptId)
            .orElseThrow(() -> new NoSuchElementException(ATTEMPT_NOT_FOUND));

        if (!COMPLETED.equals(attempt.getStatus())) {
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
        List<UUID> selectedAnswerIds = userAnswer != null ? userAnswer.selectedAnswerIds() : null;
        String openTextAnswer = userAnswer != null ? userAnswer.openTextAnswer() : null;

        boolean isCorrect = evaluateAnswer(question, selectedAnswerIds, openTextAnswer);
        int scoreEarned = isCorrect ? question.getScore() : 0;

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

    private boolean evaluateAnswer(Question question, List<UUID> selectedAnswerIds, String openTextAnswer) {
        if ("CLOSED_SINGLE".equals(question.getQuestionType())) {
            return evaluateSingleChoice(question, selectedAnswerIds);
        } else if ("CLOSED_MULTIPLE".equals(question.getQuestionType())) {
            return evaluateMultipleChoice(question, selectedAnswerIds);
        } else if ("OPEN_TEXT".equals(question.getQuestionType())) {
            return evaluateOpenText(question, openTextAnswer);
        }
        return false;
    }

    private boolean evaluateSingleChoice(Question question, List<UUID> selectedAnswerIds) {
        if (selectedAnswerIds == null || selectedAnswerIds.size() != 1) {
            return false;
        }
        List<Answer> correctAnswers = answerRepository.findByQuestionIdAndIsCorrect(question.getId(), true);
        return correctAnswers.size() == 1 && correctAnswers.get(0).getId().equals(selectedAnswerIds.get(0));
    }

    private boolean evaluateMultipleChoice(Question question, List<UUID> selectedAnswerIds) {
        if (selectedAnswerIds == null || selectedAnswerIds.isEmpty()) {
            return false;
        }
        List<Answer> correctAnswers = answerRepository.findByQuestionIdAndIsCorrect(question.getId(), true);
        Set<UUID> correctIds = correctAnswers.stream().map(Answer::getId).collect(Collectors.toSet());
        Set<UUID> selectedIds = new HashSet<>(selectedAnswerIds);
        return correctIds.equals(selectedIds);
    }

    private boolean evaluateOpenText(Question question, String openTextAnswer) {
        if (openTextAnswer == null) {
            return false;
        }
        String normalized = normalizeText(openTextAnswer);
        return normalized.equals(question.getCorrectAnswer());
    }

    private QuestionResultResponse buildQuestionResult(Question question, UserAnswer userAnswer) {
        List<Answer> allAnswers = answerRepository.findByQuestionIdOrderByOrderIndex(question.getId());
        List<AnswerResponse> answerResponses = allAnswers.stream()
            .map(a -> new AnswerResponse(a.getId(), a.getAnswerText(), a.getIsCorrect(), a.getOrderIndex()))
            .toList();

        List<UUID> correctAnswerIds = null;
        if ("CLOSED_SINGLE".equals(question.getQuestionType()) ||
            "CLOSED_MULTIPLE".equals(question.getQuestionType())) {
            correctAnswerIds = allAnswers.stream()
                .filter(Answer::getIsCorrect)
                .map(Answer::getId)
                .toList();
        }

        List<UUID> selectedAnswerIds = userAnswer != null ? userAnswer.getSelectedAnswerIds() : null;

        return new QuestionResultResponse(
            question.getId(),
            question.getQuestionType(),
            question.getQuestionText(),
            question.getImageUrl(),
            question.getScore(),
            userAnswer != null && Boolean.TRUE.equals(userAnswer.getIsCorrect()),
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

