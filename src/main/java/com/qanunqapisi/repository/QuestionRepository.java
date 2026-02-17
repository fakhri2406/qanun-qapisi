package com.qanunqapisi.repository;

import com.qanunqapisi.domain.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface QuestionRepository extends JpaRepository<Question, UUID> {
    List<Question> findByTestIdOrderByOrderIndex(UUID testId);

    @Modifying
    @Query("DELETE FROM Question q WHERE q.testId = :testId")
    void deleteByTestId(@Param("testId") UUID testId);

    long countByTestId(UUID testId);
}
