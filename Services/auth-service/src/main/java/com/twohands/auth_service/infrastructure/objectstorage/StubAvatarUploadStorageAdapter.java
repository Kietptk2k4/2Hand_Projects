package com.twohands.auth_service.infrastructure.objectstorage;

import com.twohands.auth_service.application.useraccount.avatarupload.AvatarUploadIntent;
import com.twohands.auth_service.application.useraccount.avatarupload.AvatarUploadStoragePort;
import com.twohands.auth_service.config.AuthObjectStorageProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
@Profile("test")
public class StubAvatarUploadStorageAdapter implements AvatarUploadStoragePort {

    private final AuthObjectStorageProperties properties;

    public StubAvatarUploadStorageAdapter(AuthObjectStorageProperties properties) {
        this.properties = properties;
    }

    @Override
    public AvatarUploadIntent createUploadIntent(UUID userId, String contentType, Instant expiresAt) {
        return createIntent(userId, contentType, expiresAt, "avatars");
    }

    @Override
    public AvatarUploadIntent createCoverUploadIntent(UUID userId, String contentType, Instant expiresAt) {
        return createIntent(userId, contentType, expiresAt, "covers");
    }

    private AvatarUploadIntent createIntent(UUID userId, String contentType, Instant expiresAt, String prefix) {
        String extension = switch (contentType) {
            case "image/jpeg" -> "jpg";
            case "image/png" -> "png";
            case "image/webp" -> "webp";
            default -> "bin";
        };
        String objectKey = prefix + "/" + userId + "/" + UUID.randomUUID() + "." + extension;
        String base = properties.getPublicUrl();
        if (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        String publicUrl = base + "/" + objectKey;
        return new AvatarUploadIntent(
                "https://minio.local/presigned-stub/" + objectKey,
                objectKey,
                publicUrl,
                expiresAt
        );
    }
}
