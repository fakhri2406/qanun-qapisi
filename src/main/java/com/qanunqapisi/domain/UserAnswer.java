package com.qanunqapisi.domain;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.qanunqapisi.config.jpa.UuidListConverter;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
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
@Table(name = "user_answers")
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserAnswer extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "test_attempt_id", nullable = false)
    private UUID testAttemptId;

    @Column(name = "question_id", nullable = false)
    private UUID questionId;

    @Column(name = "selected_answer_ids", columnDefinition = "uuid[]")
    @Convert(converter = UuidListConverter.class)
    private List<UUID> selectedAnswerIds;

    @Column(name = "open_text_answer", columnDefinition = "TEXT")
    private String openTextAnswer;

    @Column(name = "is_correct", nullable = false)
    private Boolean isCorrect;

    @Column(name = "score_earned", nullable = false)
    private Integer scoreEarned;

    @Column(name = "answered_at", nullable = false)
    private LocalDateTime answeredAt;
}
