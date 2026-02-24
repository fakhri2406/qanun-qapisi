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

    @Query("SELECT q.testId, q.questionType, COUNT(q) FROM Question q WHERE q.testId IN :testIds GROUP BY q.testId, q.questionType")
    List<Object[]> countByTestIdInGroupByType(@Param("testIds") List<UUID> testIds);
}
