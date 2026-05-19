package com.twohands.social_service.infrastructure.persistence.jpa.repository;

import com.twohands.social_service.infrastructure.persistence.jpa.entity.PostSaveEntity;
import com.twohands.social_service.infrastructure.persistence.jpa.entity.PostSaveEntityId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface JpaPostSaveRepository extends JpaRepository<PostSaveEntity, PostSaveEntityId> {
    boolean existsByPostIdAndUserId(String postId, UUID userId);

    void deleteByPostIdAndUserId(String postId, UUID userId);
}
