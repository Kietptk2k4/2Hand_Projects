package com.twohands.notification_service.domain.notificationevent;

import java.util.Optional;
import java.util.UUID;

public interface NotificationEventRepository {

    NotificationEvent save(NotificationEvent event);

    Optional<NotificationEvent> findBySourceServiceAndSourceEventId(
            NotificationSourceService sourceService,
            UUID sourceEventId
    );

    Optional<NotificationEvent> findBySourceServiceAndEventKey(
            NotificationSourceService sourceService,
            String eventKey
    );
}
