package com.twohands.notification_service.application.idempotency;

import com.twohands.notification_service.application.ingest.NotificationEventIngestCommand;
import com.twohands.notification_service.domain.idempotency.NotificationEventIdempotencyKey;
import com.twohands.notification_service.domain.notificationevent.NotificationEvent;
import com.twohands.notification_service.domain.notificationevent.NotificationEventRepository;
import com.twohands.notification_service.exception.AppException;
import com.twohands.notification_service.exception.ErrorCode;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class EnsureNotificationEventIdempotencyUseCase {

    private final NotificationEventRepository notificationEventRepository;

    public EnsureNotificationEventIdempotencyUseCase(NotificationEventRepository notificationEventRepository) {
        this.notificationEventRepository = notificationEventRepository;
    }

    public void validateIdempotencyKeyPresent(NotificationEventIngestCommand command) {
        NotificationEventIdempotencyKey key = toKey(command);
        if (!key.isPresent()) {
            throw new AppException(
                    ErrorCode.MISSING_IDEMPOTENCY_KEY,
                    "Validation failed",
                    "sourceEventId",
                    "Either sourceEventId or eventKey is required."
            );
        }
    }

    public Optional<NotificationEvent> findExisting(NotificationEventIngestCommand command) {
        return findExisting(toKey(command));
    }

    public Optional<NotificationEvent> findExisting(NotificationEventIdempotencyKey key) {
        if (key.hasSourceEventId()) {
            Optional<NotificationEvent> bySourceEventId = notificationEventRepository
                    .findBySourceServiceAndSourceEventId(key.sourceService(), key.sourceEventId());
            if (bySourceEventId.isPresent()) {
                return bySourceEventId;
            }
        }
        String eventKey = key.normalizedEventKey();
        if (eventKey != null) {
            return notificationEventRepository.findBySourceServiceAndEventKey(key.sourceService(), eventKey);
        }
        return Optional.empty();
    }

    public NotificationEventIdempotencyKey toKey(NotificationEventIngestCommand command) {
        return new NotificationEventIdempotencyKey(
                command.sourceService(),
                command.sourceEventId(),
                command.eventKey()
        );
    }
}
