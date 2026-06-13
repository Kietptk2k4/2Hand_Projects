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
import com.twohands.notification_service.domain.commerce.OrderCompletedNotificationContext;
import com.twohands.notification_service.domain.delivery.NotificationDeliveryDecision;
import com.twohands.notification_service.domain.inapp.InAppNotificationTemplatePolicy;
import com.twohands.notification_service.domain.notificationevent.NotificationEvent;
import com.twohands.notification_service.domain.notificationevent.NotificationEventTypeAliasResolver;
import com.twohands.notification_service.exception.AppException;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.UUID;

@Component
@Order(42)
public class OrderCompletedNotificationEventHandler implements NotificationEventHandler {

    private static final String ORDER_COMPLETED = "ORDER_COMPLETED";
    private static final String REFERENCE_TYPE = "ORDER";
    private static final Set<String> SUPPORTED_EVENT_TYPES = Set.of(ORDER_COMPLETED, "COMMERCE_ORDER_COMPLETED");

    private final NotificationEventTypeAliasResolver eventTypeAliasResolver;
    private final OrderCompletedNotificationPayloadParser payloadParser;
    private final ApplyNotificationDeliveryRulesUseCase applyNotificationDeliveryRulesUseCase;
    private final CreateInAppNotificationUseCase createInAppNotificationUseCase;
    private final SendPushNotificationUseCase sendPushNotificationUseCase;

    public OrderCompletedNotificationEventHandler(
            NotificationEventTypeAliasResolver eventTypeAliasResolver,
            OrderCompletedNotificationPayloadParser payloadParser,
            ApplyNotificationDeliveryRulesUseCase applyNotificationDeliveryRulesUseCase,
            CreateInAppNotificationUseCase createInAppNotificationUseCase,
            SendPushNotificationUseCase sendPushNotificationUseCase
    ) {
        this.eventTypeAliasResolver = eventTypeAliasResolver;
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
        if (!ORDER_COMPLETED.equals(canonicalEventType(event.eventType()))) {
            return NotificationEventHandlerResult.failure(
                    "Unsupported order completed event type",
                    NotificationFailurePolicy.PERMANENT
            );
        }

        OrderCompletedNotificationContext context;
        try {
            context = payloadParser.parse(event);
        } catch (IllegalArgumentException ex) {
            return NotificationEventHandlerResult.failure(ex.getMessage(), NotificationFailurePolicy.PERMANENT);
        }

        boolean delivered = false;

        RecipientDeliveryResult buyerResult = notifyRecipient(
                event,
                context,
                context.buyerId(),
                null,
                CommerceNotificationPayloadSupport.RECIPIENT_AUDIENCE_BUYER
        );
        if (buyerResult.failed()) {
            return buyerResult.failure();
        }
        if (buyerResult.delivered()) {
            delivered = true;
        }

        for (UUID sellerId : context.sellerIds()) {
            if (sellerId.equals(context.buyerId())) {
                continue;
            }
            RecipientDeliveryResult sellerResult = notifyRecipient(
                    event,
                    context,
                    sellerId,
                    InAppNotificationTemplatePolicy.SELLER_TEMPLATE_VARIANT,
                    CommerceNotificationPayloadSupport.RECIPIENT_AUDIENCE_SELLER
            );
            if (sellerResult.failed()) {
                return sellerResult.failure();
            }
            if (sellerResult.delivered()) {
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
            OrderCompletedNotificationContext context,
            UUID recipientId,
            String templateVariant,
            String recipientAudience
    ) {
        String metadata = CommerceNotificationPayloadSupport.withRecipientAudience(
                event.payload(),
                recipientAudience
        );

        NotificationDeliveryDecision deliveryDecision;
        try {
            deliveryDecision = applyNotificationDeliveryRulesUseCase.execute(
                    new ApplyNotificationDeliveryRulesCommand(recipientId, ORDER_COMPLETED)
            );
        } catch (DataAccessException ex) {
            return RecipientDeliveryResult.failed(
                    "Failed to load notification delivery settings",
                    NotificationFailurePolicy.RETRYABLE
            );
        }

        boolean recipientDelivered = false;
        UUID actorId = CommerceNotificationPayloadSupport.RECIPIENT_AUDIENCE_SELLER.equals(recipientAudience)
                ? context.buyerId()
                : null;

        if (deliveryDecision.inApp()) {
            try {
                createInAppNotificationUseCase.execute(new CreateInAppNotificationCommand(
                        event.id(),
                        recipientId,
                        actorId,
                        ORDER_COMPLETED,
                        REFERENCE_TYPE,
                        context.orderId(),
                        metadata,
                        templateVariant
                ));
                recipientDelivered = true;
            } catch (AppException ex) {
                return RecipientDeliveryResult.failed(ex.getMessage(), resolveFailurePolicy(ex));
            }
        }

        if (deliveryDecision.push()) {
            SendPushNotificationResult pushResult = sendPushNotificationUseCase.execute(
                    new SendPushNotificationCommand(
                            recipientId,
                            ORDER_COMPLETED,
                            REFERENCE_TYPE,
                            context.orderId(),
                            event.id(),
                            templateVariant
                    )
            );

            var pushFailure = PushNotificationHandlerSupport.mapFailure(pushResult);
            if (pushFailure.isPresent()) {
                NotificationEventHandlerResult failure = pushFailure.get();
                return RecipientDeliveryResult.failed(failure.errorMessage(), failure.failurePolicy());
            }
            if (pushResult.outcome() == SendPushNotificationOutcome.SENT) {
                recipientDelivered = true;
            }
        }

        return RecipientDeliveryResult.delivered(recipientDelivered);
    }

    private String canonicalEventType(String eventType) {
        return eventTypeAliasResolver.resolve(eventType);
    }

    private NotificationFailurePolicy resolveFailurePolicy(AppException ex) {
        return switch (ex.getErrorCode()) {
            case UNKNOWN_EVENT_TYPE, INVALID_EVENT_PAYLOAD, VALIDATION_ERROR -> NotificationFailurePolicy.PERMANENT;
            default -> NotificationFailurePolicy.RETRYABLE;
        };
    }

    private record RecipientDeliveryResult(boolean delivered, boolean failed, String failureReason, NotificationFailurePolicy failurePolicy) {

        static RecipientDeliveryResult delivered(boolean delivered) {
            return new RecipientDeliveryResult(delivered, false, null, null);
        }

        static RecipientDeliveryResult failed(String reason, NotificationFailurePolicy policy) {
            return new RecipientDeliveryResult(false, true, reason, policy);
        }

        NotificationEventHandlerResult failure() {
            return NotificationEventHandlerResult.failure(failureReason, failurePolicy);
        }
    }
}
