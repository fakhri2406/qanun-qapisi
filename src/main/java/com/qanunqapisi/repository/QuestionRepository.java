package com.qanunqapisi.repository;

import com.qanunqapisi.domain.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface QuestionRepository extends JpaRepository<Question, UUID> {
    List<Question> findByTestIdOrderByOrderIndex(UUID testId);

    void deleteByTestId(UUID testId);

    long countByTestId(UUID testId);
}
