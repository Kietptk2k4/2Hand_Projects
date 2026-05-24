package com.twohands.notification_service.delivery.http.notification.response;

public record PageMetaResponse(
        long page,
        long size,
        long totalElements,
        long totalPages,
        boolean hasNext
) {
}
