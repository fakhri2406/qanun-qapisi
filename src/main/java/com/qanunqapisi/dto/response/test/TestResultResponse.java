package com.qanunqapisi.dto.response.test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record TestResultResponse(
    UUID attemptId,
    UUID testId,
    String testTitle,
    Integer totalScore,
    Integer maxPossibleScore,
    LocalDateTime startedAt,
    LocalDateTime submittedAt,
    List<QuestionResultResponse> questionResults
) {
}
