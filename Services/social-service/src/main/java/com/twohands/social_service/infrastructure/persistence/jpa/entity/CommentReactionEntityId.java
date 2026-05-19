package com.twohands.social_service.infrastructure.persistence.jpa.entity;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public class CommentReactionEntityId implements Serializable {

    private String commentId;
    private UUID userId;

    public CommentReactionEntityId() {
    }

    public CommentReactionEntityId(String commentId, UUID userId) {
        this.commentId = commentId;
        this.userId = userId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CommentReactionEntityId that = (CommentReactionEntityId) o;
        return Objects.equals(commentId, that.commentId) && Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(commentId, userId);
    }
}
