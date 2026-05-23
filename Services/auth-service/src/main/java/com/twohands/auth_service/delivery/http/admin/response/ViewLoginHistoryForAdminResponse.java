package com.twohands.auth_service.delivery.http.admin.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.List;

public record ViewLoginHistoryForAdminResponse(
        @JsonProperty("user_id")
        String userId,
        List<ItemData> items,
        PaginationData pagination
) {
    public record ItemData(
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

    public record PaginationData(
            int page,
            int limit,
            @JsonProperty("total_items")
            long totalItems,
            @JsonProperty("total_pages")
            int totalPages,
            @JsonProperty("has_next")
            boolean hasNext
    ) {
    }
}
