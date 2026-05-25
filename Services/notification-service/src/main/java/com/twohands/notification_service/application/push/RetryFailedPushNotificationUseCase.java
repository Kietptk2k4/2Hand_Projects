package com.twohands.notification_service.application.push;

import com.twohands.notification_service.application.delivery.PushDeliveryRetryMetadataCodec;
import com.twohands.notification_service.application.worker.NotificationFailurePolicy;
import com.twohands.notification_service.domain.delivery.PushDeliveryRetryBackoffPolicy;
import com.twohands.notification_service.domain.delivery.PushDeliveryRetryPolicy;
import com.twohands.notification_service.domain.delivery.PushDeliveryRetryState;
import com.twohands.notification_service.domain.usernotification.NotificationDeliveryStatus;
import com.twohands.notification_service.domain.usernotification.UserNotification;
import com.twohands.notification_service.domain.usernotification.UserNotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class RetryFailedPushNotificationUseCase {

    private static final Logger log = LoggerFactory.getLogger(RetryFailedPushNotificationUseCase.class);

    private final UserNotificationRepository userNotificationRepository;
    private final SendPushNotificationUseCase sendPushNotificationUseCase;
    private final PushDeliveryRetryMetadataCodec pushDeliveryRetryMetadataCodec;
    private final int baseBackoffSeconds;
    private final int maxBackoffSeconds;

    public RetryFailedPushNotificationUseCase(
            UserNotificationRepository userNotificationRepository,
            SendPushNotificationUseCase sendPushNotificationUseCase,
            PushDeliveryRetryMetadataCodec pushDeliveryRetryMetadataCodec,
            @Value("${notification.workers.retry-delivery.base-backoff-seconds:30}") int baseBackoffSeconds,
            @Value("${notification.workers.retry-delivery.max-backoff-seconds:3600}") int maxBackoffSeconds
    ) {
        this.userNotificationRepository = userNotificationRepository;
        this.sendPushNotificationUseCase = sendPushNotificationUseCase;
        this.pushDeliveryRetryMetadataCodec = pushDeliveryRetryMetadataCodec;
        this.baseBackoffSeconds = baseBackoffSeconds;
        this.maxBackoffSeconds = maxBackoffSeconds;
    }

    public int execute(int batchSize) {
        if (batchSize <= 0) {
            return 0;
        }

        Instant now = Instant.now();
        List<UserNotification> candidates = userNotificationRepository.findFailedDeliveryCandidates(batchSize);
        List<UserNotification> eligible = new ArrayList<>();
        for (UserNotification candidate : candidates) {
            if (candidate.deleted()) {
                continue;
            }
            if (!PushDeliveryRetryPolicy.supportsPushRetry(candidate.type())) {
                continue;
            }
            PushDeliveryRetryState retryState = pushDeliveryRetryMetadataCodec.parse(candidate.metadata())
                    .orElse(null);
            if (!PushDeliveryRetryBackoffPolicy.isEligibleForRetry(
                    retryState,
                    now,
                    baseBackoffSeconds,
                    maxBackoffSeconds
            )) {
                continue;
            }
            eligible.add(candidate);
        }

        int processed = 0;
        for (UserNotification notification : eligible) {
            retrySingle(notification, now);
            processed++;
        }
        return processed;
    }

    private void retrySingle(UserNotification notification, Instant attemptedAt) {
        PushDeliveryRetryState currentState = pushDeliveryRetryMetadataCodec.parse(notification.metadata())
                .orElse(null);
        if (currentState == null) {
            return;
        }

        SendPushNotificationResult result = sendPushNotificationUseCase.execute(
                new SendPushNotificationCommand(
                        notification.userId(),
                        notification.type(),
                        notification.referenceType(),
                        notification.referenceId(),
                        notification.notificationEventId()
                )
        );

        switch (result.outcome()) {
            case SENT -> {
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
                        pushDeliveryRetryMetadataCodec.clearPushDeliveryState(notification.metadata()),
                        NotificationDeliveryStatus.SENT,
                        notification.createdAt(),
                        notification.readAt()
                );
                userNotificationRepository.save(updated);
                log.info(
                        "Push delivery retry succeeded notificationId={} eventType={} sentTokenCount={}",
                        notification.id(),
                        notification.type(),
                        result.sentTokenCount()
                );
            }
            case SKIPPED -> {
                PushDeliveryRetryState stopped = PushDeliveryRetryPolicy.afterPermanentFailure(
                        currentState,
                        result.failureReason() != null
                                ? result.failureReason()
                                : "Push channel is no longer eligible.",
                        attemptedAt
                );
                persistRetryState(notification, stopped);
                log.info(
                        "Push delivery retry stopped notificationId={} eventType={} reason={}",
                        notification.id(),
                        notification.type(),
                        stopped.lastError()
                );
            }
            case FAILED -> {
                PushDeliveryRetryState nextState;
                if (result.failurePolicy() == NotificationFailurePolicy.PERMANENT) {
                    nextState = PushDeliveryRetryPolicy.afterPermanentFailure(
                            currentState,
                            result.failureReason(),
                            attemptedAt
                    );
                } else {
                    nextState = PushDeliveryRetryPolicy.afterRetryableFailure(
                            currentState,
                            result.failureReason(),
                            attemptedAt
                    );
                }
                persistRetryState(notification, nextState);
                log.warn(
                        "Push delivery retry failed notificationId={} eventType={} policy={} reason={}",
                        notification.id(),
                        notification.type(),
                        nextState.failurePolicy(),
                        nextState.lastError()
                );
            }
            default -> {
                // no-op
            }
        }
    }

    private void persistRetryState(UserNotification notification, PushDeliveryRetryState retryState) {
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
        userNotificationRepository.save(updated);
    }
}
