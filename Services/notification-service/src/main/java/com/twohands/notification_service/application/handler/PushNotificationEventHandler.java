package com.twohands.notification_service.application.handler;

import com.twohands.notification_service.application.push.PushNotificationHandlerSupport;
import com.twohands.notification_service.application.push.SendPushNotificationCommand;
import com.twohands.notification_service.application.push.SendPushNotificationOutcome;
import com.twohands.notification_service.application.push.SendPushNotificationResult;
import com.twohands.notification_service.application.push.SendPushNotificationUseCase;
import com.twohands.notification_service.application.worker.NotificationFailurePolicy;
import com.twohands.notification_service.domain.email.EmailNotificationChannelPolicy;
import com.twohands.notification_service.domain.notificationevent.NotificationEvent;
import com.twohands.notification_service.domain.push.PushNotificationChannelPolicy;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@Order(55)
public class PushNotificationEventHandler implements NotificationEventHandler {

    private final NotificationDeliveryChannelPolicy deliveryChannelPolicy;
    private final NotificationRecipientResolver recipientResolver;
    private final SendPushNotificationUseCase sendPushNotificationUseCase;

    public PushNotificationEventHandler(
            NotificationDeliveryChannelPolicy deliveryChannelPolicy,
            NotificationRecipientResolver recipientResolver,
            SendPushNotificationUseCase sendPushNotificationUseCase
    ) {
        this.deliveryChannelPolicy = deliveryChannelPolicy;
        this.recipientResolver = recipientResolver;
        this.sendPushNotificationUseCase = sendPushNotificationUseCase;
    }

    @Override
    public boolean supports(String eventType) {
        return PushNotificationChannelPolicy.supportsPushChannel(eventType)
                && !EmailNotificationChannelPolicy.supportsEmailChannel(eventType)
                && !deliveryChannelPolicy.isSocialInAppEvent(eventType)
                && !deliveryChannelPolicy.isDedicatedSocialNotificationEvent(eventType)
                && !deliveryChannelPolicy.isDedicatedCommerceNotificationEvent(eventType)
                && !deliveryChannelPolicy.isDedicatedAccountEnforcementNotificationEvent(eventType)
                && !"USER_CREATED".equals(eventType);
    }

    @Override
    public NotificationEventHandlerResult handle(NotificationEvent event) {
        List<UUID> recipients = recipientResolver.resolve(event);
        if (recipients.isEmpty()) {
            return NotificationEventHandlerResult.failure(
                    "Recipient is required for push notification event",
                    NotificationFailurePolicy.RETRYABLE
            );
        }

        int sentCount = 0;
        int skippedCount = 0;

        for (UUID recipientId : recipients) {
            SendPushNotificationResult result = sendPushNotificationUseCase.execute(
                    buildCommand(event, recipientId)
            );

            var failure = PushNotificationHandlerSupport.mapFailure(result);
            if (failure.isPresent()) {
                return failure.get();
            }

            if (result.outcome() == SendPushNotificationOutcome.SENT) {
                sentCount++;
            }
            if (result.outcome() == SendPushNotificationOutcome.SKIPPED) {
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

    private SendPushNotificationCommand buildCommand(NotificationEvent event, UUID recipientId) {
        return new SendPushNotificationCommand(
                recipientId,
                event.eventType(),
                event.aggregateType(),
                event.aggregateId(),
                event.id()
        );
    }
}
