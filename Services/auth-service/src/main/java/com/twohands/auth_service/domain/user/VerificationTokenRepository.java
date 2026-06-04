package com.twohands.auth_service.domain.user;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VerificationTokenRepository {
    Optional<VerificationToken> findByTokenHashAndType(String tokenHash, VerificationTokenType type);

    List<VerificationToken> findUnusedByType(VerificationTokenType type, Instant now);

    List<VerificationToken> findUsedByType(VerificationTokenType type);

    VerificationToken save(VerificationToken token);

    void deleteById(UUID tokenId);

    int markUnusedAsUsedByUserIdAndType(UUID userId, VerificationTokenType type, Instant usedAt);

    int markUsedById(UUID tokenId, Instant usedAt);
}
