package com.qanunqapisi.repository;

import com.qanunqapisi.domain.UserAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserAnswerRepository extends JpaRepository<UserAnswer, UUID> {
    List<UserAnswer> findByTestAttemptId(UUID testAttemptId);

    boolean existsByQuestionId(UUID questionId);
}
