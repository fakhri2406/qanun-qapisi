package com.qanunqapisi.service.impl;

import com.qanunqapisi.domain.Answer;
import com.qanunqapisi.domain.Question;
import com.qanunqapisi.domain.Role;
import com.qanunqapisi.domain.Test;
import com.qanunqapisi.domain.User;
import com.qanunqapisi.dto.request.test.CreateAnswerRequest;
import com.qanunqapisi.dto.request.test.CreateQuestionRequest;
import com.qanunqapisi.dto.request.test.CreateTestRequest;
import com.qanunqapisi.dto.request.test.UpdateTestRequest;
import com.qanunqapisi.dto.response.test.AnswerResponse;
import com.qanunqapisi.dto.response.test.QuestionResponse;
import com.qanunqapisi.dto.response.test.TestDetailResponse;
import com.qanunqapisi.dto.response.test.TestResponse;
import com.qanunqapisi.external.cloudinary.ImageUploadService;
import com.qanunqapisi.repository.AnswerRepository;
import com.qanunqapisi.repository.QuestionRepository;
import com.qanunqapisi.repository.RoleRepository;
import com.qanunqapisi.repository.TestRepository;
import com.qanunqapisi.repository.UserRepository;
import com.qanunqapisi.service.TestService;
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
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.qanunqapisi.util.ErrorMessages.*;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class TestServiceImpl implements TestService {
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

        if ("PUBLISHED".equals(test.getStatus())) {
            throw new IllegalStateException(TEST_ALREADY_PUBLISHED);
        }

        List<Question> questions = questionRepository.findByTestIdOrderByOrderIndex(testId);
        
        if (questions.isEmpty()) {
            throw new IllegalStateException(TEST_MUST_HAVE_QUESTIONS);
        }

        for (Question question : questions) {
            validateQuestion(question);
        }

        test.setStatus("PUBLISHED");
        test.setPublishedAt(LocalDateTime.now());
        testRepository.save(test);

        return getTest(testId);
    }

    @Override
    @Transactional(readOnly = true)
    public TestDetailResponse getTest(UUID testId) {
        Test test = testRepository.findById(testId)
            .orElseThrow(() -> new NoSuchElementException(TEST_NOT_FOUND));

        List<Question> questions = questionRepository.findByTestIdOrderByOrderIndex(testId);
        List<QuestionResponse> questionResponses = new ArrayList<>();

        for (Question question : questions) {
            List<Answer> answers = answerRepository.findByQuestionIdOrderByOrderIndex(question.getId());
            List<AnswerResponse> answerResponses = answers.stream()
                .map(a -> new AnswerResponse(a.getId(), a.getAnswerText(), a.getIsCorrect(), a.getOrderIndex()))
                .collect(Collectors.toList());

            questionResponses.add(new QuestionResponse(
                question.getId(),
                question.getQuestionType(),
                question.getQuestionText(),
                question.getImageUrl(),
                question.getScore(),
                question.getOrderIndex(),
                question.getCorrectAnswer(),
                answerResponses
            ));
        }

        return new TestDetailResponse(
            test.getId(),
            test.getTitle(),
            test.getDescription(),
            test.getIsPremium(),
            test.getStatus(),
            test.getQuestionCount(),
            test.getTotalPossibleScore(),
            test.getPublishedAt(),
            questionResponses,
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
        } else {
            tests = testRepository.findAll(pageable);
        }

        return tests.map(test -> new TestResponse(
            test.getId(),
            test.getTitle(),
            test.getDescription(),
            test.getIsPremium(),
            test.getStatus(),
            test.getQuestionCount(),
            test.getTotalPossibleScore(),
            test.getPublishedAt(),
            test.getCreatedAt(),
            test.getUpdatedAt()
        ));
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

        if ("CLOSED_SINGLE".equals(request.questionType()) ||
            "CLOSED_MULTIPLE".equals(request.questionType())) {
            
            if (request.answers() != null) {
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
    }

    private void validateQuestionRequest(CreateQuestionRequest request) {
        if ("CLOSED_SINGLE".equals(request.questionType())) {
            if (request.answers() == null || request.answers().isEmpty()) {
                throw new IllegalArgumentException("Closed-single question must have answers");
            }
            long correctCount = request.answers().stream().filter(CreateAnswerRequest::isCorrect).count();
            if (correctCount != 1) {
                throw new IllegalArgumentException(CLOSED_SINGLE_ONE_CORRECT);
            }
        } else if ("CLOSED_MULTIPLE".equals(request.questionType())) {
            if (request.answers() == null || request.answers().isEmpty()) {
                throw new IllegalArgumentException("Closed-multiple question must have answers");
            }
            long correctCount = request.answers().stream().filter(CreateAnswerRequest::isCorrect).count();
            if (correctCount < 1) {
                throw new IllegalArgumentException(CLOSED_MULTIPLE_AT_LEAST_ONE);
            }
        } else if ("OPEN_TEXT".equals(request.questionType())) {
            if (request.correctAnswer() == null || request.correctAnswer().isBlank()) {
                throw new IllegalArgumentException(OPEN_TEXT_REQUIRES_ANSWER);
            }
        }
    }

    private void validateQuestion(Question question) {
        if ("CLOSED_SINGLE".equals(question.getQuestionType())) {
            List<Answer> answers = answerRepository.findByQuestionIdAndIsCorrect(question.getId(), true);
            if (answers.size() != 1) {
                throw new IllegalArgumentException(CLOSED_SINGLE_ONE_CORRECT);
            }
        } else if ("CLOSED_MULTIPLE".equals(question.getQuestionType())) {
            List<Answer> answers = answerRepository.findByQuestionIdAndIsCorrect(question.getId(), true);
            if (answers.isEmpty()) {
                throw new IllegalArgumentException(CLOSED_MULTIPLE_AT_LEAST_ONE);
            }
        } else if ("OPEN_TEXT".equals(question.getQuestionType())) {
            if (question.getCorrectAnswer() == null || question.getCorrectAnswer().isBlank()) {
                throw new IllegalArgumentException(OPEN_TEXT_REQUIRES_ANSWER);
            }
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
        if (!user.getIsPremium() && !role.getTitle().equals("ADMIN")) {
            isPremiumFilter = false;
        }

        return listTests("PUBLISHED", isPremiumFilter, pageable);
    }

    @Override
    public TestDetailResponse getTestForUser(UUID testId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userRepository.findByEmail(auth.getName())
            .orElseThrow(() -> new NoSuchElementException(USER_NOT_FOUND));

        Role role = roleRepository.findById(user.getRoleId())
            .orElseThrow(() -> new NoSuchElementException(ROLE_NOT_FOUND));

        TestDetailResponse test = getTest(testId);

        if (test.isPremium() && !user.getIsPremium() && !role.getTitle().equals("ADMIN")) {
            throw new IllegalStateException(CANNOT_START_PREMIUM_TEST);
        }

        return test;
    }

    private String normalizeText(String text) {
        if (text == null) return null;
        return text.toLowerCase().trim();
    }
}
