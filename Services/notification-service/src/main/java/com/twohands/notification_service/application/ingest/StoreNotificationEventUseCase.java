package com.twohands.notification_service.application.ingest;

import com.twohands.notification_service.domain.notificationevent.NotificationEvent;
import com.twohands.notification_service.domain.notificationevent.NotificationEventPayloadSanitizer;
import com.twohands.notification_service.domain.notificationevent.NotificationEventRepository;
import com.twohands.notification_service.domain.notificationevent.NotificationEventStatus;
import com.twohands.notification_service.exception.AppException;
import com.twohands.notification_service.exception.ErrorCode;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class StoreNotificationEventUseCase {

    private static final int DEFAULT_MAX_RETRY_COUNT = 5;
    private static final Pattern EVENT_TYPE_PATTERN = Pattern.compile("^[A-Z][A-Z0-9_]*$");

    private final NotificationEventRepository notificationEventRepository;
    private final NotificationEventPayloadSanitizer payloadSanitizer;

    public StoreNotificationEventUseCase(
            NotificationEventRepository notificationEventRepository,
            NotificationEventPayloadSanitizer payloadSanitizer
    ) {
        this.notificationEventRepository = notificationEventRepository;
        this.payloadSanitizer = payloadSanitizer;
    }

    @Transactional
    public IngestNotificationEventResult execute(NotificationEventIngestCommand command) {
        validateIdempotencyKey(command);
        validateEventType(command.eventType());

        String sanitizedPayload = payloadSanitizer.sanitize(command.payload());

        Optional<NotificationEvent> existing = findExisting(command);
        if (existing.isPresent()) {
            return new IngestNotificationEventResult(existing.get().id(), true);
        }

        NotificationEvent pendingEvent = new NotificationEvent(
                UUID.randomUUID(),
                command.sourceEventId(),
                normalizeEventKey(command.eventKey()),
                command.eventType(),
                command.sourceService(),
                command.aggregateType(),
                command.aggregateId(),
                command.actorId(),
                command.recipientUserId(),
                sanitizedPayload,
                NotificationEventStatus.PENDING,
                0,
                DEFAULT_MAX_RETRY_COUNT,
                null,
                null,
                null,
                Instant.now(),
                null
        );

        try {
            NotificationEvent saved = notificationEventRepository.save(pendingEvent);
            return new IngestNotificationEventResult(saved.id(), false);
        } catch (DataIntegrityViolationException ex) {
            Optional<NotificationEvent> duplicate = findExisting(command);
            if (duplicate.isPresent()) {
                return new IngestNotificationEventResult(duplicate.get().id(), true);
            }
            throw ex;
        }
    }

    private void validateIdempotencyKey(NotificationEventIngestCommand command) {
        boolean hasSourceEventId = command.sourceEventId() != null;
        boolean hasEventKey = command.eventKey() != null && !command.eventKey().isBlank();
        if (!hasSourceEventId && !hasEventKey) {
            throw new AppException(
                    ErrorCode.MISSING_IDEMPOTENCY_KEY,
                    "Validation failed",
                    "sourceEventId",
                    "Either sourceEventId or eventKey is required."
            );
        }
    }

    private void validateEventType(String eventType) {
        if (eventType == null || eventType.isBlank()) {
            throw new AppException(
                    ErrorCode.VALIDATION_ERROR,
                    "Validation failed",
                    "eventType",
                    "Event type must not be blank."
            );
        }
        if (!EVENT_TYPE_PATTERN.matcher(eventType).matches()) {
            throw new AppException(
                    ErrorCode.VALIDATION_ERROR,
                    "Validation failed",
                    "eventType",
                    "Event type must use UPPER_SNAKE_CASE."
            );
        }
    }

    private Optional<NotificationEvent> findExisting(NotificationEventIngestCommand command) {
        if (command.sourceEventId() != null) {
            Optional<NotificationEvent> bySourceEventId = notificationEventRepository
                    .findBySourceServiceAndSourceEventId(command.sourceService(), command.sourceEventId());
            if (bySourceEventId.isPresent()) {
                return bySourceEventId;
            }
        }
        String eventKey = normalizeEventKey(command.eventKey());
        if (eventKey != null) {
            return notificationEventRepository.findBySourceServiceAndEventKey(command.sourceService(), eventKey);
        }
        return Optional.empty();
    }

    private String normalizeEventKey(String eventKey) {
        if (eventKey == null || eventKey.isBlank()) {
            return null;
        }
        return eventKey.trim();
    }
}
