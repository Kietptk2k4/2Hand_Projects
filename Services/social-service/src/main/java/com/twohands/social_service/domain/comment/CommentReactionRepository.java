package com.twohands.social_service.domain.comment;

import java.util.UUID;

public interface CommentReactionRepository {
    boolean existsByCommentIdAndUserId(String commentId, UUID userId);

    void save(String commentId, UUID userId);

    void deleteByCommentIdAndUserId(String commentId, UUID userId);
}
