package com.qanunqapisi.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.qanunqapisi.domain.UserAnswer;

@Repository
public interface UserAnswerRepository extends JpaRepository<UserAnswer, UUID> {
    List<UserAnswer> findByTestAttemptId(UUID testAttemptId);
    boolean existsByQuestionId(UUID questionId);
}
