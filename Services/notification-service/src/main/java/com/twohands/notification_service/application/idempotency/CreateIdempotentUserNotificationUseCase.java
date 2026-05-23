package com.twohands.notification_service.application.idempotency;

import com.twohands.notification_service.domain.idempotency.UserNotificationIdempotencyKey;
import com.twohands.notification_service.domain.usernotification.NotificationDeliveryStatus;
import com.twohands.notification_service.domain.usernotification.UserNotification;
import com.twohands.notification_service.domain.usernotification.UserNotificationRepository;
import com.twohands.notification_service.exception.AppException;
import com.twohands.notification_service.exception.ErrorCode;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class CreateIdempotentUserNotificationUseCase {

    private final UserNotificationRepository userNotificationRepository;

    public CreateIdempotentUserNotificationUseCase(UserNotificationRepository userNotificationRepository) {
        this.userNotificationRepository = userNotificationRepository;
    }

    @Transactional
    public CreateIdempotentUserNotificationResult execute(CreateIdempotentUserNotificationCommand command) {
        validateCommand(command);

        UserNotificationIdempotencyKey idempotencyKey = UserNotificationIdempotencyKey.of(
                command.notificationEventId(),
                command.userId(),
                command.type(),
                command.referenceType(),
                command.referenceId()
        );

        Optional<UserNotification> existing = userNotificationRepository.findByIdempotencyKey(idempotencyKey);
        if (existing.isPresent()) {
            return new CreateIdempotentUserNotificationResult(existing.get().id(), true);
        }

        UserNotification notification = new UserNotification(
                UUID.randomUUID(),
                command.notificationEventId(),
                command.userId(),
                command.actorId(),
                command.type(),
                command.title(),
                command.content(),
                idempotencyKey.referenceType(),
                idempotencyKey.referenceId(),
                false,
                false,
                normalizeMetadata(command.metadata()),
                resolveDeliveryStatus(command.deliveryStatus()),
                Instant.now(),
                null
        );

        try {
            UserNotification saved = userNotificationRepository.save(notification);
            return new CreateIdempotentUserNotificationResult(saved.id(), false);
        } catch (DataIntegrityViolationException ex) {
            Optional<UserNotification> duplicate = userNotificationRepository.findByIdempotencyKey(idempotencyKey);
            if (duplicate.isPresent()) {
                return new CreateIdempotentUserNotificationResult(duplicate.get().id(), true);
            }
            throw ex;
        }
    }

    private void validateCommand(CreateIdempotentUserNotificationCommand command) {
        if (command.notificationEventId() == null) {
            throw validationError("notificationEventId", "Notification event id is required.");
        }
        if (command.userId() == null) {
            throw validationError("userId", "User id is required.");
        }
        if (command.type() == null || command.type().isBlank()) {
            throw validationError("type", "Notification type must not be blank.");
        }
        if (command.title() == null || command.title().isBlank()) {
            throw validationError("title", "Title must not be blank.");
        }
        if (command.content() == null || command.content().isBlank()) {
            throw validationError("content", "Content must not be blank.");
        }
    }

    private String normalizeMetadata(String metadata) {
        if (metadata == null || metadata.isBlank()) {
            return "{}";
        }
        return metadata;
    }

    private NotificationDeliveryStatus resolveDeliveryStatus(NotificationDeliveryStatus deliveryStatus) {
        return deliveryStatus == null ? NotificationDeliveryStatus.PENDING : deliveryStatus;
    }

    private AppException validationError(String field, String reason) {
        return new AppException(ErrorCode.VALIDATION_ERROR, "Validation failed", field, reason);
    }
}
