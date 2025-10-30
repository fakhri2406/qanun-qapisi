package com.qanunqapisi.dto.response.admin;

import java.time.LocalDateTime;
import java.util.UUID;

public record TestAttemptAdminResponse(
    UUID id,
    UUID userId,
    String userEmail,
    String userFirstName,
    String userLastName,
    Integer totalScore,
    Integer maxPossibleScore,
    String status,
    LocalDateTime startedAt,
    LocalDateTime submittedAt
) {
}
