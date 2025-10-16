package com.qanunqapisi.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tests")
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Test extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "created_by")
    private UUID createdBy;

    @Column(name = "title", nullable = false, length = 500)
    private String title;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "is_premium", nullable = false)
    private Boolean isPremium;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "question_count", nullable = false)
    private Integer questionCount;

    @Column(name = "total_possible_score", nullable = false)
    private Integer totalPossibleScore;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;
}
