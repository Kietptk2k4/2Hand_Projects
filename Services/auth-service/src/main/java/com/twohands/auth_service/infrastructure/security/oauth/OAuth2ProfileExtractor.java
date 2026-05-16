package com.twohands.auth_service.infrastructure.security.oauth;

import com.twohands.auth_service.application.auth.oauth.OAuthProfile;
import com.twohands.auth_service.domain.oauth.OAuthProvider;
import com.twohands.auth_service.exception.AppException;
import com.twohands.auth_service.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;

@Component
public class OAuth2ProfileExtractor {

    private final String googleClientId;
    private final String facebookClientId;

    public OAuth2ProfileExtractor(
            @Value("${spring.security.oauth2.client.registration.google.client-id:}") String googleClientId,
            @Value("${spring.security.oauth2.client.registration.facebook.client-id:}") String facebookClientId
    ) {
        this.googleClientId = googleClientId;
        this.facebookClientId = facebookClientId;
    }

    public OAuthProfile extract(String registrationId, OAuth2User oauth2User) {
        OAuthProvider provider = parseProvider(registrationId);
        validateAudience(provider, oauth2User.getAttributes());
        String providerUserId = readString(oauth2User.getAttributes(), "id");
        String email = readString(oauth2User.getAttributes(), "email");
        String name = readString(oauth2User.getAttributes(), "name");
        String avatarUrl = extractAvatar(provider, oauth2User.getAttributes());

        if (provider == OAuthProvider.GOOGLE) {
            if (providerUserId == null || providerUserId.isBlank()) {
                providerUserId = readString(oauth2User.getAttributes(), "sub");
            }
        }
        if (providerUserId == null || providerUserId.isBlank()) {
            throw new AppException(ErrorCode.OAUTH_PROVIDER_PROFILE_INVALID, "Xac thuc OAuth that bai.");
        }

        return new OAuthProfile(provider, providerUserId, email, name, avatarUrl);
    }

    private OAuthProvider parseProvider(String registrationId) {
        if (registrationId == null) {
            throw new AppException(ErrorCode.OAUTH_PROVIDER_PROFILE_INVALID, "Xac thuc OAuth that bai.");
        }
        return switch (registrationId.toLowerCase()) {
            case "google" -> OAuthProvider.GOOGLE;
            case "facebook" -> OAuthProvider.FACEBOOK;
            default -> throw new AppException(ErrorCode.OAUTH_PROVIDER_PROFILE_INVALID, "Xac thuc OAuth that bai.");
        };
    }

    private String extractAvatar(OAuthProvider provider, Map<String, Object> attributes) {
        if (provider == OAuthProvider.GOOGLE) {
            return readString(attributes, "picture");
        }
        Object picture = attributes.get("picture");
        if (!(picture instanceof Map<?, ?> pictureMap)) {
            return null;
        }
        Object data = pictureMap.get("data");
        if (!(data instanceof Map<?, ?> dataMap)) {
            return null;
        }
        Object url = dataMap.get("url");
        return url == null ? null : String.valueOf(url);
    }

    private String readString(Map<String, Object> attributes, String key) {
        Object value = attributes.get(key);
        if (value == null) {
            return null;
        }
        String asString = String.valueOf(value).trim();
        return asString.isBlank() ? null : asString;
    }

    private void validateAudience(OAuthProvider provider, Map<String, Object> attributes) {
        if (provider == OAuthProvider.GOOGLE) {
            if (googleClientId == null || googleClientId.isBlank()) {
                return;
            }
            String azp = readString(attributes, "azp");
            if (azp != null && !googleClientId.equals(azp)) {
                throw new AppException(ErrorCode.OAUTH_PROVIDER_PROFILE_INVALID, "Xac thuc OAuth that bai.");
            }
            Object aud = attributes.get("aud");
            if (aud instanceof String audString && !googleClientId.equals(audString.trim())) {
                throw new AppException(ErrorCode.OAUTH_PROVIDER_PROFILE_INVALID, "Xac thuc OAuth that bai.");
            }
            if (aud instanceof Collection<?> audCollection) {
                boolean match = audCollection.stream().map(String::valueOf).anyMatch(googleClientId::equals);
                if (!match) {
                    throw new AppException(ErrorCode.OAUTH_PROVIDER_PROFILE_INVALID, "Xac thuc OAuth that bai.");
                }
            }
            return;
        }
        if (facebookClientId == null || facebookClientId.isBlank()) {
            return;
        }
        String appId = readString(attributes, "app_id");
        if (appId != null && !facebookClientId.equals(appId)) {
            throw new AppException(ErrorCode.OAUTH_PROVIDER_PROFILE_INVALID, "Xac thuc OAuth that bai.");
        }
    }
}
