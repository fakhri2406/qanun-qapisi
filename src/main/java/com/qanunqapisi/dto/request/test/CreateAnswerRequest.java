package com.qanunqapisi.dto.request.test;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateAnswerRequest(
    @NotBlank(message = "Answer text is required")
    String answerText,
    
    @NotNull(message = "isCorrect flag is required")
    Boolean isCorrect,
    
    Integer orderIndex
) {}
