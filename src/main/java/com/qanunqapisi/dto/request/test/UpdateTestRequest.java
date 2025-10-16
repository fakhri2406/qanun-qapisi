package com.qanunqapisi.dto.request.test;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

import java.util.List;

public record UpdateTestRequest(
    @Size(max = 500, message = "Title must not exceed 500 characters")
    String title,
    
    String description,
    
    Boolean isPremium,
    
    @Valid
    List<CreateQuestionRequest> questions
) {}
