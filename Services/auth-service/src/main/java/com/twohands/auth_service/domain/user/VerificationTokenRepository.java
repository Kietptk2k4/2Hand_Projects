package com.twohands.auth_service.domain.user;

import java.util.Optional;
import java.util.UUID;

public interface VerificationTokenRepository {
    Optional<VerificationToken> findByTokenHashAndType(String tokenHash, VerificationTokenType type);

    VerificationToken save(VerificationToken token);

    void deleteById(UUID tokenId);
}
