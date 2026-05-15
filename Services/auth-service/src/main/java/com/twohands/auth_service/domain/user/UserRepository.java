package com.twohands.auth_service.domain.user;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository {
    Optional<User> findById(UUID userId);

    Optional<User> findByEmailNormalized(String emailNormalized);

    boolean existsByEmailNormalized(String emailNormalized);

    User save(User user);

    void deleteById(UUID userId);

    int countByStatus(UserStatus status);

    void updateLastLoginAt(UUID userId, Instant lastLoginAt);

    void updatePassword(UUID userId, PasswordHash passwordHash, Instant passwordChangedAt);
}
