package com.twohands.notification_service.domain.devicetoken;

import java.util.Locale;
import java.util.Optional;

public final class RegisterDeviceTokenPolicy {

    private static final int MAX_TOKEN_LENGTH = 512;

    private RegisterDeviceTokenPolicy() {
    }

    public static RegisterDeviceTokenDecision resolve(
            DeviceType deviceType,
            String deviceToken,
            Optional<UserDeviceToken> existingToken,
            java.util.UUID currentUserId
    ) {
        boolean alreadyRegistered = existingToken
                .filter(token -> token.userId().equals(currentUserId))
                .filter(token -> token.deviceType() == deviceType)
                .filter(UserDeviceToken::active)
                .isPresent();

        return new RegisterDeviceTokenDecision(deviceType, deviceToken, alreadyRegistered);
    }

    public static DeviceType parseDeviceType(String rawDeviceType) {
        if (rawDeviceType == null || rawDeviceType.isBlank()) {
            throw invalidDeviceType();
        }

        try {
            return DeviceType.valueOf(rawDeviceType.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw invalidDeviceType();
        }
    }

    public static String normalizeDeviceToken(String rawDeviceToken) {
        if (rawDeviceToken == null) {
            throw invalidDeviceToken("Device token must not be blank.");
        }

        String normalized = rawDeviceToken.trim();
        if (normalized.isEmpty()) {
            throw invalidDeviceToken("Device token must not be blank.");
        }
        if (normalized.length() > MAX_TOKEN_LENGTH) {
            throw invalidDeviceToken("Device token must not exceed 512 characters.");
        }
        return normalized;
    }

    public static String maskDeviceToken(String deviceToken) {
        if (deviceToken == null || deviceToken.length() <= 4) {
            return "****";
        }
        return "****" + deviceToken.substring(deviceToken.length() - 4);
    }

    private static IllegalArgumentException invalidDeviceType() {
        return new IllegalArgumentException("Invalid device type.");
    }

    private static IllegalArgumentException invalidDeviceToken(String reason) {
        return new IllegalArgumentException(reason);
    }
}
