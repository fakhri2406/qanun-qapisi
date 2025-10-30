package com.qanunqapisi.dto.response.test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record TestResponse(
    UUID id,
    String title,
    String description,
    Boolean isPremium,
    String status,
    Integer questionCount,
    Integer totalPossibleScore,
    Integer estimatedMinutes,
    List<QuestionTypeCount> questionTypeCounts,
    LocalDateTime publishedAt,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
