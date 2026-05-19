package com.twohands.social_service.domain.user;

import java.util.Optional;
import java.util.UUID;

public interface UserProjectionRepository {
    Optional<UserProjection> findByUserId(UUID userId);
}
