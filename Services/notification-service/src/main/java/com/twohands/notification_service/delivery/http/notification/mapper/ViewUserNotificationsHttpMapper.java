package com.twohands.notification_service.delivery.http.notification.mapper;

import com.twohands.notification_service.application.read.ViewUserNotificationsResult;
import com.twohands.notification_service.delivery.http.notification.response.PageMetaResponse;
import com.twohands.notification_service.delivery.http.notification.response.UserNotificationItemResponse;
import com.twohands.notification_service.delivery.http.notification.response.ViewUserNotificationsResponse;
import org.springframework.stereotype.Component;

@Component
public class ViewUserNotificationsHttpMapper {

    public ViewUserNotificationsResponse toResponse(ViewUserNotificationsResult result) {
        return new ViewUserNotificationsResponse(
                result.items().stream().map(this::toItemResponse).toList(),
                toMetaResponse(result.meta())
        );
    }

    private UserNotificationItemResponse toItemResponse(ViewUserNotificationsResult.UserNotificationItem item) {
        return new UserNotificationItemResponse(
                item.id(),
                item.actorId(),
                item.type(),
                item.title(),
                item.content(),
                item.referenceType(),
                item.referenceId(),
                item.metadata(),
                item.read(),
                item.readAt(),
                item.createdAt()
        );
    }

    private PageMetaResponse toMetaResponse(ViewUserNotificationsResult.PageMeta meta) {
        return new PageMetaResponse(
                meta.page(),
                meta.size(),
                meta.totalElements(),
                meta.totalPages(),
                meta.hasNext()
        );
    }
}
