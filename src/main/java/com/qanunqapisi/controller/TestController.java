package com.qanunqapisi.controller;

import com.qanunqapisi.dto.request.test.SubmitTestRequest;
import com.qanunqapisi.dto.response.test.TestAttemptResponse;
import com.qanunqapisi.dto.response.test.TestDetailResponse;
import com.qanunqapisi.dto.response.test.TestResponse;
import com.qanunqapisi.dto.response.test.TestResultResponse;
import com.qanunqapisi.service.TestAttemptService;
import com.qanunqapisi.service.TestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tests")
@Validated
@RequiredArgsConstructor
public class TestController {
    private final TestService testService;
    private final TestAttemptService testAttemptService;

    @GetMapping
    public ResponseEntity<Page<TestResponse>> listPublishedTests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "publishedAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("ASC") ? 
            Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, Math.min(size, 100), sort);

        return ResponseEntity.ok(testService.listPublishedTestsForUser(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TestDetailResponse> getTest(@PathVariable UUID id) {
        return ResponseEntity.ok(testService.getTestForUser(id));
    }

    @PostMapping("/{id}/start")
    public ResponseEntity<TestAttemptResponse> startTest(@PathVariable UUID id) {
        return ResponseEntity.ok(testAttemptService.startTest(id));
    }

    @PostMapping("/{id}/submit")
    public ResponseEntity<TestResultResponse> submitTest(
            @PathVariable UUID id,
            @Valid @RequestBody SubmitTestRequest request) {
        return ResponseEntity.ok(testAttemptService.submitTest(id, request));
    }

    @GetMapping("/{id}/attempts")
    public ResponseEntity<List<TestAttemptResponse>> getTestAttempts(@PathVariable UUID id) {
        return ResponseEntity.ok(testAttemptService.getTestAttempts(id));
    }

    @GetMapping("/attempts/{attemptId}")
    public ResponseEntity<TestResultResponse> getAttemptResults(@PathVariable UUID attemptId) {
        return ResponseEntity.ok(testAttemptService.getAttemptResults(attemptId));
    }
}
