package com.twohands.auth_service.application.useraccount.avatarupload;

import java.util.UUID;

public interface AvatarUploadRateLimitService {
    void validateUploadUrlRequest(UUID userId);
}
