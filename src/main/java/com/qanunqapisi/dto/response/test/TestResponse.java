package com.qanunqapisi.dto.response.test;

import java.time.LocalDateTime;
import java.util.UUID;

public record TestResponse(
    UUID id,
    String title,
    String description,
    Boolean isPremium,
    String status,
    Integer questionCount,
    Integer totalPossibleScore,
    LocalDateTime publishedAt,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
