package com.twohands.auth_service.delivery.http.users.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.List;

public record TrackLoginHistoryResponse(
        List<Item> items,
        int limit,
        int offset
) {
    public record Item(
            String id,
            @JsonProperty("login_method")
            String loginMethod,
            @JsonProperty("ip_address")
            String ipAddress,
            @JsonProperty("user_agent")
            String userAgent,
            boolean success,
            @JsonProperty("created_at")
            Instant createdAt
    ) {
    }
}
