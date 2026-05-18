package com.twohands.social_service.domain.follow;

import java.util.List;
import java.util.UUID;

public interface FollowRepository {
    List<UUID> findAcceptedFolloweeIds(UUID followerId);
}
