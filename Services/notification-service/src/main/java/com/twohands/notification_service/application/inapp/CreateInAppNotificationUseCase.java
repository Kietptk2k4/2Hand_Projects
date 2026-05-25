package com.twohands.notification_service.application.inapp;

import com.twohands.notification_service.application.idempotency.CreateIdempotentUserNotificationCommand;
import com.twohands.notification_service.application.idempotency.CreateIdempotentUserNotificationResult;
import com.twohands.notification_service.application.idempotency.CreateIdempotentUserNotificationUseCase;
import com.twohands.notification_service.domain.idempotency.UserNotificationIdempotencyKey;
import com.twohands.notification_service.domain.inapp.InAppNotificationTemplate;
import com.twohands.notification_service.domain.inapp.InAppNotificationTemplatePolicy;
import com.twohands.notification_service.domain.notificationevent.NotificationEventPayloadSanitizer;
import com.twohands.notification_service.domain.usernotification.NotificationDeliveryStatus;
import com.twohands.notification_service.exception.AppException;
import com.twohands.notification_service.exception.ErrorCode;
import org.springframework.stereotype.Service;

@Service
public class CreateInAppNotificationUseCase {

    private final NotificationEventPayloadSanitizer metadataSanitizer;
    private final CreateIdempotentUserNotificationUseCase createIdempotentUserNotificationUseCase;

    public CreateInAppNotificationUseCase(
            NotificationEventPayloadSanitizer metadataSanitizer,
            CreateIdempotentUserNotificationUseCase createIdempotentUserNotificationUseCase
    ) {
        this.metadataSanitizer = metadataSanitizer;
        this.createIdempotentUserNotificationUseCase = createIdempotentUserNotificationUseCase;
    }

    public CreateInAppNotificationResult execute(CreateInAppNotificationCommand command) {
        validateCommand(command);

        InAppNotificationTemplate template = InAppNotificationTemplatePolicy.resolve(
                        command.eventType(),
                        command.templateVariant()
                )
                .orElseThrow(() -> new AppException(
                        ErrorCode.UNKNOWN_EVENT_TYPE,
                        "In-app notification template not configured",
                        "eventType",
                        "Event type does not have an in-app notification template."
                ));

        String referenceType = UserNotificationIdempotencyKey.normalizeReference(command.referenceType());
        String referenceId = UserNotificationIdempotencyKey.normalizeReference(command.referenceId());
        String sanitizedMetadata = metadataSanitizer.sanitize(command.metadata());

        CreateIdempotentUserNotificationResult result = createIdempotentUserNotificationUseCase.execute(
                new CreateIdempotentUserNotificationCommand(
                        command.notificationEventId(),
                        command.userId(),
                        command.actorId(),
                        command.eventType(),
                        template.title(),
                        template.content(),
                        referenceType,
                        referenceId,
                        sanitizedMetadata,
                        NotificationDeliveryStatus.SENT
                )
        );

        return new CreateInAppNotificationResult(result.userNotificationId(), result.duplicate());
    }

    private void validateCommand(CreateInAppNotificationCommand command) {
        if (command.notificationEventId() == null) {
            throw validationError("notificationEventId", "Notification event id is required.");
        }
        if (command.userId() == null) {
            throw validationError("userId", "User id is required.");
        }
        if (command.eventType() == null || command.eventType().isBlank()) {
            throw validationError("eventType", "Event type must not be blank.");
        }
    }

    private AppException validationError(String field, String reason) {
        return new AppException(ErrorCode.VALIDATION_ERROR, "Validation failed", field, reason);
    }
}
