package com.twohands.auth_service.application.auth.oauth;

import com.twohands.auth_service.domain.oauth.OAuthProvider;

public record OAuthProfile(
        OAuthProvider provider,
        String providerUserId,
        String email,
        String name,
        String avatarUrl
) {
}
