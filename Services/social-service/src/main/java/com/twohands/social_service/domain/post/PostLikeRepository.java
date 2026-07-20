package com.twohands.social_service.domain.post;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

public interface PostLikeRepository {
    boolean existsByPostIdAndUserId(String postId, UUID userId);

    PageResult<PostLikeEntry> findLikersByPostId(String postId, int page, int size);

    Set<String> findLikedPostIdsByUserIdAndPostIds(UUID userId, Collection<String> postIds);

    void save(String postId, UUID userId);

    void deleteByPostIdAndUserId(String postId, UUID userId);

    java.util.List<String> findRecentLikedPostIds(UUID userId, int limit);
}
