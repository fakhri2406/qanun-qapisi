package com.qanunqapisi.dto.request.test;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreateTestRequest(
    @NotBlank(message = "Title is required")
    @Size(max = 500, message = "Title must not exceed 500 characters")
    String title,
    
    @NotBlank(message = "Description is required")
    String description,
    
    @NotNull(message = "isPremium flag is required")
    Boolean isPremium,
    
    @Valid
    List<CreateQuestionRequest> questions
) {}
