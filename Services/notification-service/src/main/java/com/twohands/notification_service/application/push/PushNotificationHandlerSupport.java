package com.twohands.notification_service.application.push;

import com.twohands.notification_service.application.handler.NotificationEventHandlerResult;
import com.twohands.notification_service.application.worker.NotificationFailurePolicy;

import java.util.Optional;

public final class PushNotificationHandlerSupport {

    private PushNotificationHandlerSupport() {
    }

    public static Optional<NotificationEventHandlerResult> mapFailure(SendPushNotificationResult result) {
        if (result.outcome() != SendPushNotificationOutcome.FAILED) {
            return Optional.empty();
        }
        NotificationFailurePolicy policy = result.failurePolicy() != null
                ? result.failurePolicy()
                : NotificationFailurePolicy.RETRYABLE;
        return Optional.of(NotificationEventHandlerResult.failure(result.failureReason(), policy));
    }
}
