package com.twohands.social_service.domain.post;

import java.util.UUID;

public interface PostSaveRepository {
    boolean existsByPostIdAndUserId(String postId, UUID userId);

    void save(String postId, UUID userId);

    void deleteByPostIdAndUserId(String postId, UUID userId);
}
