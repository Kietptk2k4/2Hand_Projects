package com.twohands.social_service.infrastructure.persistence.adapter;

import com.twohands.social_service.domain.comment.CommentLikeEntry;
import com.twohands.social_service.domain.comment.CommentReactionRepository;
import com.twohands.social_service.domain.post.PageResult;
import com.twohands.social_service.infrastructure.persistence.jpa.entity.CommentReactionEntity;
import com.twohands.social_service.infrastructure.persistence.jpa.repository.JpaCommentReactionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Set;
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
    public PageResult<CommentLikeEntry> findLikersByCommentId(String commentId, int page, int size) {
        Page<CommentReactionEntity> result = jpaCommentReactionRepository.findByCommentIdOrderByCreatedAtDesc(
                commentId,
                PageRequest.of(page, size)
        );
        List<CommentLikeEntry> items = result.getContent().stream()
                .map(entity -> new CommentLikeEntry(entity.getUserId(), entity.getCreatedAt()))
                .toList();
        return new PageResult<>(
                items,
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages(),
                result.hasNext()
        );
    }

    @Override
    public Set<String> findLikedCommentIdsByUserIdAndCommentIds(UUID userId, Collection<String> commentIds) {
        if (commentIds == null || commentIds.isEmpty()) {
            return Set.of();
        }
        return jpaCommentReactionRepository.findCommentIdsByUserIdAndCommentIdIn(userId, commentIds);
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
