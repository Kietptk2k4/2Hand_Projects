package com.twohands.social_service.domain.post;

public record MediaItem(
        String url,
        String type,
        Integer width,
        Integer height
) {
    public MediaItem(String url, String type) {
        this(url, type, null, null);
    }
}
