package com.twohands.notification_service.application.handler;

import com.twohands.notification_service.application.delivery.ApplySkipSelfNotificationCommand;
import com.twohands.notification_service.application.delivery.ApplySkipSelfNotificationUseCase;
import com.twohands.notification_service.application.delivery.ApplyNotificationDeliveryRulesCommand;
import com.twohands.notification_service.application.delivery.ApplyNotificationDeliveryRulesUseCase;
import com.twohands.notification_service.application.inapp.CreateInAppNotificationCommand;
import com.twohands.notification_service.application.inapp.CreateInAppNotificationUseCase;
import com.twohands.notification_service.application.worker.NotificationFailurePolicy;
import com.twohands.notification_service.domain.delivery.NotificationDeliveryDecision;
import com.twohands.notification_service.domain.delivery.SkipSelfNotificationOutcome;
import com.twohands.notification_service.domain.notificationevent.NotificationEvent;
import com.twohands.notification_service.exception.AppException;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@Order(100)
public class InAppSocialNotificationEventHandler implements NotificationEventHandler {

    private final NotificationDeliveryChannelPolicy deliveryChannelPolicy;
    private final NotificationRecipientResolver recipientResolver;
    private final ApplySkipSelfNotificationUseCase applySkipSelfNotificationUseCase;
    private final ApplyNotificationDeliveryRulesUseCase applyNotificationDeliveryRulesUseCase;
    private final CreateInAppNotificationUseCase createInAppNotificationUseCase;

    public InAppSocialNotificationEventHandler(
            NotificationDeliveryChannelPolicy deliveryChannelPolicy,
            NotificationRecipientResolver recipientResolver,
            ApplySkipSelfNotificationUseCase applySkipSelfNotificationUseCase,
            ApplyNotificationDeliveryRulesUseCase applyNotificationDeliveryRulesUseCase,
            CreateInAppNotificationUseCase createInAppNotificationUseCase
    ) {
        this.deliveryChannelPolicy = deliveryChannelPolicy;
        this.recipientResolver = recipientResolver;
        this.applySkipSelfNotificationUseCase = applySkipSelfNotificationUseCase;
        this.applyNotificationDeliveryRulesUseCase = applyNotificationDeliveryRulesUseCase;
        this.createInAppNotificationUseCase = createInAppNotificationUseCase;
    }

    @Override
    public boolean supports(String eventType) {
        return deliveryChannelPolicy.isSocialInAppEvent(eventType);
    }

    @Override
    public NotificationEventHandlerResult handle(NotificationEvent event) {
        List<UUID> recipients = recipientResolver.resolve(event);
        if (recipients.isEmpty()) {
            return NotificationEventHandlerResult.failure(
                    "Recipient is required for notification event",
                    NotificationFailurePolicy.RETRYABLE
            );
        }

        int deliveredCount = 0;
        for (UUID recipientId : recipients) {
            SkipSelfNotificationOutcome skipOutcome = applySkipSelfNotificationUseCase.execute(
                    new ApplySkipSelfNotificationCommand(
                            event.eventType(),
                            event.sourceService(),
                            event.actorId(),
                            recipientId
                    )
            );
            if (skipOutcome == SkipSelfNotificationOutcome.SKIP) {
                continue;
            }
            if (skipOutcome == SkipSelfNotificationOutcome.MISSING_ACTOR) {
                return NotificationEventHandlerResult.failure(
                        "Actor id is required for self-skip social notification event",
                        NotificationFailurePolicy.RETRYABLE
                );
            }

            NotificationDeliveryDecision deliveryDecision;
            try {
                deliveryDecision = applyNotificationDeliveryRulesUseCase.execute(
                        new ApplyNotificationDeliveryRulesCommand(recipientId, event.eventType())
                );
            } catch (DataAccessException ex) {
                return NotificationEventHandlerResult.failure(
                        "Failed to load notification delivery settings",
                        NotificationFailurePolicy.RETRYABLE
                );
            }

            if (!deliveryDecision.inApp()) {
                continue;
            }

            try {
                createInAppNotificationUseCase.execute(new CreateInAppNotificationCommand(
                        event.id(),
                        recipientId,
                        event.actorId(),
                        event.eventType(),
                        event.aggregateType(),
                        event.aggregateId(),
                        event.payload()
                ));
            } catch (AppException ex) {
                return NotificationEventHandlerResult.failure(
                        ex.getMessage(),
                        resolveFailurePolicy(ex)
                );
            }
            deliveredCount++;
        }

        if (deliveredCount == 0) {
            return NotificationEventHandlerResult.noOp();
        }

        return NotificationEventHandlerResult.success();
    }

    private NotificationFailurePolicy resolveFailurePolicy(AppException ex) {
        return switch (ex.getErrorCode()) {
            case UNKNOWN_EVENT_TYPE, INVALID_EVENT_PAYLOAD, VALIDATION_ERROR -> NotificationFailurePolicy.PERMANENT;
            default -> NotificationFailurePolicy.RETRYABLE;
        };
    }
}
