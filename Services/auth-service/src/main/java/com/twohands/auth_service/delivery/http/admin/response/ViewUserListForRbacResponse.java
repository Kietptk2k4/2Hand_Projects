package com.twohands.auth_service.delivery.http.admin.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.List;

public record ViewUserListForRbacResponse(
        List<ItemData> items,
        PaginationData pagination
) {
    public record ItemData(
            String id,
            String email,
            @JsonProperty("display_name")
            String displayName,
            String status,
            @JsonProperty("role_codes")
            List<String> roleCodes,
            @JsonProperty("created_at")
            Instant createdAt
    ) {
    }

    public record PaginationData(
            int page,
            int size,
            @JsonProperty("total_items")
            long totalItems,
            @JsonProperty("total_pages")
            int totalPages,
            @JsonProperty("has_next")
            boolean hasNext
    ) {
    }
}
