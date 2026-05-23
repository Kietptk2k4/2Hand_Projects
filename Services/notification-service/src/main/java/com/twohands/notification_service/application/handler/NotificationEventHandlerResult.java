package com.twohands.notification_service.application.handler;

import com.twohands.notification_service.application.worker.NotificationFailurePolicy;

public record NotificationEventHandlerResult(
        HandlerOutcome outcome,
        String errorMessage,
        NotificationFailurePolicy failurePolicy
) {

    public static NotificationEventHandlerResult success() {
        return new NotificationEventHandlerResult(HandlerOutcome.SUCCESS, null, null);
    }

    public static NotificationEventHandlerResult noOp() {
        return new NotificationEventHandlerResult(HandlerOutcome.NO_OP, null, null);
    }

    public static NotificationEventHandlerResult failure(String errorMessage, NotificationFailurePolicy failurePolicy) {
        return new NotificationEventHandlerResult(HandlerOutcome.FAILURE, errorMessage, failurePolicy);
    }
}
