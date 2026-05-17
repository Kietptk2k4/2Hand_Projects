package com.twohands.auth_service.domain.user;

import java.util.Optional;
import java.util.UUID;

public interface UserProfileRepository {
    Optional<UserProfile> findByUserId(UUID userId);

    UserProfile save(UserProfile profile);

    int updateByUserId(UserProfile profile);

    void deleteByUserId(UUID userId);
}
