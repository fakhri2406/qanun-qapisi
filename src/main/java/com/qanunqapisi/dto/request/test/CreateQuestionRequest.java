package com.qanunqapisi.dto.request.test;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.List;

public record CreateQuestionRequest(
    @NotNull(message = "Question type is required")
    @Pattern(regexp = "^(CLOSED_SINGLE|CLOSED_MULTIPLE|OPEN_TEXT)$", message = "Question type must be CLOSED_SINGLE, CLOSED_MULTIPLE, or OPEN_TEXT")
    String questionType,

    @NotBlank(message = "Question text is required")
    String questionText,

    @NotNull(message = "Score is required")
    @Min(value = 1, message = "Score must be at least 1")
    Integer score,

    Integer orderIndex,

    String correctAnswer,

    @Valid
    List<CreateAnswerRequest> answers
) {
}
