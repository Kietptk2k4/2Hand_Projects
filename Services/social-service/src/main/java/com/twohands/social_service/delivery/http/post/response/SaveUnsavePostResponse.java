package com.twohands.social_service.delivery.http.post.response;

public record SaveUnsavePostResponse(
        String postId,
        boolean saved
) {
}
