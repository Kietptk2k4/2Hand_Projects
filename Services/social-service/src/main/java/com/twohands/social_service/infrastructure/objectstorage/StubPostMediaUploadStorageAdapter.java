package com.twohands.social_service.infrastructure.objectstorage;

import com.twohands.social_service.application.post.uploadpostmedia.PostMediaUploadIntent;
import com.twohands.social_service.application.post.uploadpostmedia.PostMediaUploadStoragePort;
import com.twohands.social_service.config.SocialObjectStorageProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
@Profile("test")
public class StubPostMediaUploadStorageAdapter implements PostMediaUploadStoragePort {

    private final SocialObjectStorageProperties properties;

    public StubPostMediaUploadStorageAdapter(SocialObjectStorageProperties properties) {
        this.properties = properties;
    }

    @Override
    public PostMediaUploadIntent createUploadIntent(
            UUID userId,
            String contentType,
            String mediaKind,
            Instant expiresAt,
            String clientUploadOrigin
    ) {
        String extension = switch (contentType) {
            case "image/jpeg" -> "jpg";
            case "image/png" -> "png";
            case "image/webp" -> "webp";
            case "video/mp4" -> "mp4";
            default -> "bin";
        };
        String objectKey = "posts/" + userId + "/" + UUID.randomUUID() + "." + extension;
        String mediaUrl = properties.buildPublicObjectUrl(objectKey);
        return new PostMediaUploadIntent(
                "https://minio.local/presigned-stub/" + objectKey,
                objectKey,
                mediaUrl,
                mediaKind,
                expiresAt
        );
    }
}
