package com.twohands.auth_service.application.useraccount.avatarupload;

import java.util.UUID;

public record CreateAvatarUploadUrlCommand(
        UUID userId,
        String contentType,
        long fileSizeBytes
) {
}
