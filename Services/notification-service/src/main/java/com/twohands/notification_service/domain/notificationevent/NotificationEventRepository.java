package com.twohands.notification_service.domain.notificationevent;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NotificationEventRepository {

    NotificationEvent save(NotificationEvent event);

    Optional<NotificationEvent> findById(UUID id);

    Optional<NotificationEvent> findBySourceServiceAndSourceEventId(
            NotificationSourceService sourceService,
            UUID sourceEventId
    );

    Optional<NotificationEvent> findBySourceServiceAndEventKey(
            NotificationSourceService sourceService,
            String eventKey
    );

    List<NotificationEvent> findStaleProcessingEvents(Instant lockedBefore, int limit);

    List<NotificationEvent> claimPendingEvents(int batchSize, String lockedBy);

    List<NotificationEvent> claimRetryableFailedEvents(
            int batchSize,
            String lockedBy,
            Instant now,
            int baseBackoffSeconds,
            int maxBackoffSeconds
    );
}
