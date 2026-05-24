package com.twohands.notification_service.domain.common;

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
