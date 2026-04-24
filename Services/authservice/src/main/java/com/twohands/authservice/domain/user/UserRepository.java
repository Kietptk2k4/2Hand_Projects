package com.twohands.authservice.domain.user;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository {

    boolean existsByEmailNormalized(String emailNormalized);

    Optional<User> findByEmailNormalized(String emailNormalized);

    User save(User user);

    Optional<User> findById(UUID id);
}
