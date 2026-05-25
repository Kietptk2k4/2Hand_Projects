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
@Order(49)
public class ShopSuspendedEmailNotificationEventHandler implements NotificationEventHandler {

    private static final String SHOP_SUSPENDED = "SHOP_SUSPENDED";

    private final ShopSuspendedNotificationPayloadParser payloadParser;
    private final SendEmailNotificationUseCase sendEmailNotificationUseCase;

    public ShopSuspendedEmailNotificationEventHandler(
            ShopSuspendedNotificationPayloadParser payloadParser,
            SendEmailNotificationUseCase sendEmailNotificationUseCase
    ) {
        this.payloadParser = payloadParser;
        this.sendEmailNotificationUseCase = sendEmailNotificationUseCase;
    }

    @Override
    public boolean supports(String eventType) {
        return SHOP_SUSPENDED.equals(eventType);
    }

    @Override
    public NotificationEventHandlerResult handle(NotificationEvent event) {
        try {
            payloadParser.parse(event);
        } catch (IllegalArgumentException ex) {
            return NotificationEventHandlerResult.failure(ex.getMessage(), NotificationFailurePolicy.PERMANENT);
        }

        List<UUID> recipients = resolveEmailRecipients(event);
        if (recipients.isEmpty()) {
            return NotificationEventHandlerResult.failure(
                    "shop_owner_id is required for shop suspended email notification event",
                    NotificationFailurePolicy.PERMANENT
            );
        }

        int sentCount = 0;
        int skippedCount = 0;

        for (UUID recipientId : recipients) {
            SendEmailNotificationResult result = sendEmailNotificationUseCase.execute(
                    new SendEmailNotificationCommand(recipientId, SHOP_SUSPENDED, event.payload())
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

    private List<UUID> resolveEmailRecipients(NotificationEvent event) {
        UUID shopOwnerId = event.recipientUserId();
        if (shopOwnerId == null) {
            try {
                shopOwnerId = payloadParser.parse(event).shopOwnerId();
            } catch (IllegalArgumentException ignored) {
                return List.of();
            }
        }
        return List.of(shopOwnerId);
    }
}
