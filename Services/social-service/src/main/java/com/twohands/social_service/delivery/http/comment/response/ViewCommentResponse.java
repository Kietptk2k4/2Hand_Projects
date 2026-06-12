package com.twohands.social_service.delivery.http.comment.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ViewCommentResponse(
        @JsonProperty("comment_id")
        String commentId,
        @JsonProperty("post_id")
        String postId,
        AuthorResponse author,
        String status,
        @JsonProperty("moderation_status")
        String moderationStatus
) {
    public record AuthorResponse(
            @JsonProperty("userId")
            String userId
    ) {
    }
}
