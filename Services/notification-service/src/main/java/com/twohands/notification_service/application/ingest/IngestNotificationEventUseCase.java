package com.twohands.notification_service.application.ingest;

import org.springframework.stereotype.Service;

@Service
public class IngestNotificationEventUseCase {

    private final StoreNotificationEventUseCase storeNotificationEventUseCase;

    public IngestNotificationEventUseCase(StoreNotificationEventUseCase storeNotificationEventUseCase) {
        this.storeNotificationEventUseCase = storeNotificationEventUseCase;
    }

    public IngestNotificationEventResult execute(NotificationEventIngestCommand command) {
        return storeNotificationEventUseCase.execute(command);
    }
}
