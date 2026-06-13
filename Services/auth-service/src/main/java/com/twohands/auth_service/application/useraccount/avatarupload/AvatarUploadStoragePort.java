package com.twohands.auth_service.application.useraccount.avatarupload;

import java.time.Instant;
import java.util.UUID;

public interface AvatarUploadStoragePort {

    AvatarUploadIntent createUploadIntent(UUID userId, String contentType, Instant expiresAt);

    AvatarUploadIntent createCoverUploadIntent(UUID userId, String contentType, Instant expiresAt);
}
