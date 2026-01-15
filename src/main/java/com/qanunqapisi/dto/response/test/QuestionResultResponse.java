package com.qanunqapisi.dto.response.test;

import java.util.List;
import java.util.UUID;

public record QuestionResultResponse(
    UUID questionId,
    String questionType,
    String questionText,
    Integer score,
    Integer orderIndex,
    Boolean isCorrect,
    Integer scoreEarned,
    List<UUID> selectedAnswerIds,
    String openTextAnswer,
    List<UUID> correctAnswerIds,
    String correctAnswer,
    List<AnswerResponse> allAnswers
) {
}
