package com.qanunqapisi.dto.response.test;

import java.util.UUID;

public record TestStatisticsResponse(
    UUID testId,
    String testTitle,
    Long totalParticipants
) {
}
