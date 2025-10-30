package com.qanunqapisi.controller;

import java.util.Map;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.qanunqapisi.dto.request.test.CreateTestRequest;
import com.qanunqapisi.dto.request.test.UpdateTestRequest;
import com.qanunqapisi.dto.response.admin.TestAttemptAdminResponse;
import com.qanunqapisi.dto.response.error.ErrorResponse;
import com.qanunqapisi.dto.response.test.TestDetailResponse;
import com.qanunqapisi.dto.response.test.TestResponse;
import com.qanunqapisi.service.TestAttemptService;
import com.qanunqapisi.service.TestService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/admin/tests")
@Validated
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin: Tests", description = "Admin endpoints for test management")
@SecurityRequirement(name = "bearerAuth")
public class AdminTestController {
    private final TestService testService;
    private final TestAttemptService testAttemptService;

    @PostMapping
    @Operation(summary = "Create test", description = "Creates a new test with optional questions and answers")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Test created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<TestDetailResponse> createTest(@Valid @RequestBody CreateTestRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(testService.createTest(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update test", description = "Updates an existing test and its questions")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Test updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Test not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<TestDetailResponse> updateTest(
        @Parameter(description = "Test ID") @PathVariable UUID id,
        @Valid @RequestBody UpdateTestRequest request) {
        return ResponseEntity.ok(testService.updateTest(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete test", description = "Deletes a test and all associated data")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Test deleted successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Test not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> deleteTest(@Parameter(description = "Test ID") @PathVariable UUID id) {
        testService.deleteTest(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/publish")
    @Operation(summary = "Publish test", description = "Publishes a test, making it available to users")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Test published successfully"),
        @ApiResponse(responseCode = "400", description = "Test already published or has no questions", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Test not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<TestDetailResponse> publishTest(@Parameter(description = "Test ID") @PathVariable UUID id) {
        return ResponseEntity.ok(testService.publishTest(id));
    }

    @GetMapping
    @Operation(summary = "List tests", description = "Lists all tests with optional filtering by status and premium flag")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Tests retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Page<TestResponse>> listTests(
        @Parameter(description = "Filter by status (DRAFT/PUBLISHED)") @RequestParam(required = false) String status,
        @Parameter(description = "Filter by premium flag") @RequestParam(required = false) Boolean isPremium,
        @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") int page,
        @Parameter(description = "Page size (max 100)") @RequestParam(defaultValue = "50") int size,
        @Parameter(description = "Sort field") @RequestParam(defaultValue = "createdAt") String sortBy,
        @Parameter(description = "Sort direction (ASC/DESC)") @RequestParam(defaultValue = "DESC") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("ASC") ?
            Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, Math.min(size, 100), sort);

        return ResponseEntity.ok(testService.listTests(status, isPremium, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get test details", description = "Retrieves detailed information about a specific test")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Test retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Test not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<TestDetailResponse> getTest(@Parameter(description = "Test ID") @PathVariable UUID id) {
        return ResponseEntity.ok(testService.getTest(id));
    }

    @PostMapping(value = "/questions/{questionId}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload question image", description = "Uploads an image for a specific question (max 5MB, jpg/png)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Image uploaded successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid file", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Question not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Map<String, String>> uploadQuestionImage(
        @Parameter(description = "Question ID") @PathVariable UUID questionId,
        @RequestParam("file") MultipartFile file) {
        String imageUrl = testService.uploadQuestionImage(questionId, file);
        return ResponseEntity.ok(Map.of("imageUrl", imageUrl));
    }

    @DeleteMapping("/questions/{questionId}/image")
    @Operation(summary = "Delete question image", description = "Deletes the image associated with a question")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Image deleted successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Question not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> deleteQuestionImage(@Parameter(description = "Question ID") @PathVariable UUID questionId) {
        testService.deleteQuestionImage(questionId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/results")
    @Operation(
        summary = "Get test results",
        description = "Retrieves all completed test attempts for a specific test with user details (Admin only)"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Test results retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Test not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Page<TestAttemptAdminResponse>> getTestResults(
        @Parameter(description = "Test ID") @PathVariable UUID id,
        @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") int page,
        @Parameter(description = "Page size (max 100)") @RequestParam(defaultValue = "50") int size,
        @Parameter(description = "Sort field") @RequestParam(defaultValue = "submittedAt") String sortBy,
        @Parameter(description = "Sort direction (ASC/DESC)") @RequestParam(defaultValue = "DESC") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("ASC") ?
            Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, Math.min(size, 100), sort);

        return ResponseEntity.ok(testAttemptService.getTestResultsForAdmin(id, pageable));
    }
}
