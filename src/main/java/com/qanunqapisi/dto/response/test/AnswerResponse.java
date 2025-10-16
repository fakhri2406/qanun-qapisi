package com.qanunqapisi.dto.response.test;

import java.util.UUID;

public record AnswerResponse(
    UUID id,
    String answerText,
    Boolean isCorrect,
    Integer orderIndex
) {
}
