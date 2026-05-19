package com.twohands.social_service.infrastructure.persistence.jpa.entity;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public class PostLikeEntityId implements Serializable {

    private String postId;
    private UUID userId;

    public PostLikeEntityId() {
    }

    public PostLikeEntityId(String postId, UUID userId) {
        this.postId = postId;
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
        PostLikeEntityId that = (PostLikeEntityId) o;
        return Objects.equals(postId, that.postId) && Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(postId, userId);
    }
}
