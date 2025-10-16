package com.qanunqapisi.dto.response.test;

import com.qanunqapisi.domain.enums.AttemptStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record TestAttemptResponse(
    UUID id,
    UUID testId,
    String testTitle,
    Integer totalScore,
    Integer maxPossibleScore,
    AttemptStatus status,
    LocalDateTime startedAt,
    LocalDateTime submittedAt
) {}
