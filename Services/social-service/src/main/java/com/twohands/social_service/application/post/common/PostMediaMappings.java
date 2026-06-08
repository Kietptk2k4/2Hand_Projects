package com.twohands.social_service.application.post.common;

import com.twohands.social_service.domain.post.MediaItem;

public final class PostMediaMappings {

    private PostMediaMappings() {
    }

    public static MediaItem toDomain(String url, String type, Integer width, Integer height) {
        return new MediaItem(url, type, width, height);
    }

    public static MediaItem toDomain(MediaItemCommand command) {
        return new MediaItem(command.url(), command.type(), command.width(), command.height());
    }

    public static MediaItemData toData(MediaItem mediaItem) {
        return new MediaItemData(
                mediaItem.url(),
                mediaItem.type(),
                mediaItem.width(),
                mediaItem.height()
        );
    }

    public record MediaItemCommand(String url, String type, Integer width, Integer height) {
    }

    public record MediaItemData(String url, String type, Integer width, Integer height) {
    }
}
