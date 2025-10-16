package com.qanunqapisi.repository;

import com.qanunqapisi.domain.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TestRepository extends JpaRepository<Test, UUID>, JpaSpecificationExecutor<Test> {
    Page<Test> findByStatus(String status, Pageable pageable);

    Page<Test> findByStatusAndIsPremium(String status, Boolean isPremium, Pageable pageable);

    long countByStatus(String status);
}
