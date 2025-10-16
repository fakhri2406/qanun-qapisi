package com.qanunqapisi.controller;

import com.qanunqapisi.dto.request.test.CreateTestRequest;
import com.qanunqapisi.dto.request.test.UpdateTestRequest;
import com.qanunqapisi.dto.response.test.TestDetailResponse;
import com.qanunqapisi.dto.response.test.TestResponse;
import com.qanunqapisi.service.TestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/tests")
@Validated
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminTestController {
    private final TestService testService;

    @PostMapping
    public ResponseEntity<TestDetailResponse> createTest(@Valid @RequestBody CreateTestRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(testService.createTest(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TestDetailResponse> updateTest(
        @PathVariable UUID id,
        @Valid @RequestBody UpdateTestRequest request) {
        return ResponseEntity.ok(testService.updateTest(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTest(@PathVariable UUID id) {
        testService.deleteTest(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/publish")
    public ResponseEntity<TestDetailResponse> publishTest(@PathVariable UUID id) {
        return ResponseEntity.ok(testService.publishTest(id));
    }

    @GetMapping
    public ResponseEntity<Page<TestResponse>> listTests(
        @RequestParam(required = false) String status,
        @RequestParam(required = false) Boolean isPremium,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "50") int size,
        @RequestParam(defaultValue = "createdAt") String sortBy,
        @RequestParam(defaultValue = "DESC") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("ASC") ?
            Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, Math.min(size, 100), sort);

        return ResponseEntity.ok(testService.listTests(status, isPremium, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TestDetailResponse> getTest(@PathVariable UUID id) {
        return ResponseEntity.ok(testService.getTest(id));
    }

    @PostMapping(value = "/questions/{questionId}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> uploadQuestionImage(
        @PathVariable UUID questionId,
        @RequestParam("file") MultipartFile file) {
        String imageUrl = testService.uploadQuestionImage(questionId, file);
        return ResponseEntity.ok(Map.of("imageUrl", imageUrl));
    }

    @DeleteMapping("/questions/{questionId}/image")
    public ResponseEntity<Void> deleteQuestionImage(@PathVariable UUID questionId) {
        testService.deleteQuestionImage(questionId);
        return ResponseEntity.noContent().build();
    }
}
