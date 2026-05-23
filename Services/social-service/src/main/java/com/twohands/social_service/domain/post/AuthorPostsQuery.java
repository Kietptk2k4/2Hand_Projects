package com.twohands.social_service.domain.post;

public record AuthorPostsQuery(
        String authorId,
        AuthorPostsScope scope,
        int page,
        int size
) {
}
