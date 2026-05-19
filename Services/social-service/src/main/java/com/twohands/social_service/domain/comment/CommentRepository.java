package com.twohands.social_service.domain.comment;

import java.util.Optional;

public interface CommentRepository {
    Comment save(Comment comment);

    Optional<Comment> findById(String commentId);

    void incrementLikeCount(String commentId);

    void decrementLikeCount(String commentId);
}
