package com.twohands.notification_service.application.worker;

import com.twohands.notification_service.application.handler.HandlerOutcome;
import com.twohands.notification_service.application.handler.NotificationEventHandler;
import com.twohands.notification_service.application.handler.NotificationEventHandlerRegistry;
import com.twohands.notification_service.application.handler.NotificationEventHandlerResult;
import com.twohands.notification_service.domain.notificationevent.NotificationEvent;
import com.twohands.notification_service.domain.notificationevent.NotificationEventProcessingPolicy;
import com.twohands.notification_service.domain.notificationevent.NotificationEventRepository;
import com.twohands.notification_service.exception.AppException;
import com.twohands.notification_service.exception.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProcessNotificationEventUseCase {

    private static final Logger log = LoggerFactory.getLogger(ProcessNotificationEventUseCase.class);

    private final NotificationEventRepository notificationEventRepository;
    private final NotificationEventHandlerRegistry handlerRegistry;
    private final MarkNotificationEventCompletedUseCase markNotificationEventCompletedUseCase;
    private final MarkNotificationEventFailedUseCase markNotificationEventFailedUseCase;

    public ProcessNotificationEventUseCase(
            NotificationEventRepository notificationEventRepository,
            NotificationEventHandlerRegistry handlerRegistry,
            MarkNotificationEventCompletedUseCase markNotificationEventCompletedUseCase,
            MarkNotificationEventFailedUseCase markNotificationEventFailedUseCase
    ) {
        this.notificationEventRepository = notificationEventRepository;
        this.handlerRegistry = handlerRegistry;
        this.markNotificationEventCompletedUseCase = markNotificationEventCompletedUseCase;
        this.markNotificationEventFailedUseCase = markNotificationEventFailedUseCase;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ProcessNotificationEventResult execute(ProcessNotificationEventCommand command) {
        validateCommand(command);

        NotificationEvent event = notificationEventRepository.findById(command.notificationEventId())
                .orElseThrow(() -> new AppException(
                        ErrorCode.NOTIFICATION_EVENT_NOT_FOUND,
                        "Notification event not found"
                ));

        if (NotificationEventProcessingPolicy.shouldSkipDuplicateProcessing(event)) {
            return new ProcessNotificationEventResult(event.id(), ProcessNotificationEventOutcome.SKIPPED);
        }

        try {
            return dispatch(event);
        } catch (Exception ex) {
            log.error("Unexpected error while processing notification event. eventId={}", event.id(), ex);
            markNotificationEventFailedUseCase.execute(new MarkNotificationEventFailedCommand(
                    event.id(),
                    "Unexpected processing error",
                    NotificationFailurePolicy.RETRYABLE
            ));
            return new ProcessNotificationEventResult(event.id(), ProcessNotificationEventOutcome.FAILED);
        }
    }

    private ProcessNotificationEventResult dispatch(NotificationEvent event) {
        NotificationEventHandler handler = handlerRegistry.resolve(event.eventType())
                .orElse(null);

        if (handler == null) {
            markNotificationEventFailedUseCase.execute(new MarkNotificationEventFailedCommand(
                    event.id(),
                    "Unsupported event type: " + event.eventType(),
                    NotificationFailurePolicy.PERMANENT
            ));
            return new ProcessNotificationEventResult(event.id(), ProcessNotificationEventOutcome.FAILED);
        }

        NotificationEventHandlerResult handlerResult = handler.handle(event);
        return switch (handlerResult.outcome()) {
            case SUCCESS, NO_OP -> {
                markNotificationEventCompletedUseCase.execute(
                        new MarkNotificationEventCompletedCommand(event.id())
                );
                yield new ProcessNotificationEventResult(event.id(), ProcessNotificationEventOutcome.COMPLETED);
            }
            case FAILURE -> {
                markNotificationEventFailedUseCase.execute(new MarkNotificationEventFailedCommand(
                        event.id(),
                        handlerResult.errorMessage(),
                        handlerResult.failurePolicy()
                ));
                yield new ProcessNotificationEventResult(event.id(), ProcessNotificationEventOutcome.FAILED);
            }
        };
    }

    private void validateCommand(ProcessNotificationEventCommand command) {
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
