package com.twohands.social_service.domain.post;

public record PostSearchQuery(
        String keyword,
        int page,
        int size
) {
}
