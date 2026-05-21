package com.twohands.notification_service.infrastructure.messaging;

import com.twohands.notification_service.application.ingest.IngestNotificationEventResult;
import com.twohands.notification_service.application.ingest.NotificationEventIngestCommand;

public interface NotificationEventConsumer {

    IngestNotificationEventResult consume(NotificationEventIngestCommand command);
}
