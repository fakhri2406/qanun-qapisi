package com.qanunqapisi.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ErrorMessages {
    // Common
    public static final String NOT_AUTHENTICATED = "Not authenticated";
    public static final String RESOURCE_NOT_FOUND = "Resource not found";
    public static final String TEST_NOT_FOUND = "Test not found";
    public static final String QUESTION_NOT_FOUND = "Question not found";
    public static final String USER_NOT_FOUND = "User not found";
    public static final String ROLE_NOT_FOUND = "Role not found";
    public static final String ATTEMPT_NOT_FOUND = "Test attempt not found";
    public static final String EMAIL_IN_USE = "Email already in use";
    public static final String INVALID_REQUEST = "Invalid request";
    public static final String ACCESS_DENIED = "Access denied";
    public static final String CANNOT_DELETE_SELF = "Cannot delete your own account";
    public static final String LAST_ADMIN_GUARD = "Cannot remove or demote the last admin";

    // Auth
    public static final String INVALID_CREDENTIALS = "Invalid credentials";
    public static final String ACCOUNT_NOT_VERIFIED = "Account not verified";
    public static final String ACCOUNT_LOCKED = "Account locked. Try later";
    public static final String INVALID_REFRESH_TOKEN = "Invalid refresh token";
    public static final String REFRESH_TOKEN_EXPIRED = "Refresh token expired";
    public static final String REFRESH_TOKEN_MISMATCH = "Refresh token mismatch";
    public static final String INVALID_ACCESS_TOKEN = "Invalid access token";
    public static final String ACCESS_TOKEN_REVOKED = "Access token has been revoked";

    // Verification
    public static final String VERIFICATION_LOCKED = "Verification locked. Try later";
    public static final String VERIFICATION_EXPIRED = "Verification code expired";
    public static final String VERIFICATION_INVALID = "Invalid verification code";
    public static final String FAILED_TO_SEND_EMAIL = "Failed to send email";
    public static final String FAILED_TO_LOAD_TEMPLATE = "Failed to load email template";
    public static final String RESEND_COOLDOWN = "Please wait before requesting a new code";

    // Password or Email change
    public static final String INVALID_CURRENT_PASSWORD = "Invalid current password";
    public static final String PASSWORD_POLICY = "Password must be 8+ chars, include uppercase and digit";
    public static final String NEW_EMAIL_SAME = "New email must be different";
    public static final String INVALID_PASSWORD = "Invalid password";
    public static final String EMAIL_CHANGE_LOCKED = "Email change locked. Try later";
    public static final String EMAIL_CHANGE_MISMATCH = "Pending email mismatch";
    public static final String EMAIL_CHANGE_EXPIRED = "Email change code expired";
    public static final String EMAIL_CHANGE_INVALID = "Invalid email change code";
    public static final String PASSWORD_RESET_EXPIRED = "Password reset token expired";
    public static final String PASSWORD_RESET_INVALID = "Invalid password reset token";

    // Test Management
    public static final String TEST_MUST_HAVE_QUESTIONS = "Test must have at least one question";
    public static final String CLOSED_SINGLE_ONE_CORRECT = "Closed-single question must have exactly one correct answer";
    public static final String CLOSED_MULTIPLE_AT_LEAST_ONE = "Closed-multiple question must have at least one correct answer";
    public static final String OPEN_TEXT_REQUIRES_ANSWER = "Open-text question must have a correct answer";
    public static final String CANNOT_DELETE_QUESTION_WITH_ANSWERS = "Cannot delete question that has been answered";
    public static final String TEST_ALREADY_PUBLISHED = "Test is already published";
    public static final String TEST_NOT_PUBLISHED = "Test is not published";
    public static final String CANNOT_START_PREMIUM_TEST = "Premium subscription required to access this test";

    // Test Taking
    public static final String ATTEMPT_ALREADY_COMPLETED = "Test attempt already completed";
    public static final String ATTEMPT_NOT_IN_PROGRESS = "Test attempt is not in progress";
    public static final String ANSWER_MISMATCH = "Answer does not belong to this test";

    // Image Upload
    public static final String INVALID_FILE_TYPE = "Invalid file type. Only images are allowed";
    public static final String FILE_TOO_LARGE = "File size exceeds the maximum allowed limit";
    public static final String UPLOAD_FAILED = "File upload failed";
}
