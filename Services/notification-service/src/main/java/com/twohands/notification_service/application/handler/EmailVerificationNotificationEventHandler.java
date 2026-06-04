package com.twohands.notification_service.application.handler;

import com.twohands.notification_service.application.email.AuthSecurityEmailNotificationPayloadNormalizer;
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
@Order(49)
public class EmailVerificationNotificationEventHandler implements NotificationEventHandler {

    private static final String EMAIL_VERIFICATION_REQUESTED = "EMAIL_VERIFICATION_REQUESTED";

    private final NotificationRecipientResolver recipientResolver;
    private final SendEmailNotificationUseCase sendEmailNotificationUseCase;
    private final AuthSecurityEmailNotificationPayloadNormalizer authSecurityEmailPayloadNormalizer;

    public EmailVerificationNotificationEventHandler(
            NotificationRecipientResolver recipientResolver,
            SendEmailNotificationUseCase sendEmailNotificationUseCase,
            AuthSecurityEmailNotificationPayloadNormalizer authSecurityEmailPayloadNormalizer
    ) {
        this.recipientResolver = recipientResolver;
        this.sendEmailNotificationUseCase = sendEmailNotificationUseCase;
        this.authSecurityEmailPayloadNormalizer = authSecurityEmailPayloadNormalizer;
    }

    @Override
    public boolean supports(String eventType) {
        return EMAIL_VERIFICATION_REQUESTED.equals(eventType);
    }

    @Override
    public NotificationEventHandlerResult handle(NotificationEvent event) {
        List<UUID> recipients = recipientResolver.resolve(event);
        if (recipients.isEmpty()) {
            return NotificationEventHandlerResult.failure(
                    "Recipient user id is required for email verification notification event",
                    NotificationFailurePolicy.RETRYABLE
            );
        }

        int sentCount = 0;
        int skippedCount = 0;

        String deliveryPayload = authSecurityEmailPayloadNormalizer.normalizeForStorage(
                event.eventType(),
                event.payload()
        );

        for (UUID recipientId : recipients) {
            SendEmailNotificationResult result = sendEmailNotificationUseCase.execute(
                    new SendEmailNotificationCommand(recipientId, event.eventType(), deliveryPayload)
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
