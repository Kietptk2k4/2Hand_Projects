package com.twohands.notification_service.application.delivery;

import com.twohands.notification_service.domain.delivery.PushDeliveryRetryPolicy;
import com.twohands.notification_service.domain.delivery.PushDeliveryRetryState;
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
public class RecordPushDeliveryFailureUseCase {

    private final UserNotificationRepository userNotificationRepository;
    private final PushDeliveryRetryMetadataCodec pushDeliveryRetryMetadataCodec;
    private final int defaultMaxRetryCount;

    public RecordPushDeliveryFailureUseCase(
            UserNotificationRepository userNotificationRepository,
            PushDeliveryRetryMetadataCodec pushDeliveryRetryMetadataCodec,
            @Value("${notification.workers.retry-delivery.max-retry-count:5}") int defaultMaxRetryCount
    ) {
        this.userNotificationRepository = userNotificationRepository;
        this.pushDeliveryRetryMetadataCodec = pushDeliveryRetryMetadataCodec;
        this.defaultMaxRetryCount = defaultMaxRetryCount;
    }

    @Transactional
    public UserNotification execute(RecordPushDeliveryFailureCommand command) {
        validateCommand(command);

        UserNotification notification = userNotificationRepository.findById(command.userNotificationId())
                .orElseThrow(() -> new AppException(
                        ErrorCode.USER_NOTIFICATION_NOT_FOUND,
                        "User notification not found"
                ));

        if (!PushDeliveryRetryPolicy.supportsPushRetry(notification.type())) {
            throw new AppException(
                    ErrorCode.VALIDATION_ERROR,
                    "Validation failed",
                    "eventType",
                    "Event type does not support push delivery retry."
            );
        }

        PushDeliveryRetryState retryState = PushDeliveryRetryPolicy.initialFailure(
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
                pushDeliveryRetryMetadataCodec.mergePushDeliveryState(notification.metadata(), retryState),
                NotificationDeliveryStatus.FAILED,
                notification.createdAt(),
                notification.readAt()
        );

        return userNotificationRepository.save(updated);
    }

    private void validateCommand(RecordPushDeliveryFailureCommand command) {
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
