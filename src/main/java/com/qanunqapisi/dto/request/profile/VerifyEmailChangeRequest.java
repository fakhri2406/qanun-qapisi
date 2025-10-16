package com.qanunqapisi.dto.request.profile;

import jakarta.validation.constraints.NotNull;

public record VerifyEmailChangeRequest(
    @NotNull(message = "Verification code is required")
    Integer code
) {}
