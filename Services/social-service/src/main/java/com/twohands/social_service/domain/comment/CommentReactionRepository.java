package com.twohands.social_service.domain.comment;

import com.twohands.social_service.domain.post.PageResult;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

public interface CommentReactionRepository {
    boolean existsByCommentIdAndUserId(String commentId, UUID userId);

    PageResult<CommentLikeEntry> findLikersByCommentId(String commentId, int page, int size);

    Set<String> findLikedCommentIdsByUserIdAndCommentIds(UUID userId, Collection<String> commentIds);

    void save(String commentId, UUID userId);

    void deleteByCommentIdAndUserId(String commentId, UUID userId);
}
