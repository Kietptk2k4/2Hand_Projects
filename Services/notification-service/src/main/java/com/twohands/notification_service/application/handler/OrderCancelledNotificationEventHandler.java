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
import com.twohands.notification_service.domain.commerce.OrderCancelledNotificationContext;
import com.twohands.notification_service.domain.delivery.NotificationDeliveryDecision;
import com.twohands.notification_service.domain.inapp.InAppNotificationTemplatePolicy;
import com.twohands.notification_service.domain.notificationevent.NotificationEvent;
import com.twohands.notification_service.domain.notificationevent.NotificationEventTypeAliasResolver;
import com.twohands.notification_service.exception.AppException;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Component
@Order(38)
public class OrderCancelledNotificationEventHandler implements NotificationEventHandler {

    private static final String ORDER_CANCELLED = "ORDER_CANCELLED";
    private static final String REFERENCE_TYPE = "ORDER";
    private static final Set<String> SUPPORTED_EVENT_TYPES = Set.of(
            ORDER_CANCELLED,
            "COMMERCE_ORDER_CANCELLED"
    );

    private final NotificationEventTypeAliasResolver eventTypeAliasResolver;
    private final OrderCancelledNotificationPayloadParser payloadParser;
    private final ApplyNotificationDeliveryRulesUseCase applyNotificationDeliveryRulesUseCase;
    private final CreateInAppNotificationUseCase createInAppNotificationUseCase;
    private final SendPushNotificationUseCase sendPushNotificationUseCase;

    public OrderCancelledNotificationEventHandler(
            NotificationEventTypeAliasResolver eventTypeAliasResolver,
            OrderCancelledNotificationPayloadParser payloadParser,
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
        if (!ORDER_CANCELLED.equals(canonicalEventType(event.eventType()))) {
            return NotificationEventHandlerResult.failure(
                    "Unsupported order cancelled event type",
                    NotificationFailurePolicy.PERMANENT
            );
        }

        OrderCancelledNotificationContext context;
        try {
            context = payloadParser.parse(event);
        } catch (IllegalArgumentException ex) {
            return NotificationEventHandlerResult.failure(ex.getMessage(), NotificationFailurePolicy.PERMANENT);
        }

        List<OrderCancelNotificationRecipientResolver.Recipient> recipients =
                OrderCancelNotificationRecipientResolver.forCancelled(
                        context.buyerId(),
                        context.sellerIds(),
                        context.cancelledBy(),
                        context.cancelledByUserId()
                );

        boolean delivered = false;
        for (OrderCancelNotificationRecipientResolver.Recipient recipient : recipients) {
            RecipientDeliveryResult result = notifyRecipient(event, context, recipient);
            if (result.failed()) {
                return result.failure();
            }
            if (result.delivered()) {
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
            OrderCancelledNotificationContext context,
            OrderCancelNotificationRecipientResolver.Recipient recipient
    ) {
        String templateVariant = null;
        if (recipient.sellerAudience()) {
            if ("ADMIN".equalsIgnoreCase(context.cancelledBy())) {
                templateVariant = InAppNotificationTemplatePolicy.ADMIN_CONFIRMED_TEMPLATE_VARIANT;
            } else {
                templateVariant = InAppNotificationTemplatePolicy.SELLER_TEMPLATE_VARIANT;
            }
        }
        String recipientAudience = recipient.sellerAudience()
                ? CommerceNotificationPayloadSupport.RECIPIENT_AUDIENCE_SELLER
                : CommerceNotificationPayloadSupport.RECIPIENT_AUDIENCE_BUYER;
        String metadata = CommerceNotificationPayloadSupport.withRecipientAudience(
                event.payload(),
                recipientAudience
        );

        NotificationDeliveryDecision deliveryDecision;
        try {
            deliveryDecision = applyNotificationDeliveryRulesUseCase.execute(
                    new ApplyNotificationDeliveryRulesCommand(recipient.userId(), ORDER_CANCELLED)
            );
        } catch (DataAccessException ex) {
            return RecipientDeliveryResult.failed(
                    "Failed to load notification delivery settings",
                    NotificationFailurePolicy.RETRYABLE
            );
        }

        boolean recipientDelivered = false;
        UUID actorId = recipient.sellerAudience() ? context.cancelledByUserId() : null;

        if (deliveryDecision.inApp()) {
            try {
                createInAppNotificationUseCase.execute(new CreateInAppNotificationCommand(
                        event.id(),
                        recipient.userId(),
                        actorId,
                        ORDER_CANCELLED,
                        REFERENCE_TYPE,
                        context.orderId(),
                        metadata,
                        templateVariant,
                        context.reason()
                ));
                recipientDelivered = true;
            } catch (AppException ex) {
                return RecipientDeliveryResult.failed(ex.getMessage(), resolveFailurePolicy(ex));
            }
        }

        if (deliveryDecision.push()) {
            SendPushNotificationResult pushResult = sendPushNotificationUseCase.execute(
                    new SendPushNotificationCommand(
                            recipient.userId(),
                            ORDER_CANCELLED,
                            REFERENCE_TYPE,
                            context.orderId(),
                            event.id(),
                            templateVariant,
                            context.reason()
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
