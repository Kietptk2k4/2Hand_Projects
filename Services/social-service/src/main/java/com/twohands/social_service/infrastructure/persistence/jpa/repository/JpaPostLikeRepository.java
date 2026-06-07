package com.twohands.social_service.infrastructure.persistence.jpa.repository;

import com.twohands.social_service.infrastructure.persistence.jpa.entity.PostLikeEntity;
import com.twohands.social_service.infrastructure.persistence.jpa.entity.PostLikeEntityId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

public interface JpaPostLikeRepository extends JpaRepository<PostLikeEntity, PostLikeEntityId> {
    boolean existsByPostIdAndUserId(String postId, UUID userId);

    Page<PostLikeEntity> findByPostIdOrderByCreatedAtDesc(String postId, Pageable pageable);

    @Query("SELECT p.postId FROM PostLikeEntity p WHERE p.userId = :userId AND p.postId IN :postIds")
    Set<String> findPostIdsByUserIdAndPostIdIn(
            @Param("userId") UUID userId,
            @Param("postIds") Collection<String> postIds
    );

    void deleteByPostIdAndUserId(String postId, UUID userId);
}
