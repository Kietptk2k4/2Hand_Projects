package com.twohands.social_service.delivery.http.post.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.twohands.social_service.application.post.common.PostMediaMappings;

public record PostMediaItemResponse(
        String url,
        String type,
        Integer width,
        Integer height
) {
    public static PostMediaItemResponse from(PostMediaMappings.MediaItemData data) {
        return new PostMediaItemResponse(data.url(), data.type(), data.width(), data.height());
    }

    public static PostMediaItemResponse from(
            String url,
            String type,
            Integer width,
            Integer height
    ) {
        return new PostMediaItemResponse(url, type, width, height);
    }
}
