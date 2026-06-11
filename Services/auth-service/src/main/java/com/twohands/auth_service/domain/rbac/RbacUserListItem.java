package com.twohands.auth_service.domain.rbac;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record RbacUserListItem(
        UUID id,
        String email,
        String displayName,
        String status,
        List<String> roleCodes,
        Instant createdAt
) {
}
