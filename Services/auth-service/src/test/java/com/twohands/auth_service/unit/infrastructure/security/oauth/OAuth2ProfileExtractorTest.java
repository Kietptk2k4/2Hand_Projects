package com.twohands.auth_service.unit.infrastructure.security.oauth;

import com.twohands.auth_service.application.auth.oauth.OAuthProfile;
import com.twohands.auth_service.domain.oauth.OAuthProvider;
import com.twohands.auth_service.infrastructure.security.oauth.OAuth2ProfileExtractor;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class OAuth2ProfileExtractorTest {

    private final OAuth2ProfileExtractor extractor = new OAuth2ProfileExtractor(
            "google-client-id",
            "facebook-client-id"
    );

    @Test
    void googleProfileShouldPreferSubWhenIdMissing() {
        OAuth2User user = oauth2User(Map.of(
                "sub", "google-sub-1",
                "email", "user@example.com",
                "name", "Google User",
                "picture", "https://cdn.example.com/avatar.png",
                "aud", "google-client-id"
        ), "sub");

        OAuthProfile profile = extractor.extract("google", user);

        assertEquals(OAuthProvider.GOOGLE, profile.provider());
        assertEquals("google-sub-1", profile.providerUserId());
        assertEquals("user@example.com", profile.email());
        assertEquals("https://cdn.example.com/avatar.png", profile.avatarUrl());
    }

    @Test
    void facebookProfileShouldReadNestedPictureUrl() {
        Map<String, Object> picture = Map.of(
                "data", Map.of("url", "https://fb.cdn.example.com/pic.jpg")
        );
        OAuth2User user = oauth2User(Map.of(
                "id", "fb-user-1",
                "email", "fb.user@example.com",
                "name", "Facebook User",
                "picture", picture,
                "app_id", "facebook-client-id"
        ), "id");

        OAuthProfile profile = extractor.extract("facebook", user);

        assertEquals(OAuthProvider.FACEBOOK, profile.provider());
        assertEquals("fb-user-1", profile.providerUserId());
        assertEquals("fb.user@example.com", profile.email());
        assertEquals("https://fb.cdn.example.com/pic.jpg", profile.avatarUrl());
    }

    @Test
    void facebookProfileShouldAllowMissingEmailForDownstreamValidation() {
        OAuth2User user = oauth2User(Map.of(
                "id", "fb-user-2",
                "name", "No Email User"
        ), "id");

        OAuthProfile profile = extractor.extract("facebook", user);

        assertEquals("fb-user-2", profile.providerUserId());
        assertNull(profile.email());
    }

    private static OAuth2User oauth2User(Map<String, Object> attributes, String nameAttributeKey) {
        return new DefaultOAuth2User(List.of(), new HashMap<>(attributes), nameAttributeKey);
    }
}
