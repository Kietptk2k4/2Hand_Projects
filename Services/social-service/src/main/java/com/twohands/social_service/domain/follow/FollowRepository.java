package com.twohands.social_service.domain.follow;

import com.twohands.social_service.domain.post.PageResult;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FollowRepository {
    List<UUID> findFolloweeIdsByFollowerId(UUID followerId);

    List<UUID> findAcceptedFolloweeIds(UUID followerId);

    List<UUID> findAcceptedFollowerIds(UUID followeeId);

    Optional<Follow> findByFollowerIdAndFolloweeId(UUID followerId, UUID followeeId);

    void save(Follow follow);

    void deleteByFollowerIdAndFolloweeId(UUID followerId, UUID followeeId);

    long countAcceptedFollowers(UUID followeeId);

    long countAcceptedFollowing(UUID followerId);

    PageResult<FollowRelationEntry> findAcceptedFollowersPage(UUID followeeId, int page, int size);

    PageResult<FollowRelationEntry> findAcceptedFollowingPage(UUID followerId, int page, int size);
}
