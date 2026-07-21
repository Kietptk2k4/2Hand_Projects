package com.twohands.social_service.application.admin.common;

import com.twohands.social_service.domain.post.MediaItem;

import java.util.List;

public final class PostMediaThumbnailResolver {

    private PostMediaThumbnailResolver() {
    }

    public static String resolveThumbnailUrl(List<MediaItem> media) {
        if (media == null || media.isEmpty()) {
            return null;
        }
        for (MediaItem item : media) {
            if (item != null && isImageType(item.type()) && hasUrl(item.url())) {
                return item.url();
            }
        }
        for (MediaItem item : media) {
            if (item != null && hasUrl(item.url())) {
                return item.url();
            }
        }
        return null;
    }

    public static int countMedia(List<MediaItem> media) {
        if (media == null) {
            return 0;
        }
        return (int) media.stream().filter(item -> item != null && hasUrl(item.url())).count();
    }

    private static boolean isImageType(String type) {
        return type != null && "IMAGE".equalsIgnoreCase(type.trim());
    }

    private static boolean hasUrl(String url) {
        return url != null && !url.isBlank();
    }
}
