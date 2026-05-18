package com.twohands.social_service.domain.post;

import java.util.List;

public record PageResult<T>(
        List<T> items,
        long page,
        long size,
        long totalElements,
        long totalPages,
        boolean hasNext
) {
}
