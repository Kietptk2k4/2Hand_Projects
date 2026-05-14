package com.twohands.auth_service.domain.user;

import java.util.Optional;
import java.util.UUID;

public interface UserSettingsRepository {
    Optional<UserSettings> findByUserId(UUID userId);

    UserSettings save(UserSettings settings);

    void deleteByUserId(UUID userId);
}
