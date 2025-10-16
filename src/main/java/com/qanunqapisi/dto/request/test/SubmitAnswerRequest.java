package com.qanunqapisi.dto.request.test;

import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record SubmitAnswerRequest(
    @NotNull(message = "Question ID is required")
    UUID questionId,
    
    List<UUID> selectedAnswerIds,
    
    String openTextAnswer
) {}
