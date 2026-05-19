package com.twohands.social_service.domain.follow;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FollowRepository {
    List<UUID> findAcceptedFolloweeIds(UUID followerId);

    Optional<Follow> findByFollowerIdAndFolloweeId(UUID followerId, UUID followeeId);

    void save(Follow follow);

    void deleteByFollowerIdAndFolloweeId(UUID followerId, UUID followeeId);
}
