package com.qanunqapisi.dto.request.test;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record SubmitTestRequest(
    @NotEmpty(message = "Answers are required")
    @Valid
    List<SubmitAnswerRequest> answers
) {
}
