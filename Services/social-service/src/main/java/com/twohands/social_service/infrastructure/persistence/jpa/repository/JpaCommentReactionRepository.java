package com.twohands.social_service.infrastructure.persistence.jpa.repository;

import com.twohands.social_service.infrastructure.persistence.jpa.entity.CommentReactionEntity;
import com.twohands.social_service.infrastructure.persistence.jpa.entity.CommentReactionEntityId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

public interface JpaCommentReactionRepository extends JpaRepository<CommentReactionEntity, CommentReactionEntityId> {
    boolean existsByCommentIdAndUserId(String commentId, UUID userId);

    Page<CommentReactionEntity> findByCommentIdOrderByCreatedAtDesc(String commentId, Pageable pageable);

    @Query("SELECT c.commentId FROM CommentReactionEntity c WHERE c.userId = :userId AND c.commentId IN :commentIds")
    Set<String> findCommentIdsByUserIdAndCommentIdIn(
            @Param("userId") UUID userId,
            @Param("commentIds") Collection<String> commentIds
    );

    void deleteByCommentIdAndUserId(String commentId, UUID userId);
}
