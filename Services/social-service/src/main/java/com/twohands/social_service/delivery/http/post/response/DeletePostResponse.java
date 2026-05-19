package com.twohands.social_service.delivery.http.post.response;

public record DeletePostResponse(
        String postId,
        String status,
        String deletedAt,
        String updatedAt
) {
}
