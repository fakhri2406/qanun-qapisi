package com.qanunqapisi.service.impl;

import com.qanunqapisi.domain.*;
import com.qanunqapisi.dto.request.test.CreateAnswerRequest;
import com.qanunqapisi.dto.request.test.CreateQuestionRequest;
import com.qanunqapisi.dto.request.test.CreateTestRequest;
import com.qanunqapisi.dto.request.test.UpdateTestRequest;
import com.qanunqapisi.dto.response.test.*;
import com.qanunqapisi.repository.*;
import com.qanunqapisi.service.TestService;
import com.qanunqapisi.service.external.cloudinary.ImageUploadService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.qanunqapisi.util.ErrorMessages.*;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class TestServiceImpl implements TestService {
    private static final String CLOSED_SINGLE = "CLOSED_SINGLE";
    private static final String CLOSED_MULTIPLE = "CLOSED_MULTIPLE";
    private static final String OPEN_TEXT = "OPEN_TEXT";
    private static final String PUBLISHED = "PUBLISHED";
    private static final int BASE_MINUTES_PER_QUESTION = 2;

    private final TestRepository testRepository;
    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final ImageUploadService imageUploadService;

    @Override
    public TestDetailResponse createTest(@Valid CreateTestRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userRepository.findByEmail(auth.getName())
            .orElseThrow(() -> new NoSuchElementException(USER_NOT_FOUND));

        if (request.questions() != null && !request.questions().isEmpty()) {
            validateQuestionOrderIndices(request.questions());
        }

        Test test = Test.builder()
            .createdBy(user.getId())
            .title(request.title())
            .description(request.description())
            .isPremium(request.isPremium())
            .status("DRAFT")
            .questionCount(0)
            .totalPossibleScore(0)
            .build();

        testRepository.save(test);

        if (request.questions() != null && !request.questions().isEmpty()) {
            for (int i = 0; i < request.questions().size(); i++) {
                CreateQuestionRequest qReq = request.questions().get(i);
                createQuestion(test.getId(), qReq, i);
            }
        }

        recalculateTestScores(test.getId());
        return getTest(test.getId());
    }

    @Override
    public TestDetailResponse updateTest(UUID testId, @Valid UpdateTestRequest request) {
        Test test = testRepository.findById(testId)
            .orElseThrow(() -> new NoSuchElementException(TEST_NOT_FOUND));

        if (request.title() != null) {
            test.setTitle(request.title());
        }
        if (request.description() != null) {
            test.setDescription(request.description());
        }
        if (request.isPremium() != null) {
            test.setIsPremium(request.isPremium());
        }

        if (request.questions() != null) {
            validateQuestionOrderIndices(request.questions());

            questionRepository.deleteByTestId(testId);

            for (int i = 0; i < request.questions().size(); i++) {
                CreateQuestionRequest qReq = request.questions().get(i);
                createQuestion(testId, qReq, i);
            }
        }

        testRepository.save(test);
        recalculateTestScores(testId);
        return getTest(testId);
    }

    @Override
    public void deleteTest(UUID testId) {
        Test test = testRepository.findById(testId)
            .orElseThrow(() -> new NoSuchElementException(TEST_NOT_FOUND));

        testRepository.delete(test);
    }

    @Override
    public TestDetailResponse publishTest(UUID testId) {
        Test test = testRepository.findById(testId)
            .orElseThrow(() -> new NoSuchElementException(TEST_NOT_FOUND));

        if (PUBLISHED.equals(test.getStatus())) {
            throw new IllegalStateException(TEST_ALREADY_PUBLISHED);
        }

        List<Question> questions = questionRepository.findByTestIdOrderByOrderIndex(testId);

        if (questions.isEmpty()) {
            throw new IllegalStateException(TEST_MUST_HAVE_QUESTIONS);
        }

        validateTestIntegrity(test, questions);

        test.setStatus(PUBLISHED);
        test.setPublishedAt(LocalDateTime.now());
        testRepository.save(test);

        return getTest(testId);
    }

    private void validateTestIntegrity(Test test, List<Question> questions) {
        for (Question question : questions) {
            validateQuestion(question);
        }

        validateOrderIndexContinuity(questions);

        List<UUID> questionIds = questions.stream().map(Question::getId).toList();
        List<Answer> allAnswers = answerRepository.findByQuestionIdInOrderByQuestionIdAndOrderIndex(questionIds);
        Map<UUID, List<Answer>> answersByQuestion = allAnswers.stream()
            .collect(Collectors.groupingBy(Answer::getQuestionId));

        for (Question question : questions) {
            List<Answer> answers = answersByQuestion.getOrDefault(question.getId(), List.of());
            if ((CLOSED_SINGLE.equals(question.getQuestionType()) ||
                CLOSED_MULTIPLE.equals(question.getQuestionType())) && answers.isEmpty()) {
                throw new IllegalStateException(
                    "Question at position " + question.getOrderIndex() +
                        " requires answers but has none. Cannot publish test."
                );
            }

            if (!answers.isEmpty()) {
                validateAnswerOrderIndexContinuity(answers, question.getOrderIndex());
            }
        }

        if (test.getQuestionCount() != questions.size()) {
            log.warn("Test {} has incorrect question count. Expected {}, found {}. Recalculating...",
                test.getId(), questions.size(), test.getQuestionCount());
            recalculateTestScores(test.getId());
        }

        if (test.getTitle() == null || test.getTitle().isBlank()) {
            throw new IllegalStateException("Test must have a valid title");
        }
        if (test.getDescription() == null || test.getDescription().isBlank()) {
            throw new IllegalStateException("Test must have a valid description");
        }
    }

    private void validateOrderIndexContinuity(List<Question> questions) {
        if (questions.isEmpty()) return;

        List<Integer> indices = questions.stream()
            .map(Question::getOrderIndex)
            .sorted()
            .toList();

        Set<Integer> uniqueIndices = new HashSet<>(indices);
        if (uniqueIndices.size() != indices.size()) {
            throw new IllegalStateException(
                "Questions have duplicate orderIndex values. Cannot publish. " +
                    "Please edit the test to fix ordering."
            );
        }

        int expectedMax = questions.size() - 1;
        int actualMax = indices.get(indices.size() - 1);
        if (actualMax > expectedMax * 2) {
            log.warn("Test has large gaps in question orderIndex values. " +
                    "Expected max {}, found {}. This might indicate a data issue.",
                expectedMax, actualMax);
        }
    }

    private void validateAnswerOrderIndexContinuity(List<Answer> answers, int questionPosition) {
        if (answers.isEmpty()) return;

        List<Integer> indices = answers.stream()
            .map(Answer::getOrderIndex)
            .sorted()
            .toList();

        Set<Integer> uniqueIndices = new HashSet<>(indices);
        if (uniqueIndices.size() != indices.size()) {
            throw new IllegalStateException(
                "Answers in question at position " + questionPosition +
                    " have duplicate orderIndex values. Cannot publish."
            );
        }
    }

    @Override
    @Transactional(readOnly = true)
    public TestDetailResponse getTest(UUID testId) {
        Test test = testRepository.findById(testId)
            .orElseThrow(() -> new NoSuchElementException(TEST_NOT_FOUND));

        List<Question> questions = questionRepository.findByTestIdOrderByOrderIndex(testId);

        if (questions.isEmpty()) {
            return buildTestDetailResponse(test, questions, List.of());
        }

        List<UUID> questionIds = questions.stream().map(Question::getId).toList();
        List<Answer> allAnswers = answerRepository.findByQuestionIdInOrderByQuestionIdAndOrderIndex(questionIds);

        Map<UUID, List<Answer>> answersByQuestionId = allAnswers.stream()
            .collect(Collectors.groupingBy(Answer::getQuestionId));

        List<QuestionResponse> questionResponses = questions.stream()
            .map(question -> {
                List<Answer> answers = answersByQuestionId.getOrDefault(question.getId(), List.of());
                List<AnswerResponse> answerResponses = answers.stream()
                    .map(a -> new AnswerResponse(a.getId(), a.getAnswerText(), a.getIsCorrect(), a.getOrderIndex()))
                    .toList();

                return new QuestionResponse(
                    question.getId(),
                    question.getQuestionType(),
                    question.getQuestionText(),
                    question.getImageUrl(),
                    question.getScore(),
                    question.getOrderIndex(),
                    question.getCorrectAnswer(),
                    answerResponses
                );
            })
            .toList();

        return buildTestDetailResponse(test, questions, questionResponses);
    }

    private TestDetailResponse buildTestDetailResponse(Test test, List<Question> questions, List<QuestionResponse> questionResponses) {
        List<QuestionTypeCount> questionTypeCounts = calculateQuestionTypeCounts(questions);

        return new TestDetailResponse(
            test.getId(),
            test.getTitle(),
            test.getDescription(),
            test.getIsPremium(),
            test.getStatus(),
            test.getQuestionCount(),
            test.getTotalPossibleScore(),
            calculateEstimatedTime(test.getQuestionCount()),
            test.getPublishedAt(),
            questionResponses,
            questionTypeCounts,
            test.getCreatedAt(),
            test.getUpdatedAt()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TestResponse> listTests(String status, Boolean isPremium, Pageable pageable) {
        Page<Test> tests;

        if (status != null && isPremium != null) {
            tests = testRepository.findByStatusAndIsPremium(status.toUpperCase(), isPremium, pageable);
        } else if (status != null) {
            tests = testRepository.findByStatus(status.toUpperCase(), pageable);
        } else if (isPremium != null) {
            tests = testRepository.findByIsPremium(isPremium, pageable);
        } else {
            tests = testRepository.findAll(pageable);
        }

        return tests.map(test -> {
            List<Question> questions = questionRepository.findByTestIdOrderByOrderIndex(test.getId());
            List<QuestionTypeCount> questionTypeCounts = calculateQuestionTypeCounts(questions);

            return new TestResponse(
                test.getId(),
                test.getTitle(),
                test.getDescription(),
                test.getIsPremium(),
                test.getStatus(),
                test.getQuestionCount(),
                test.getTotalPossibleScore(),
                calculateEstimatedTime(test.getQuestionCount()),
                questionTypeCounts,
                test.getPublishedAt(),
                test.getCreatedAt(),
                test.getUpdatedAt()
            );
        });
    }

    @Override
    public void recalculateTestScores(UUID testId) {
        Test test = testRepository.findById(testId)
            .orElseThrow(() -> new NoSuchElementException(TEST_NOT_FOUND));

        List<Question> questions = questionRepository.findByTestIdOrderByOrderIndex(testId);

        int questionCount = questions.size();
        int totalScore = questions.stream().mapToInt(Question::getScore).sum();

        test.setQuestionCount(questionCount);
        test.setTotalPossibleScore(totalScore);
        testRepository.save(test);
    }

    private void createQuestion(UUID testId, CreateQuestionRequest request, int orderIndex) {
        validateQuestionRequest(request);

        Question question = Question.builder()
            .testId(testId)
            .questionType(request.questionType())
            .questionText(request.questionText())
            .imageUrl(request.imageUrl())
            .score(request.score())
            .orderIndex(request.orderIndex() != null ? request.orderIndex() : orderIndex)
            .correctAnswer(request.correctAnswer() != null ? normalizeText(request.correctAnswer()) : null)
            .build();

        questionRepository.save(question);

        if ((CLOSED_SINGLE.equals(request.questionType()) ||
            CLOSED_MULTIPLE.equals(request.questionType())) && request.answers() != null) {
            for (int i = 0; i < request.answers().size(); i++) {
                CreateAnswerRequest aReq = request.answers().get(i);
                Answer answer = Answer.builder()
                    .questionId(question.getId())
                    .answerText(aReq.answerText())
                    .isCorrect(aReq.isCorrect())
                    .orderIndex(aReq.orderIndex() != null ? aReq.orderIndex() : i)
                    .build();
                answerRepository.save(answer);
            }
        }
    }

    private void validateQuestionRequest(CreateQuestionRequest request) {
        String questionType = request.questionType();

        switch (questionType) {
            case CLOSED_SINGLE -> validateClosedSingleRequest(request);
            case CLOSED_MULTIPLE -> validateClosedMultipleRequest(request);
            case OPEN_TEXT -> validateOpenTextRequest(request);
            default -> {
                // No validation needed for other types
            }
        }
    }

    private void validateClosedSingleRequest(CreateQuestionRequest request) {
        if (request.answers() == null || request.answers().isEmpty()) {
            throw new IllegalArgumentException(CLOSED_SINGLE_MUST_HAVE_ANSWER);
        }
        long correctCount = request.answers().stream().filter(CreateAnswerRequest::isCorrect).count();
        if (correctCount != 1) {
            throw new IllegalArgumentException(CLOSED_SINGLE_ONE_CORRECT);
        }
    }

    private void validateClosedMultipleRequest(CreateQuestionRequest request) {
        if (request.answers() == null || request.answers().isEmpty()) {
            throw new IllegalArgumentException(CLOSED_MULTIPLE_MUST_HAVE_ANSWER);
        }
        long correctCount = request.answers().stream().filter(CreateAnswerRequest::isCorrect).count();
        if (correctCount < 1) {
            throw new IllegalArgumentException(CLOSED_MULTIPLE_AT_LEAST_ONE);
        }
    }

    private void validateOpenTextRequest(CreateQuestionRequest request) {
        if (request.correctAnswer() == null || request.correctAnswer().isBlank()) {
            throw new IllegalArgumentException(OPEN_TEXT_REQUIRES_ANSWER);
        }
    }

    private void validateQuestion(Question question) {
        if (CLOSED_SINGLE.equals(question.getQuestionType())) {
            List<Answer> answers = answerRepository.findByQuestionIdAndIsCorrect(question.getId(), true);
            if (answers.size() != 1) {
                throw new IllegalArgumentException(CLOSED_SINGLE_ONE_CORRECT);
            }
        } else if (CLOSED_MULTIPLE.equals(question.getQuestionType())) {
            List<Answer> answers = answerRepository.findByQuestionIdAndIsCorrect(question.getId(), true);
            if (answers.isEmpty()) {
                throw new IllegalArgumentException(CLOSED_MULTIPLE_AT_LEAST_ONE);
            }
        } else if (OPEN_TEXT.equals(question.getQuestionType()) && (question.getCorrectAnswer() == null || question.getCorrectAnswer().isBlank())) {
            throw new IllegalArgumentException(OPEN_TEXT_REQUIRES_ANSWER);
        }

    }

    @Override
    public String uploadQuestionImage(UUID questionId, MultipartFile file) {
        Question question = questionRepository.findById(questionId)
            .orElseThrow(() -> new NoSuchElementException(QUESTION_NOT_FOUND));

        if (question.getImageUrl() != null) {
            imageUploadService.deleteImage(question.getImageUrl());
        }

        String imageUrl = imageUploadService.uploadImage(file, "question-images");
        question.setImageUrl(imageUrl);
        questionRepository.save(question);

        return imageUrl;
    }

    @Override
    public void deleteQuestionImage(UUID questionId) {
        Question question = questionRepository.findById(questionId)
            .orElseThrow(() -> new NoSuchElementException(QUESTION_NOT_FOUND));

        if (question.getImageUrl() != null) {
            imageUploadService.deleteImage(question.getImageUrl());
            question.setImageUrl(null);
            questionRepository.save(question);
        }
    }

    @Override
    public Page<TestResponse> listPublishedTestsForUser(Pageable pageable) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userRepository.findByEmail(auth.getName())
            .orElseThrow(() -> new NoSuchElementException(USER_NOT_FOUND));

        Role role = roleRepository.findById(user.getRoleId())
            .orElseThrow(() -> new NoSuchElementException(ROLE_NOT_FOUND));

        Boolean isPremiumFilter = null;
        if (!Boolean.TRUE.equals(user.getIsPremium()) && !"ADMIN".equals(role.getTitle())) {
            isPremiumFilter = false;
        }

        return listTests(PUBLISHED, isPremiumFilter, pageable);
    }

    @Override
    public TestDetailResponse getTestForUser(UUID testId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userRepository.findByEmail(auth.getName())
            .orElseThrow(() -> new NoSuchElementException(USER_NOT_FOUND));

        Role role = roleRepository.findById(user.getRoleId())
            .orElseThrow(() -> new NoSuchElementException(ROLE_NOT_FOUND));

        TestDetailResponse test = getTest(testId);

        if (Boolean.TRUE.equals(test.isPremium()) && !Boolean.TRUE.equals(user.getIsPremium()) && !"ADMIN".equals(role.getTitle())) {
            throw new IllegalStateException(CANNOT_START_PREMIUM_TEST);
        }

        return test;
    }

    private String normalizeText(String text) {
        if (text == null) return null;
        return text.toLowerCase().trim();
    }

    private Integer calculateEstimatedTime(Integer questionCount) {
        if (questionCount == null || questionCount == 0) {
            return 0;
        }

        int baseTime = questionCount * BASE_MINUTES_PER_QUESTION;
        int overhead = questionCount >= 5 ? 5 : 0;

        return baseTime + overhead;
    }

    private List<QuestionTypeCount> calculateQuestionTypeCounts(List<Question> questions) {
        if (questions == null || questions.isEmpty()) {
            return List.of();
        }

        Map<String, Long> typeCounts = questions.stream()
            .collect(Collectors.groupingBy(Question::getQuestionType, Collectors.counting()));

        return typeCounts.entrySet().stream()
            .map(entry -> new QuestionTypeCount(entry.getKey(), entry.getValue().intValue()))
            .toList();
    }

    private void validateQuestionOrderIndices(List<CreateQuestionRequest> questions) {
        List<Integer> orderIndices = new ArrayList<>();
        for (int i = 0; i < questions.size(); i++) {
            CreateQuestionRequest q = questions.get(i);
            Integer orderIndex = q.orderIndex() != null ? q.orderIndex() : i;
            orderIndices.add(orderIndex);
        }

        Set<Integer> uniqueIndices = new HashSet<>(orderIndices);
        if (uniqueIndices.size() != orderIndices.size()) {
            Set<Integer> seen = new HashSet<>();
            for (Integer idx : orderIndices) {
                if (!seen.add(idx)) {
                    throw new IllegalArgumentException("Duplicate question orderIndex found: " + idx + ". Each question must have a unique orderIndex.");
                }
            }
        }

        for (int i = 0; i < questions.size(); i++) {
            CreateQuestionRequest q = questions.get(i);
            if (q.answers() != null && !q.answers().isEmpty()) {
                validateAnswerOrderIndices(q.answers(), i);
            }
        }
    }

    private void validateAnswerOrderIndices(List<CreateAnswerRequest> answers, int questionPosition) {
        List<Integer> orderIndices = new ArrayList<>();
        for (int i = 0; i < answers.size(); i++) {
            CreateAnswerRequest a = answers.get(i);
            Integer orderIndex = a.orderIndex() != null ? a.orderIndex() : i;
            orderIndices.add(orderIndex);
        }

        Set<Integer> uniqueIndices = new HashSet<>(orderIndices);
        if (uniqueIndices.size() != orderIndices.size()) {
            Set<Integer> seen = new HashSet<>();
            for (Integer idx : orderIndices) {
                if (!seen.add(idx)) {
                    throw new IllegalArgumentException(
                        "Duplicate answer orderIndex found: " + idx + " in question at position " + questionPosition +
                            ". Each answer must have a unique orderIndex within its question."
                    );
                }
            }
        }
    }
}
