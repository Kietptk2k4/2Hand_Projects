package com.twohands.social_service.application.post.deletepost;

public record DeletePostResult(
        String postId,
        String status,
        String deletedAt,
        String updatedAt
) {
}
