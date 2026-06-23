package com.twohands.social_service.application.post.uploadpostmedia;

import java.time.Instant;
import java.util.UUID;

public interface PostMediaUploadStoragePort {

    PostMediaUploadIntent createUploadIntent(
            UUID userId,
            String contentType,
            String mediaKind,
            Instant expiresAt,
            String clientUploadOrigin
    );
}
