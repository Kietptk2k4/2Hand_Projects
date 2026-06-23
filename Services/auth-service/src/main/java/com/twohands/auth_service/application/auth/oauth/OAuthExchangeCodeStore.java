package com.twohands.auth_service.application.auth.oauth;

import java.util.Optional;

public interface OAuthExchangeCodeStore {

    void save(String code, OAuthExchangeCodePayload payload, long ttlSeconds);

    Optional<OAuthExchangeCodePayload> consume(String code);
}
