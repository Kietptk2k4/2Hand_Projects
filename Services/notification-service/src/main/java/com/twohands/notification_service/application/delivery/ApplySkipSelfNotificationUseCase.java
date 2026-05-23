package com.twohands.notification_service.application.delivery;

import com.twohands.notification_service.domain.delivery.SkipSelfNotificationOutcome;
import com.twohands.notification_service.domain.delivery.SkipSelfNotificationPolicy;
import com.twohands.notification_service.exception.AppException;
import com.twohands.notification_service.exception.ErrorCode;
import org.springframework.stereotype.Service;

@Service
public class ApplySkipSelfNotificationUseCase {

    public SkipSelfNotificationOutcome execute(ApplySkipSelfNotificationCommand command) {
        validateCommand(command);

        return SkipSelfNotificationPolicy.evaluate(
                command.eventType(),
                command.sourceService(),
                command.actorId(),
                command.recipientId()
        );
    }

    private void validateCommand(ApplySkipSelfNotificationCommand command) {
        if (command.eventType() == null || command.eventType().isBlank()) {
            throw validationError("eventType", "Event type must not be blank.");
        }
        if (command.sourceService() == null) {
            throw validationError("sourceService", "Source service is required.");
        }
        if (command.recipientId() == null) {
            throw validationError("recipientId", "Recipient id is required.");
        }
    }

    private AppException validationError(String field, String reason) {
        return new AppException(ErrorCode.VALIDATION_ERROR, "Validation failed", field, reason);
    }
}
