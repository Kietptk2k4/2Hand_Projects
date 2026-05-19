package com.twohands.social_service.infrastructure.persistence.adapter;

import com.twohands.social_service.domain.comment.CommentReactionRepository;
import com.twohands.social_service.infrastructure.persistence.jpa.entity.CommentReactionEntity;
import com.twohands.social_service.infrastructure.persistence.jpa.repository.JpaCommentReactionRepository;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class CommentReactionRepositoryAdapter implements CommentReactionRepository {

    private final JpaCommentReactionRepository jpaCommentReactionRepository;

    public CommentReactionRepositoryAdapter(JpaCommentReactionRepository jpaCommentReactionRepository) {
        this.jpaCommentReactionRepository = jpaCommentReactionRepository;
    }

    @Override
    public boolean existsByCommentIdAndUserId(String commentId, UUID userId) {
        return jpaCommentReactionRepository.existsByCommentIdAndUserId(commentId, userId);
    }

    @Override
    public void save(String commentId, UUID userId) {
        Instant now = Instant.now();
        CommentReactionEntity entity = new CommentReactionEntity();
        entity.setCommentId(commentId);
        entity.setUserId(userId);
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        jpaCommentReactionRepository.save(entity);
    }

    @Override
    public void deleteByCommentIdAndUserId(String commentId, UUID userId) {
        jpaCommentReactionRepository.deleteByCommentIdAndUserId(commentId, userId);
    }
}
