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
import com.twohands.notification_service.domain.commerce.OrderCancelPendingRefundNotificationContext;
import com.twohands.notification_service.domain.delivery.NotificationDeliveryDecision;
import com.twohands.notification_service.domain.notificationevent.NotificationEvent;
import com.twohands.notification_service.domain.notificationevent.NotificationEventTypeAliasResolver;
import com.twohands.notification_service.exception.AppException;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@Order(40)
public class OrderCancelPendingRefundNotificationEventHandler implements NotificationEventHandler {

    private static final String ORDER_CANCEL_PENDING_REFUND = "ORDER_CANCEL_PENDING_REFUND";
    private static final String REFERENCE_TYPE = "ORDER";
    private static final Set<String> SUPPORTED_EVENT_TYPES = Set.of(
            ORDER_CANCEL_PENDING_REFUND,
            "COMMERCE_ORDER_CANCEL_PENDING_REFUND"
    );

    private final NotificationEventTypeAliasResolver eventTypeAliasResolver;
    private final OrderCancelPendingRefundNotificationPayloadParser payloadParser;
    private final ApplyNotificationDeliveryRulesUseCase applyNotificationDeliveryRulesUseCase;
    private final CreateInAppNotificationUseCase createInAppNotificationUseCase;
    private final SendPushNotificationUseCase sendPushNotificationUseCase;

    public OrderCancelPendingRefundNotificationEventHandler(
            NotificationEventTypeAliasResolver eventTypeAliasResolver,
            OrderCancelPendingRefundNotificationPayloadParser payloadParser,
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
        if (!ORDER_CANCEL_PENDING_REFUND.equals(canonicalEventType(event.eventType()))) {
            return NotificationEventHandlerResult.failure(
                    "Unsupported order cancel pending refund event type",
                    NotificationFailurePolicy.PERMANENT
            );
        }

        OrderCancelPendingRefundNotificationContext context;
        try {
            context = payloadParser.parse(event);
        } catch (IllegalArgumentException ex) {
            return NotificationEventHandlerResult.failure(ex.getMessage(), NotificationFailurePolicy.PERMANENT);
        }

        NotificationDeliveryDecision deliveryDecision;
        try {
            deliveryDecision = applyNotificationDeliveryRulesUseCase.execute(
                    new ApplyNotificationDeliveryRulesCommand(context.buyerId(), ORDER_CANCEL_PENDING_REFUND)
            );
        } catch (DataAccessException ex) {
            return NotificationEventHandlerResult.failure(
                    "Failed to load notification delivery settings",
                    NotificationFailurePolicy.RETRYABLE
            );
        }

        boolean delivered = false;

        if (deliveryDecision.inApp()) {
            try {
                createInAppNotificationUseCase.execute(new CreateInAppNotificationCommand(
                        event.id(),
                        context.buyerId(),
                        null,
                        ORDER_CANCEL_PENDING_REFUND,
                        REFERENCE_TYPE,
                        context.orderId(),
                        event.payload(),
                        null
                ));
                delivered = true;
            } catch (AppException ex) {
                return NotificationEventHandlerResult.failure(ex.getMessage(), resolveFailurePolicy(ex));
            }
        }

        if (deliveryDecision.push()) {
            SendPushNotificationResult pushResult = sendPushNotificationUseCase.execute(
                    new SendPushNotificationCommand(
                            context.buyerId(),
                            ORDER_CANCEL_PENDING_REFUND,
                            REFERENCE_TYPE,
                            context.orderId(),
                            event.id(),
                            null
                    )
            );

            var pushFailure = PushNotificationHandlerSupport.mapFailure(pushResult);
            if (pushFailure.isPresent()) {
                return pushFailure.get();
            }
            if (pushResult.outcome() == SendPushNotificationOutcome.SENT) {
                delivered = true;
            }
        }

        if (!delivered) {
            return NotificationEventHandlerResult.noOp();
        }

        return NotificationEventHandlerResult.success();
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
}
