package com.twohands.notification_service.application.ingest;

import com.twohands.notification_service.infrastructure.messaging.NotificationEventConsumer;
import org.springframework.stereotype.Service;

@Service
public class IngestNotificationEventUseCase {

    private final NotificationEventConsumer notificationEventConsumer;

    public IngestNotificationEventUseCase(NotificationEventConsumer notificationEventConsumer) {
        this.notificationEventConsumer = notificationEventConsumer;
    }

    public IngestNotificationEventResult execute(NotificationEventIngestCommand command) {
        return notificationEventConsumer.consume(command);
    }
}
