package com.qanunqapisi.dto.response.test;

import java.util.List;
import java.util.UUID;

public record QuestionResponse(
    UUID id,
    String questionType,
    String questionText,
    String imageUrl,
    Integer score,
    Integer orderIndex,
    String correctAnswer,
    List<AnswerResponse> answers
) {
}
