package com.twohands.social_service.infrastructure.persistence.jpa.repository;

import com.twohands.social_service.infrastructure.persistence.jpa.entity.CommentReactionEntity;
import com.twohands.social_service.infrastructure.persistence.jpa.entity.CommentReactionEntityId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface JpaCommentReactionRepository extends JpaRepository<CommentReactionEntity, CommentReactionEntityId> {
    boolean existsByCommentIdAndUserId(String commentId, UUID userId);

    void deleteByCommentIdAndUserId(String commentId, UUID userId);
}
