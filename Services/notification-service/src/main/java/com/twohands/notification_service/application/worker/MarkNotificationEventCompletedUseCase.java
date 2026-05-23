package com.twohands.notification_service.application.worker;

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
public class MarkNotificationEventCompletedUseCase {

    private final NotificationEventRepository notificationEventRepository;

    public MarkNotificationEventCompletedUseCase(NotificationEventRepository notificationEventRepository) {
        this.notificationEventRepository = notificationEventRepository;
    }

    @Transactional
    public MarkNotificationEventCompletedResult execute(MarkNotificationEventCompletedCommand command) {
        validateCommand(command);

        NotificationEvent event = notificationEventRepository.findById(command.notificationEventId())
                .orElseThrow(() -> new AppException(
                        ErrorCode.NOTIFICATION_EVENT_NOT_FOUND,
                        "Notification event not found"
                ));

        if (NotificationEventProcessingPolicy.isTerminal(event.status())) {
            return new MarkNotificationEventCompletedResult(
                    event.id(),
                    event.processedAt(),
                    false
            );
        }

        Instant processedAt = Instant.now();
        NotificationEvent completedEvent = new NotificationEvent(
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
                NotificationEventStatus.COMPLETED,
                event.retryCount(),
                event.maxRetryCount(),
                null,
                null,
                null,
                event.createdAt(),
                processedAt
        );

        notificationEventRepository.save(completedEvent);

        return new MarkNotificationEventCompletedResult(
                completedEvent.id(),
                processedAt,
                true
        );
    }

    private void validateCommand(MarkNotificationEventCompletedCommand command) {
        if (command.notificationEventId() == null) {
            throw new AppException(
                    ErrorCode.VALIDATION_ERROR,
                    "Validation failed",
                    "notificationEventId",
                    "Notification event id is required."
            );
        }
    }
}
