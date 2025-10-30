package com.qanunqapisi.dto.response.test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record TestDetailResponse(
    UUID id,
    String title,
    String description,
    Boolean isPremium,
    String status,
    Integer questionCount,
    Integer totalPossibleScore,
    Integer estimatedMinutes,
    LocalDateTime publishedAt,
    List<QuestionResponse> questions,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
