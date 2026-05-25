package com.twohands.notification_service.application.ingest;

import com.twohands.notification_service.application.email.AccountEnforcementEmailPayloadNormalizer;
import com.twohands.notification_service.application.email.AuthSecurityEmailNotificationPayloadNormalizer;
import com.twohands.notification_service.application.email.CommerceOrderNotificationPayloadNormalizer;
import com.twohands.notification_service.application.idempotency.EnsureNotificationEventIdempotencyUseCase;
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
    private final AuthSecurityEmailNotificationPayloadNormalizer authSecurityEmailPayloadNormalizer;
    private final AccountEnforcementEmailPayloadNormalizer accountEnforcementEmailPayloadNormalizer;
    private final CommerceOrderNotificationPayloadNormalizer commerceOrderNotificationPayloadNormalizer;
    private final NotificationEventPayloadSanitizer payloadSanitizer;
    private final EnsureNotificationEventIdempotencyUseCase ensureNotificationEventIdempotencyUseCase;

    public StoreNotificationEventUseCase(
            NotificationEventRepository notificationEventRepository,
            AuthSecurityEmailNotificationPayloadNormalizer authSecurityEmailPayloadNormalizer,
            AccountEnforcementEmailPayloadNormalizer accountEnforcementEmailPayloadNormalizer,
            CommerceOrderNotificationPayloadNormalizer commerceOrderNotificationPayloadNormalizer,
            NotificationEventPayloadSanitizer payloadSanitizer,
            EnsureNotificationEventIdempotencyUseCase ensureNotificationEventIdempotencyUseCase
    ) {
        this.notificationEventRepository = notificationEventRepository;
        this.authSecurityEmailPayloadNormalizer = authSecurityEmailPayloadNormalizer;
        this.accountEnforcementEmailPayloadNormalizer = accountEnforcementEmailPayloadNormalizer;
        this.commerceOrderNotificationPayloadNormalizer = commerceOrderNotificationPayloadNormalizer;
        this.payloadSanitizer = payloadSanitizer;
        this.ensureNotificationEventIdempotencyUseCase = ensureNotificationEventIdempotencyUseCase;
    }

    @Transactional
    public IngestNotificationEventResult execute(NotificationEventIngestCommand command) {
        ensureNotificationEventIdempotencyUseCase.validateIdempotencyKeyPresent(command);
        validateEventType(command.eventType());

        String normalizedPayload = authSecurityEmailPayloadNormalizer.normalizeForStorage(
                command.eventType(),
                command.payload()
        );
        normalizedPayload = accountEnforcementEmailPayloadNormalizer.normalizeForStorage(
                command.eventType(),
                normalizedPayload
        );
        normalizedPayload = commerceOrderNotificationPayloadNormalizer.normalizeForStorage(
                command.eventType(),
                normalizedPayload
        );
        String sanitizedPayload = payloadSanitizer.sanitize(normalizedPayload);

        Optional<NotificationEvent> existing = ensureNotificationEventIdempotencyUseCase.findExisting(command);
        if (existing.isPresent()) {
            return new IngestNotificationEventResult(existing.get().id(), true);
        }

        NotificationEvent pendingEvent = new NotificationEvent(
                UUID.randomUUID(),
                command.sourceEventId(),
                ensureNotificationEventIdempotencyUseCase.toKey(command).normalizedEventKey(),
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
            Optional<NotificationEvent> duplicate = ensureNotificationEventIdempotencyUseCase.findExisting(command);
            if (duplicate.isPresent()) {
                return new IngestNotificationEventResult(duplicate.get().id(), true);
            }
            throw ex;
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
}
