package com.twohands.notification_service.application.email;

import com.twohands.notification_service.application.delivery.EmailDeliveryRetryMetadataCodec;
import com.twohands.notification_service.application.worker.NotificationFailurePolicy;
import com.twohands.notification_service.domain.delivery.EmailDeliveryRetryBackoffPolicy;
import com.twohands.notification_service.domain.delivery.EmailDeliveryRetryPolicy;
import com.twohands.notification_service.domain.delivery.EmailDeliveryRetryState;
import com.twohands.notification_service.domain.notificationevent.NotificationEvent;
import com.twohands.notification_service.domain.notificationevent.NotificationEventRepository;
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
public class RetryFailedEmailNotificationUseCase {

    private static final Logger log = LoggerFactory.getLogger(RetryFailedEmailNotificationUseCase.class);

    private final UserNotificationRepository userNotificationRepository;
    private final NotificationEventRepository notificationEventRepository;
    private final SendEmailNotificationUseCase sendEmailNotificationUseCase;
    private final EmailDeliveryRetryMetadataCodec emailDeliveryRetryMetadataCodec;
    private final int baseBackoffSeconds;
    private final int maxBackoffSeconds;

    public RetryFailedEmailNotificationUseCase(
            UserNotificationRepository userNotificationRepository,
            NotificationEventRepository notificationEventRepository,
            SendEmailNotificationUseCase sendEmailNotificationUseCase,
            EmailDeliveryRetryMetadataCodec emailDeliveryRetryMetadataCodec,
            @Value("${notification.workers.retry-delivery.base-backoff-seconds:30}") int baseBackoffSeconds,
            @Value("${notification.workers.retry-delivery.max-backoff-seconds:3600}") int maxBackoffSeconds
    ) {
        this.userNotificationRepository = userNotificationRepository;
        this.notificationEventRepository = notificationEventRepository;
        this.sendEmailNotificationUseCase = sendEmailNotificationUseCase;
        this.emailDeliveryRetryMetadataCodec = emailDeliveryRetryMetadataCodec;
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
            if (!EmailDeliveryRetryPolicy.supportsEmailRetry(candidate.type())) {
                continue;
            }
            EmailDeliveryRetryState retryState = emailDeliveryRetryMetadataCodec.parse(candidate.metadata())
                    .orElse(null);
            if (!EmailDeliveryRetryBackoffPolicy.isEligibleForRetry(
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
        EmailDeliveryRetryState currentState = emailDeliveryRetryMetadataCodec.parse(notification.metadata())
                .orElse(null);
        if (currentState == null) {
            return;
        }

        String payload = resolveEventPayload(notification);
        if (payload == null) {
            EmailDeliveryRetryState stopped = EmailDeliveryRetryPolicy.afterPermanentFailure(
                    currentState,
                    "Notification event payload is unavailable for email retry.",
                    attemptedAt
            );
            persistRetryState(notification, stopped);
            log.warn(
                    "Email delivery retry stopped notificationId={} eventType={} reason=missing event payload",
                    notification.id(),
                    notification.type()
            );
            return;
        }

        SendEmailNotificationResult result = sendEmailNotificationUseCase.execute(
                new SendEmailNotificationCommand(
                        notification.userId(),
                        notification.type(),
                        payload
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
                        emailDeliveryRetryMetadataCodec.clearEmailDeliveryState(notification.metadata()),
                        NotificationDeliveryStatus.SENT,
                        notification.createdAt(),
                        notification.readAt()
                );
                userNotificationRepository.save(updated);
                log.info(
                        "Email delivery retry succeeded notificationId={} eventType={}",
                        notification.id(),
                        notification.type()
                );
            }
            case SKIPPED -> {
                EmailDeliveryRetryState stopped = EmailDeliveryRetryPolicy.afterPermanentFailure(
                        currentState,
                        result.failureReason() != null
                                ? result.failureReason()
                                : "Email channel is no longer eligible.",
                        attemptedAt
                );
                persistRetryState(notification, stopped);
                log.info(
                        "Email delivery retry stopped notificationId={} eventType={} reason={}",
                        notification.id(),
                        notification.type(),
                        stopped.lastError()
                );
            }
            case FAILED -> {
                EmailDeliveryRetryState nextState;
                if (result.failurePolicy() == NotificationFailurePolicy.PERMANENT) {
                    nextState = EmailDeliveryRetryPolicy.afterPermanentFailure(
                            currentState,
                            result.failureReason(),
                            attemptedAt
                    );
                } else {
                    nextState = EmailDeliveryRetryPolicy.afterRetryableFailure(
                            currentState,
                            result.failureReason(),
                            attemptedAt
                    );
                }
                persistRetryState(notification, nextState);
                log.warn(
                        "Email delivery retry failed notificationId={} eventType={} policy={} reason={}",
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

    private String resolveEventPayload(UserNotification notification) {
        if (notification.notificationEventId() == null) {
            return null;
        }
        return notificationEventRepository.findById(notification.notificationEventId())
                .map(NotificationEvent::payload)
                .orElse(null);
    }

    private void persistRetryState(UserNotification notification, EmailDeliveryRetryState retryState) {
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
        userNotificationRepository.save(updated);
    }
}
