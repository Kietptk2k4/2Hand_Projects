package com.twohands.social_service.infrastructure.persistence.jpa.repository;

import com.twohands.social_service.infrastructure.persistence.jpa.entity.PostLikeEntity;
import com.twohands.social_service.infrastructure.persistence.jpa.entity.PostLikeEntityId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface JpaPostLikeRepository extends JpaRepository<PostLikeEntity, PostLikeEntityId> {
    boolean existsByPostIdAndUserId(String postId, UUID userId);

    void deleteByPostIdAndUserId(String postId, UUID userId);
}
