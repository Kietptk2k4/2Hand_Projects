package com.twohands.social_service.domain.post;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface UserSeenPostsRepository {

    Set<String> findSeenPostIds(UUID userId);

    void upsertSeenPosts(UUID userId, List<String> postIds, Instant seenAt);
}
