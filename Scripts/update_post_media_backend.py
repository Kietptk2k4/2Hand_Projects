# -*- coding: utf-8 -*-
from pathlib import Path

ROOT = Path(r"d:/Projects/2Hand_Projects/Services/social-service/src/main/java/com/twohands/social_service")

(ROOT / "application/post/common/PostMediaMappings.java").write_text(
    """package com.twohands.social_service.application.post.common;

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
""",
    encoding="utf-8",
    newline="\n",
)

(ROOT / "application/post/common/PostMediaDimensionValidator.java").write_text(
    """package com.twohands.social_service.application.post.common;

import com.twohands.social_service.exception.AppException;
import com.twohands.social_service.exception.ErrorCode;

public final class PostMediaDimensionValidator {

    private static final int MIN_DIMENSION = 1;
    private static final int MAX_DIMENSION = 10000;

    private PostMediaDimensionValidator() {
    }

    public static void validate(Integer width, Integer height, String fieldPrefix) {
        if (width == null && height == null) {
            return;
        }
        if (width == null || height == null) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Validation failed",
                    fieldPrefix + ".width", "width va height phai duoc gui cung nhau.");
        }
        if (width < MIN_DIMENSION || width > MAX_DIMENSION) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Validation failed",
                    fieldPrefix + ".width", "width phai tu " + MIN_DIMENSION + " den " + MAX_DIMENSION + ".");
        }
        if (height < MIN_DIMENSION || height > MAX_DIMENSION) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Validation failed",
                    fieldPrefix + ".height", "height phai tu " + MIN_DIMENSION + " den " + MAX_DIMENSION + ".");
        }
    }
}
""",
    encoding="utf-8",
    newline="\n",
)

(ROOT / "delivery/http/post/response/PostMediaItemResponse.java").write_text(
    """package com.twohands.social_service.delivery.http.post.response;

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
""",
    encoding="utf-8",
    newline="\n",
)

print("common files written")
