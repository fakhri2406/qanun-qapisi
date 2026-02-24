package com.qanunqapisi.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.qanunqapisi.domain.TestAttempt;

@Repository
public interface TestAttemptRepository extends JpaRepository<TestAttempt, UUID> {
    List<TestAttempt> findByUserId(UUID userId);

    List<TestAttempt> findByUserIdAndTestId(UUID userId, UUID testId);

    List<TestAttempt> findByUserIdAndTestIdAndStatusOrderByStartedAtDesc(UUID userId, UUID testId, String status);

    Page<TestAttempt> findByTestIdAndStatus(UUID testId, String status, Pageable pageable);

    long countDistinctUserIdByTestId(UUID testId);

    long count();

    List<TestAttempt> findByTestId(UUID testId);

    @Modifying
    @Query("DELETE FROM TestAttempt ta WHERE ta.userId = :userId")
    void deleteByUserId(@Param("userId") UUID userId);

    @Modifying
    @Query("DELETE FROM TestAttempt ta WHERE ta.testId = :testId")
    void deleteByTestId(@Param("testId") UUID testId);
}
