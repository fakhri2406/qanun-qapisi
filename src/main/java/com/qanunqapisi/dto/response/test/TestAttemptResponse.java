package com.qanunqapisi.dto.response.test;

import java.time.LocalDateTime;
import java.util.UUID;

public record TestAttemptResponse(
    UUID id,
    UUID testId,
    String testTitle,
    Integer totalScore,
    Integer maxPossibleScore,
    String status,
    LocalDateTime startedAt,
    LocalDateTime submittedAt
) {
}
