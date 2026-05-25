package com.twohands.notification_service.application.push;

import com.twohands.notification_service.application.worker.NotificationFailurePolicy;

public record SendPushNotificationResult(
        SendPushNotificationOutcome outcome,
        int sentTokenCount,
        int deactivatedTokenCount,
        NotificationFailurePolicy failurePolicy,
        String failureReason
) {

    public static SendPushNotificationResult sent(int sentTokenCount, int deactivatedTokenCount) {
        return new SendPushNotificationResult(
                SendPushNotificationOutcome.SENT,
                sentTokenCount,
                deactivatedTokenCount,
                null,
                null
        );
    }

    public static SendPushNotificationResult skipped(String reason) {
        return skipped(reason, 0);
    }

    public static SendPushNotificationResult skipped(String reason, int deactivatedTokenCount) {
        return new SendPushNotificationResult(
                SendPushNotificationOutcome.SKIPPED,
                0,
                deactivatedTokenCount,
                null,
                reason
        );
    }

    public static SendPushNotificationResult failed(
            NotificationFailurePolicy failurePolicy,
            String reason,
            int deactivatedTokenCount
    ) {
        return new SendPushNotificationResult(
                SendPushNotificationOutcome.FAILED,
                0,
                deactivatedTokenCount,
                failurePolicy,
                reason
        );
    }
}
