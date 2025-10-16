package com.qanunqapisi.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.qanunqapisi.domain.Answer;

@Repository
public interface AnswerRepository extends JpaRepository<Answer, UUID> {
    List<Answer> findByQuestionIdOrderByOrderIndex(UUID questionId);
    void deleteByQuestionId(UUID questionId);
    List<Answer> findByQuestionIdAndIsCorrect(UUID questionId, Boolean isCorrect);
}
