package com.qanunqapisi.repository;

import com.qanunqapisi.domain.TestAttempt;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TestAttemptRepository extends JpaRepository<TestAttempt, UUID> {
    List<TestAttempt> findByUserId(UUID userId);

    Page<TestAttempt> findByUserId(UUID userId, Pageable pageable);

    List<TestAttempt> findByUserIdAndTestId(UUID userId, UUID testId);

    Optional<TestAttempt> findByUserIdAndTestIdAndStatus(UUID userId, UUID testId, String status);

    Page<TestAttempt> findByTestIdAndStatus(UUID testId, String status, Pageable pageable);

    long countDistinctUserIdByTestId(UUID testId);

    long count();
}
