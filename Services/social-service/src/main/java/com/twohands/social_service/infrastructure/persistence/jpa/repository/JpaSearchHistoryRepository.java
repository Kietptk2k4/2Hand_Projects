package com.twohands.social_service.infrastructure.persistence.jpa.repository;

import com.twohands.social_service.infrastructure.persistence.jpa.entity.SearchHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface JpaSearchHistoryRepository extends JpaRepository<SearchHistoryEntity, UUID> {
    Optional<SearchHistoryEntity> findByUserIdAndKeywordIgnoreCase(UUID userId, String keyword);
}
