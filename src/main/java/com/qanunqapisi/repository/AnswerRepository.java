package com.qanunqapisi.repository;

import com.qanunqapisi.domain.Answer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AnswerRepository extends JpaRepository<Answer, UUID> {
    List<Answer> findByQuestionIdOrderByOrderIndex(UUID questionId);

    void deleteByQuestionId(UUID questionId);

    List<Answer> findByQuestionIdAndIsCorrect(UUID questionId, Boolean isCorrect);
}
