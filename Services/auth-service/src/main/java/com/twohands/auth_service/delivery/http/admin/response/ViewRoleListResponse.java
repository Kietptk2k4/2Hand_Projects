package com.twohands.auth_service.delivery.http.admin.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.List;

public record ViewRoleListResponse(
        List<RoleData> roles
) {
    public record RoleData(
            String id,
            String code,
            String name,
            @JsonProperty("created_at")
            Instant createdAt,
            @JsonProperty("updated_at")
            Instant updatedAt
    ) {
    }
}
