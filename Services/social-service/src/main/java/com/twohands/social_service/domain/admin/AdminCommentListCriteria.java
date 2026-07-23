package com.twohands.social_service.domain.admin;

import java.util.Optional;

public record AdminCommentListCriteria(
        Optional<String> status,
        Optional<String> moderationStatus,
        Optional<String> postId,
        Optional<String> query,
        AdminCommentListSortField sortField,
        int page,
        int size
) {
}
