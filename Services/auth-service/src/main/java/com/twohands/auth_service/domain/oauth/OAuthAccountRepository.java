package com.twohands.auth_service.domain.oauth;

import java.util.Optional;
import java.util.UUID;

public interface OAuthAccountRepository {
    Optional<OAuthAccount> findByProviderAndProviderUserId(OAuthProvider provider, String providerUserId);

    Optional<OAuthAccount> findByUserIdAndProvider(UUID userId, OAuthProvider provider);

    OAuthAccount save(OAuthAccount account);
}
