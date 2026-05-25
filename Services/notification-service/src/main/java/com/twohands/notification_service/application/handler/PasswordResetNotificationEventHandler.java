package com.twohands.notification_service.application.handler;

import com.twohands.notification_service.application.email.SendEmailNotificationCommand;
import com.twohands.notification_service.application.email.SendEmailNotificationOutcome;
import com.twohands.notification_service.application.email.SendEmailNotificationResult;
import com.twohands.notification_service.application.email.SendEmailNotificationUseCase;
import com.twohands.notification_service.application.worker.NotificationFailurePolicy;
import com.twohands.notification_service.domain.notificationevent.NotificationEvent;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@Order(48)
public class PasswordResetNotificationEventHandler implements NotificationEventHandler {

    private static final String PASSWORD_RESET_REQUESTED = "PASSWORD_RESET_REQUESTED";

    private final NotificationRecipientResolver recipientResolver;
    private final SendEmailNotificationUseCase sendEmailNotificationUseCase;

    public PasswordResetNotificationEventHandler(
            NotificationRecipientResolver recipientResolver,
            SendEmailNotificationUseCase sendEmailNotificationUseCase
    ) {
        this.recipientResolver = recipientResolver;
        this.sendEmailNotificationUseCase = sendEmailNotificationUseCase;
    }

    @Override
    public boolean supports(String eventType) {
        return PASSWORD_RESET_REQUESTED.equals(eventType);
    }

    @Override
    public NotificationEventHandlerResult handle(NotificationEvent event) {
        List<UUID> recipients = recipientResolver.resolve(event);
        if (recipients.isEmpty()) {
            return NotificationEventHandlerResult.failure(
                    "Recipient user id is required for password reset notification event",
                    NotificationFailurePolicy.RETRYABLE
            );
        }

        int sentCount = 0;
        int skippedCount = 0;

        for (UUID recipientId : recipients) {
            SendEmailNotificationResult result = sendEmailNotificationUseCase.execute(
                    new SendEmailNotificationCommand(recipientId, event.eventType(), event.payload())
            );

            if (result.outcome() == SendEmailNotificationOutcome.FAILED) {
                return NotificationEventHandlerResult.failure(
                        result.failureReason(),
                        result.failurePolicy()
                );
            }
            if (result.outcome() == SendEmailNotificationOutcome.SENT) {
                sentCount++;
            }
            if (result.outcome() == SendEmailNotificationOutcome.SKIPPED) {
                skippedCount++;
            }
        }

        if (sentCount > 0) {
            return NotificationEventHandlerResult.success();
        }
        if (skippedCount > 0) {
            return NotificationEventHandlerResult.noOp();
        }

        return NotificationEventHandlerResult.noOp();
    }
}
