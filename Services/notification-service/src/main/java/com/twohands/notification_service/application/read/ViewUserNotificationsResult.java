package com.twohands.notification_service.application.read;

import com.twohands.notification_service.domain.common.PageResult;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ViewUserNotificationsResult(
        List<UserNotificationItem> items,
        PageMeta meta
) {

    public static ViewUserNotificationsResult from(PageResult<UserNotificationItem> pageResult) {
        return new ViewUserNotificationsResult(
                pageResult.items(),
                new PageMeta(
                        pageResult.page(),
                        pageResult.size(),
                        pageResult.totalElements(),
                        pageResult.totalPages(),
                        pageResult.hasNext()
                )
        );
    }

    public record UserNotificationItem(
            UUID id,
            UUID actorId,
            String type,
            String title,
            String content,
            String referenceType,
            String referenceId,
            String metadata,
            boolean read,
            Instant readAt,
            Instant createdAt
    ) {
    }

    public record PageMeta(
            long page,
            long size,
            long totalElements,
            long totalPages,
            boolean hasNext
    ) {
    }
}
