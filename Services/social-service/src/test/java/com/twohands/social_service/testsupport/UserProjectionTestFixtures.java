package com.twohands.social_service.testsupport;

import com.twohands.social_service.domain.user.UserProjection;

import java.util.Optional;
import java.util.UUID;

public final class UserProjectionTestFixtures {

    private UserProjectionTestFixtures() {
    }

    public static UserProjection active(UUID userId) {
        return new UserProjection(userId.toString(), "ACTIVE", "User", null, false);
    }

    public static Optional<UserProjection> activeOptional(UUID userId) {
        return Optional.of(active(userId));
    }
}
