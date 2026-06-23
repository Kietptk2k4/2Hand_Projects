package com.twohands.auth_service.application.auth.oauth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Base64;

@Service
public class IssueOAuthExchangeCodeUseCase {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final OAuthExchangeCodeStore exchangeCodeStore;
    private final long ttlSeconds;

    public IssueOAuthExchangeCodeUseCase(
            OAuthExchangeCodeStore exchangeCodeStore,
            @Value("${auth.oauth2.exchange-code-ttl-seconds:60}") long ttlSeconds
    ) {
        this.exchangeCodeStore = exchangeCodeStore;
        this.ttlSeconds = ttlSeconds;
    }

    public String execute(OAuthExchangeCodePayload payload) {
        String code = generateCode();
        exchangeCodeStore.save(code, payload, ttlSeconds);
        return code;
    }

    private static String generateCode() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
