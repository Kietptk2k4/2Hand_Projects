package com.twohands.auth_service.domain.rbac;

import java.util.List;

public record RbacUserListPagedResult(
        List<RbacUserListItem> items,
        int page,
        int size,
        long totalItems,
        int totalPages,
        boolean hasNext
) {
}
