package com.twohands.auth_service.domain.rbac;

import java.util.Optional;

public record RbacUserListCriteria(
        Optional<String> status,
        Optional<String> emailFragment,
        RbacUserListSortField sortField,
        int page,
        int size
) {
}
