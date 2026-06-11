package com.twohands.social_service.domain.admin;

import java.util.Optional;

public record AdminPostListCriteria(
        Optional<String> status,
        Optional<String> moderationStatus,
        Optional<String> query,
        AdminModerationListSortField sortField,
        int page,
        int size
) {
}
