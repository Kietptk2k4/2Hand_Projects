package com.twohands.auth_service.delivery.http.admin.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.List;

public record ViewUserSessionsForAdminResponse(
        @JsonProperty("user_id")
        String userId,
        List<SessionData> sessions,
        PaginationData pagination
) {
    public record SessionData(
            @JsonProperty("session_id")
            String sessionId,
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
            Instant updatedAt
    ) {
    }

    public record PaginationData(
            int page,
            int limit,
            @JsonProperty("total_items")
            long totalItems,
            @JsonProperty("has_next")
            boolean hasNext
    ) {
    }
}
