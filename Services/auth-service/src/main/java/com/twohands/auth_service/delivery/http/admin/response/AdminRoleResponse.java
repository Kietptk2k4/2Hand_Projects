package com.twohands.auth_service.delivery.http.admin.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

public record AdminRoleResponse(
        String id,
        String code,
        String name,
        @JsonProperty("created_at")
        Instant createdAt,
        @JsonProperty("updated_at")
        Instant updatedAt
) {
}
