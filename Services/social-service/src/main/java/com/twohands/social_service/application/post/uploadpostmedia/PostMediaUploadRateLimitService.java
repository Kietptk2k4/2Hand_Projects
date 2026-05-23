package com.twohands.social_service.application.post.uploadpostmedia;

import java.util.UUID;

public interface PostMediaUploadRateLimitService {

    void validateUploadUrlRequest(UUID userId);
}
