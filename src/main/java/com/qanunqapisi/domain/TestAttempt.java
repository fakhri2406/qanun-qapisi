package com.qanunqapisi.domain;

import java.time.LocalDateTime;
import java.util.UUID;

import com.qanunqapisi.domain.enums.AttemptStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "test_attempts")
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestAttempt extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "test_id", nullable = false)
    private UUID testId;

    @Column(name = "total_score", nullable = false)
    private Integer totalScore;

    @Column(name = "max_possible_score", nullable = false)
    private Integer maxPossibleScore;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, columnDefinition = "attempt_status")
    private AttemptStatus status;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;
}
