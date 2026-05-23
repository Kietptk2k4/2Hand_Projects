package com.twohands.social_service.domain.comment;

import com.twohands.social_service.domain.post.PageResult;

import java.util.Optional;

public interface CommentRepository {
    Comment save(Comment comment);

    Optional<Comment> findById(String commentId);

    Optional<Comment> findActiveByIdAndPostId(String commentId, String postId);

    PageResult<Comment> findActiveByPost(CommentListQuery query);

    long countActiveReplies(String postId, String parentCommentId);

    void incrementLikeCount(String commentId);

    void decrementLikeCount(String commentId);
}
