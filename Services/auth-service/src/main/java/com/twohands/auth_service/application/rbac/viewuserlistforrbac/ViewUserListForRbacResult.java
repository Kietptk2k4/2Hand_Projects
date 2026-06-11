package com.twohands.auth_service.application.rbac.viewuserlistforrbac;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ViewUserListForRbacResult(
        List<Item> items,
        Pagination pagination
) {
    public record Item(
            UUID id,
            String email,
            String displayName,
            String status,
            List<String> roleCodes,
            Instant createdAt
    ) {
    }

    public record Pagination(
            int page,
            int size,
            long totalItems,
            int totalPages,
            boolean hasNext
    ) {
    }
}
