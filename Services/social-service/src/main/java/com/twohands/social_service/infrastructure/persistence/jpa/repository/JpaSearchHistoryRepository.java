package com.twohands.social_service.infrastructure.persistence.jpa.repository;

import com.twohands.social_service.infrastructure.persistence.jpa.entity.SearchHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface JpaSearchHistoryRepository extends JpaRepository<SearchHistoryEntity, UUID> {
    Optional<SearchHistoryEntity> findByUserIdAndKeywordIgnoreCase(UUID userId, String keyword);

    @Query("SELECT s.keyword FROM SearchHistoryEntity s WHERE s.userId = :userId ORDER BY s.updatedAt DESC")
    java.util.List<String> findRecentKeywordsByUserId(@Param("userId") UUID userId, Pageable pageable);
}
