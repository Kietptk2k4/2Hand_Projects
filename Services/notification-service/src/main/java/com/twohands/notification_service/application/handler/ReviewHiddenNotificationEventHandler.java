package com.twohands.notification_service.application.handler;

import com.twohands.notification_service.application.delivery.ApplyNotificationDeliveryRulesCommand;
import com.twohands.notification_service.application.delivery.ApplyNotificationDeliveryRulesUseCase;
import com.twohands.notification_service.application.inapp.CreateInAppNotificationCommand;
import com.twohands.notification_service.application.inapp.CreateInAppNotificationUseCase;
import com.twohands.notification_service.application.push.PushNotificationHandlerSupport;
import com.twohands.notification_service.application.push.SendPushNotificationCommand;
import com.twohands.notification_service.application.push.SendPushNotificationOutcome;
import com.twohands.notification_service.application.push.SendPushNotificationResult;
import com.twohands.notification_service.application.push.SendPushNotificationUseCase;
import com.twohands.notification_service.application.worker.NotificationFailurePolicy;
import com.twohands.notification_service.domain.admin.ReviewHiddenNotificationContext;
import com.twohands.notification_service.domain.delivery.NotificationDeliveryDecision;
import com.twohands.notification_service.domain.inapp.InAppNotificationTemplatePolicy;
import com.twohands.notification_service.domain.notificationevent.NotificationEvent;
import com.twohands.notification_service.exception.AppException;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.UUID;

@Component
@Order(43)
public class ReviewHiddenNotificationEventHandler implements NotificationEventHandler {

    private static final Set<String> SUPPORTED_EVENT_TYPES = Set.of(
            "REVIEW_HIDDEN",
            "REVIEW_REMOVED",
            "REVIEW_RESTORED"
    );

    private static final Set<String> PUSH_ENABLED_EVENT_TYPES = Set.of(
            "REVIEW_REMOVED",
            "REVIEW_RESTORED"
    );

    private final ReviewHiddenNotificationPayloadParser payloadParser;
    private final ApplyNotificationDeliveryRulesUseCase applyNotificationDeliveryRulesUseCase;
    private final CreateInAppNotificationUseCase createInAppNotificationUseCase;
    private final SendPushNotificationUseCase sendPushNotificationUseCase;

    public ReviewHiddenNotificationEventHandler(
            ReviewHiddenNotificationPayloadParser payloadParser,
            ApplyNotificationDeliveryRulesUseCase applyNotificationDeliveryRulesUseCase,
            CreateInAppNotificationUseCase createInAppNotificationUseCase,
            SendPushNotificationUseCase sendPushNotificationUseCase
    ) {
        this.payloadParser = payloadParser;
        this.applyNotificationDeliveryRulesUseCase = applyNotificationDeliveryRulesUseCase;
        this.createInAppNotificationUseCase = createInAppNotificationUseCase;
        this.sendPushNotificationUseCase = sendPushNotificationUseCase;
    }

    @Override
    public boolean supports(String eventType) {
        return SUPPORTED_EVENT_TYPES.contains(eventType);
    }

    @Override
    public NotificationEventHandlerResult handle(NotificationEvent event) {
        ReviewHiddenNotificationContext context;
        try {
            context = payloadParser.parse(event);
        } catch (IllegalArgumentException ex) {
            return NotificationEventHandlerResult.failure(ex.getMessage(), NotificationFailurePolicy.PERMANENT);
        }

        boolean delivered = false;

        for (UUID recipientId : context.recipientUserIds()) {
            RecipientDeliveryResult result = notifyRecipient(event, context, recipientId);
            if (result.hasFailed()) {
                return result.failureResult();
            }
            if (result.wasDelivered()) {
                delivered = true;
            }
        }

        if (!delivered) {
            return NotificationEventHandlerResult.noOp();
        }

        return NotificationEventHandlerResult.success();
    }

    private RecipientDeliveryResult notifyRecipient(
            NotificationEvent event,
            ReviewHiddenNotificationContext context,
            UUID recipientId
    ) {
        String eventType = event.eventType();
        NotificationDeliveryDecision deliveryDecision;
        try {
            deliveryDecision = applyNotificationDeliveryRulesUseCase.execute(
                    new ApplyNotificationDeliveryRulesCommand(recipientId, eventType)
            );
        } catch (DataAccessException ex) {
            return RecipientDeliveryResult.failed(
                    NotificationEventHandlerResult.failure(
                            "Failed to load notification delivery settings",
                            NotificationFailurePolicy.RETRYABLE
                    )
            );
        }

        String templateVariant = isSellerRecipient(context, recipientId)
                ? InAppNotificationTemplatePolicy.SELLER_TEMPLATE_VARIANT
                : null;

        boolean delivered = false;

        if (deliveryDecision.inApp()) {
            try {
                createInAppNotificationUseCase.execute(new CreateInAppNotificationCommand(
                        event.id(),
                        recipientId,
                        null,
                        eventType,
                        context.referenceType(),
                        context.referenceId(),
                        event.payload(),
                        templateVariant
                ));
                delivered = true;
            } catch (AppException ex) {
                return RecipientDeliveryResult.failed(
                        NotificationEventHandlerResult.failure(ex.getMessage(), resolveFailurePolicy(ex))
                );
            }
        }

        if (PUSH_ENABLED_EVENT_TYPES.contains(eventType) && deliveryDecision.push()) {
            SendPushNotificationResult pushResult = sendPushNotificationUseCase.execute(
                    new SendPushNotificationCommand(
                            recipientId,
                            eventType,
                            context.referenceType(),
                            context.referenceId(),
                            event.id(),
                            templateVariant
                    )
            );

            var pushFailure = PushNotificationHandlerSupport.mapFailure(pushResult);
            if (pushFailure.isPresent()) {
                return RecipientDeliveryResult.failed(pushFailure.get());
            }
            if (pushResult.outcome() == SendPushNotificationOutcome.SENT) {
                delivered = true;
            }
        }

        if (!delivered) {
            return RecipientDeliveryResult.notDelivered();
        }

        return RecipientDeliveryResult.delivered();
    }

    private boolean isSellerRecipient(ReviewHiddenNotificationContext context, UUID recipientId) {
        return context.sellerUserId() != null
                && context.sellerUserId().equals(recipientId)
                && !recipientId.equals(context.reviewAuthorId());
    }

    private NotificationFailurePolicy resolveFailurePolicy(AppException ex) {
        return switch (ex.getErrorCode()) {
            case UNKNOWN_EVENT_TYPE, INVALID_EVENT_PAYLOAD, VALIDATION_ERROR -> NotificationFailurePolicy.PERMANENT;
            default -> NotificationFailurePolicy.RETRYABLE;
        };
    }

    private record RecipientDeliveryResult(
            boolean wasDelivered,
            boolean hasFailed,
            NotificationEventHandlerResult failureResult
    ) {

        static RecipientDeliveryResult delivered() {
            return new RecipientDeliveryResult(true, false, null);
        }

        static RecipientDeliveryResult notDelivered() {
            return new RecipientDeliveryResult(false, false, null);
        }

        static RecipientDeliveryResult failed(NotificationEventHandlerResult failure) {
            return new RecipientDeliveryResult(false, true, failure);
        }
    }
}
