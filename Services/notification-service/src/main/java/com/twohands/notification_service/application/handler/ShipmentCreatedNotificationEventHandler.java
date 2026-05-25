package com.twohands.notification_service.application.handler;

import com.twohands.notification_service.application.delivery.ApplyNotificationDeliveryRulesCommand;
import com.twohands.notification_service.application.delivery.ApplyNotificationDeliveryRulesUseCase;
import com.twohands.notification_service.application.inapp.CreateInAppNotificationCommand;
import com.twohands.notification_service.application.inapp.CreateInAppNotificationUseCase;
import com.twohands.notification_service.application.worker.NotificationFailurePolicy;
import com.twohands.notification_service.domain.commerce.ShipmentCreatedNotificationContext;
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
@Order(39)
public class ShipmentCreatedNotificationEventHandler implements NotificationEventHandler {

    private static final String SHIPMENT_CREATED = "SHIPMENT_CREATED";
    private static final String REFERENCE_TYPE = "SHIPMENT";
    private static final Set<String> SUPPORTED_EVENT_TYPES = Set.of(SHIPMENT_CREATED, "COMMERCE_SHIPMENT_CREATED");

    private final NotificationEventTypeAliasResolver eventTypeAliasResolver;
    private final ShipmentCreatedNotificationPayloadParser payloadParser;
    private final ApplyNotificationDeliveryRulesUseCase applyNotificationDeliveryRulesUseCase;
    private final CreateInAppNotificationUseCase createInAppNotificationUseCase;

    public ShipmentCreatedNotificationEventHandler(
            NotificationEventTypeAliasResolver eventTypeAliasResolver,
            ShipmentCreatedNotificationPayloadParser payloadParser,
            ApplyNotificationDeliveryRulesUseCase applyNotificationDeliveryRulesUseCase,
            CreateInAppNotificationUseCase createInAppNotificationUseCase
    ) {
        this.eventTypeAliasResolver = eventTypeAliasResolver;
        this.payloadParser = payloadParser;
        this.applyNotificationDeliveryRulesUseCase = applyNotificationDeliveryRulesUseCase;
        this.createInAppNotificationUseCase = createInAppNotificationUseCase;
    }

    @Override
    public boolean supports(String eventType) {
        return SUPPORTED_EVENT_TYPES.contains(eventType);
    }

    @Override
    public NotificationEventHandlerResult handle(NotificationEvent event) {
        if (!SHIPMENT_CREATED.equals(canonicalEventType(event.eventType()))) {
            return NotificationEventHandlerResult.failure(
                    "Unsupported shipment created event type",
                    NotificationFailurePolicy.PERMANENT
            );
        }

        ShipmentCreatedNotificationContext context;
        try {
            context = payloadParser.parse(event);
        } catch (IllegalArgumentException ex) {
            return NotificationEventHandlerResult.failure(ex.getMessage(), NotificationFailurePolicy.PERMANENT);
        }

        boolean delivered = false;

        RecipientDeliveryResult buyerResult = notifyRecipient(event, context, context.buyerId(), null);
        if (buyerResult.failed()) {
            return buyerResult.failure();
        }
        if (buyerResult.delivered()) {
            delivered = true;
        }

        if (context.sellerId() != null && !context.sellerId().equals(context.buyerId())) {
            RecipientDeliveryResult sellerResult = notifyRecipient(
                    event,
                    context,
                    context.sellerId(),
                    InAppNotificationTemplatePolicy.SELLER_TEMPLATE_VARIANT
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
            ShipmentCreatedNotificationContext context,
            UUID recipientId,
            String templateVariant
    ) {
        NotificationDeliveryDecision deliveryDecision;
        try {
            deliveryDecision = applyNotificationDeliveryRulesUseCase.execute(
                    new ApplyNotificationDeliveryRulesCommand(recipientId, SHIPMENT_CREATED)
            );
        } catch (DataAccessException ex) {
            return RecipientDeliveryResult.failed(
                    "Failed to load notification delivery settings",
                    NotificationFailurePolicy.RETRYABLE
            );
        }

        if (!deliveryDecision.inApp()) {
            return RecipientDeliveryResult.delivered(false);
        }

        try {
            createInAppNotificationUseCase.execute(new CreateInAppNotificationCommand(
                    event.id(),
                    recipientId,
                    context.buyerId(),
                    SHIPMENT_CREATED,
                    REFERENCE_TYPE,
                    context.shipmentId(),
                    event.payload(),
                    templateVariant
            ));
            return RecipientDeliveryResult.delivered(true);
        } catch (AppException ex) {
            return RecipientDeliveryResult.failed(ex.getMessage(), resolveFailurePolicy(ex));
        }
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
