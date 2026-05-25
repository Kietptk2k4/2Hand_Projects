package com.twohands.notification_service.application.email;

import com.twohands.notification_service.application.worker.NotificationFailurePolicy;

public record SendEmailNotificationResult(
        SendEmailNotificationOutcome outcome,
        String providerMessageId,
        NotificationFailurePolicy failurePolicy,
        String failureReason
) {

    public static SendEmailNotificationResult sent(String providerMessageId) {
        return new SendEmailNotificationResult(
                SendEmailNotificationOutcome.SENT,
                providerMessageId,
                null,
                null
        );
    }

    public static SendEmailNotificationResult skipped(String reason) {
        return new SendEmailNotificationResult(
                SendEmailNotificationOutcome.SKIPPED,
                null,
                null,
                reason
        );
    }

    public static SendEmailNotificationResult failed(
            NotificationFailurePolicy failurePolicy,
            String failureReason
    ) {
        return new SendEmailNotificationResult(
                SendEmailNotificationOutcome.FAILED,
                null,
                failurePolicy,
                failureReason
        );
    }
}
