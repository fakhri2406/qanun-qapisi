package com.qanunqapisi.repository;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.qanunqapisi.domain.Test;

@Repository
public interface TestRepository extends JpaRepository<Test, UUID>, JpaSpecificationExecutor<Test> {
    Page<Test> findByStatus(String status, Pageable pageable);
    Page<Test> findByStatusAndIsPremium(String status, Boolean isPremium, Pageable pageable);
    long countByStatus(String status);
}
