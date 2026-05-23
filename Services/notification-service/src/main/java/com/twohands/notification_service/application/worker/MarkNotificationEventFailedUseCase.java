package com.twohands.notification_service.application.worker;

import com.twohands.notification_service.domain.idempotency.NotificationErrorSanitizer;
import com.twohands.notification_service.domain.notificationevent.NotificationEvent;
import com.twohands.notification_service.domain.notificationevent.NotificationEventProcessingPolicy;
import com.twohands.notification_service.domain.notificationevent.NotificationEventRepository;
import com.twohands.notification_service.domain.notificationevent.NotificationEventStatus;
import com.twohands.notification_service.exception.AppException;
import com.twohands.notification_service.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class MarkNotificationEventFailedUseCase {

    private final NotificationEventRepository notificationEventRepository;
    private final NotificationErrorSanitizer errorSanitizer;

    public MarkNotificationEventFailedUseCase(
            NotificationEventRepository notificationEventRepository,
            NotificationErrorSanitizer errorSanitizer
    ) {
        this.notificationEventRepository = notificationEventRepository;
        this.errorSanitizer = errorSanitizer;
    }

    @Transactional
    public MarkNotificationEventFailedResult execute(MarkNotificationEventFailedCommand command) {
        validateCommand(command);

        NotificationEvent event = notificationEventRepository.findById(command.notificationEventId())
                .orElseThrow(() -> new AppException(
                        ErrorCode.NOTIFICATION_EVENT_NOT_FOUND,
                        "Notification event not found"
                ));

        if (NotificationEventProcessingPolicy.isTerminal(event.status())) {
            return new MarkNotificationEventFailedResult(
                    event.id(),
                    event.retryCount(),
                    event.maxRetryCount(),
                    NotificationEventProcessingPolicy.isPermanentFailure(event),
                    false
            );
        }

        int retryCount = resolveRetryCount(event, command.failurePolicy());
        String sanitizedError = errorSanitizer.sanitize(command.errorMessage());
        Instant failedAt = Instant.now();
        NotificationEvent failedEvent = new NotificationEvent(
                event.id(),
                event.sourceEventId(),
                event.eventKey(),
                event.eventType(),
                event.sourceService(),
                event.aggregateType(),
                event.aggregateId(),
                event.actorId(),
                event.recipientUserId(),
                event.payload(),
                NotificationEventStatus.FAILED,
                retryCount,
                event.maxRetryCount(),
                sanitizedError,
                failedAt,
                null,
                event.createdAt(),
                null
        );

        notificationEventRepository.save(failedEvent);

        return new MarkNotificationEventFailedResult(
                failedEvent.id(),
                failedEvent.retryCount(),
                failedEvent.maxRetryCount(),
                retryCount >= failedEvent.maxRetryCount(),
                true
        );
    }

    private void validateCommand(MarkNotificationEventFailedCommand command) {
        if (command.notificationEventId() == null) {
            throw new AppException(
                    ErrorCode.VALIDATION_ERROR,
                    "Validation failed",
                    "notificationEventId",
                    "Notification event id is required."
            );
        }
        if (command.failurePolicy() == null) {
            throw new AppException(
                    ErrorCode.VALIDATION_ERROR,
                    "Validation failed",
                    "failurePolicy",
                    "Failure policy is required."
            );
        }
        if (command.errorMessage() == null || command.errorMessage().isBlank()) {
            throw new AppException(
                    ErrorCode.VALIDATION_ERROR,
                    "Validation failed",
                    "errorMessage",
                    "Error message is required."
            );
        }
    }

    private int resolveRetryCount(NotificationEvent event, NotificationFailurePolicy failurePolicy) {
        if (failurePolicy == NotificationFailurePolicy.PERMANENT) {
            return event.maxRetryCount();
        }
        return Math.min(event.retryCount() + 1, event.maxRetryCount());
    }
}
