package com.twohands.auth_service.delivery.http.users.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.List;

public record ViewLoginSesssionListResponse(
        List<SessionData> sessions
) {
    public record SessionData(
            String id,
            @JsonProperty("device_id")
            String deviceId,
            @JsonProperty("ip_address")
            String ipAddress,
            @JsonProperty("user_agent")
            String userAgent,
            String status,
            @JsonProperty("created_at")
            Instant createdAt,
            @JsonProperty("updated_at")
            Instant updatedAt,
            @JsonProperty("expires_at")
            Instant expiresAt
    ) {
    }
}
