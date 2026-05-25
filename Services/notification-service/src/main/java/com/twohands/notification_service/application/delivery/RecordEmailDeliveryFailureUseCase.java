package com.twohands.notification_service.application.delivery;

import com.twohands.notification_service.domain.delivery.EmailDeliveryRetryPolicy;
import com.twohands.notification_service.domain.delivery.EmailDeliveryRetryState;
import com.twohands.notification_service.domain.usernotification.NotificationDeliveryStatus;
import com.twohands.notification_service.domain.usernotification.UserNotification;
import com.twohands.notification_service.domain.usernotification.UserNotificationRepository;
import com.twohands.notification_service.exception.AppException;
import com.twohands.notification_service.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class RecordEmailDeliveryFailureUseCase {

    private final UserNotificationRepository userNotificationRepository;
    private final EmailDeliveryRetryMetadataCodec emailDeliveryRetryMetadataCodec;
    private final int defaultMaxRetryCount;

    public RecordEmailDeliveryFailureUseCase(
            UserNotificationRepository userNotificationRepository,
            EmailDeliveryRetryMetadataCodec emailDeliveryRetryMetadataCodec,
            @Value("${notification.workers.retry-delivery.max-retry-count:5}") int defaultMaxRetryCount
    ) {
        this.userNotificationRepository = userNotificationRepository;
        this.emailDeliveryRetryMetadataCodec = emailDeliveryRetryMetadataCodec;
        this.defaultMaxRetryCount = defaultMaxRetryCount;
    }

    @Transactional
    public UserNotification execute(RecordEmailDeliveryFailureCommand command) {
        validateCommand(command);

        UserNotification notification = userNotificationRepository.findById(command.userNotificationId())
                .orElseThrow(() -> new AppException(
                        ErrorCode.USER_NOTIFICATION_NOT_FOUND,
                        "User notification not found"
                ));

        if (!EmailDeliveryRetryPolicy.supportsEmailRetry(notification.type())) {
            throw new AppException(
                    ErrorCode.VALIDATION_ERROR,
                    "Validation failed",
                    "eventType",
                    "Event type does not support email delivery retry."
            );
        }

        EmailDeliveryRetryState retryState = EmailDeliveryRetryPolicy.initialFailure(
                command.failurePolicy(),
                command.failureReason(),
                Instant.now(),
                defaultMaxRetryCount
        );

        UserNotification updated = new UserNotification(
                notification.id(),
                notification.notificationEventId(),
                notification.userId(),
                notification.actorId(),
                notification.type(),
                notification.title(),
                notification.content(),
                notification.referenceType(),
                notification.referenceId(),
                notification.read(),
                notification.deleted(),
                emailDeliveryRetryMetadataCodec.mergeEmailDeliveryState(notification.metadata(), retryState),
                NotificationDeliveryStatus.FAILED,
                notification.createdAt(),
                notification.readAt()
        );

        return userNotificationRepository.save(updated);
    }

    private void validateCommand(RecordEmailDeliveryFailureCommand command) {
        if (command.userNotificationId() == null) {
            throw new AppException(
                    ErrorCode.VALIDATION_ERROR,
                    "Validation failed",
                    "userNotificationId",
                    "User notification id is required."
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
    }
}
