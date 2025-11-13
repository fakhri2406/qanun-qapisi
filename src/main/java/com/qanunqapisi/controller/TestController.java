package com.qanunqapisi.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.qanunqapisi.dto.request.test.SubmitTestRequest;
import com.qanunqapisi.dto.response.error.ErrorResponse;
import com.qanunqapisi.dto.response.test.TestAttemptResponse;
import com.qanunqapisi.dto.response.test.TestDetailResponse;
import com.qanunqapisi.dto.response.test.TestResponse;
import com.qanunqapisi.dto.response.test.TestResultResponse;
import com.qanunqapisi.dto.response.test.TestStatisticsResponse;
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
@RequestMapping("/api/v1/tests")
@Validated
@RequiredArgsConstructor
@Tag(name = "Tests (Customer)", description = "Test and attempt management endpoints for customers")
@SecurityRequirement(name = "bearerAuth")
public class TestController {
    private final TestService testService;
    private final TestAttemptService testAttemptService;

    @GetMapping
    @Operation(summary = "List published tests", description = "Lists all published tests (both premium and non-premium). Non-premium users can see premium tests in the list but cannot view details or take them.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Tests retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Page<TestResponse>> listPublishedTests(
        @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") int page,
        @Parameter(description = "Page size (max 100)") @RequestParam(defaultValue = "20") int size,
        @Parameter(description = "Sort field") @RequestParam(defaultValue = "publishedAt") String sortBy,
        @Parameter(description = "Sort direction (ASC/DESC)") @RequestParam(defaultValue = "DESC") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("ASC") ?
            Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, Math.min(size, 100), sort);

        return ResponseEntity.ok(testService.listPublishedTestsForUser(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get test details", description = "Retrieves detailed information about a published test")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Test retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Premium test requires subscription", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Test not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<TestDetailResponse> getTest(@Parameter(description = "Test ID") @PathVariable UUID id) {
        return ResponseEntity.ok(testService.getTestForUser(id));
    }

    @PostMapping("/{id}/start")
    @Operation(summary = "Start test attempt", description = "Starts a new test attempt for the user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Test started successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Premium test requires subscription", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Test not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<TestAttemptResponse> startTest(@Parameter(description = "Test ID") @PathVariable UUID id) {
        return ResponseEntity.ok(testAttemptService.startTest(id));
    }

    @PostMapping("/{id}/submit")
    @Operation(summary = "Submit test answers", description = "Submits answers for a test attempt and calculates the score")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Test submitted successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid answers", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Test or attempt not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<TestResultResponse> submitTest(
        @Parameter(description = "Test ID") @PathVariable UUID id,
        @Valid @RequestBody SubmitTestRequest request) {
        return ResponseEntity.ok(testAttemptService.submitTest(id, request));
    }

    @GetMapping("/{id}/attempts")
    @Operation(summary = "List test attempts", description = "Lists all attempts by the user for a specific test")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Attempts retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Test not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<List<TestAttemptResponse>> getTestAttempts(@Parameter(description = "Test ID") @PathVariable UUID id) {
        return ResponseEntity.ok(testAttemptService.getTestAttempts(id));
    }

    @GetMapping("/attempts/{attemptId}")
    @Operation(summary = "Get attempt results", description = "Retrieves detailed results of a completed test attempt")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Results retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Attempt not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<TestResultResponse> getAttemptResults(@Parameter(description = "Attempt ID") @PathVariable UUID attemptId) {
        return ResponseEntity.ok(testAttemptService.getAttemptResults(attemptId));
    }

    @GetMapping("/{id}/statistics")
    @Operation(
        summary = "Get test statistics",
        description = "Retrieves statistics for a test including total number of participants"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Test not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<TestStatisticsResponse> getTestStatistics(@Parameter(description = "Test ID") @PathVariable UUID id) {
        return ResponseEntity.ok(testAttemptService.getTestStatistics(id));
    }
}
