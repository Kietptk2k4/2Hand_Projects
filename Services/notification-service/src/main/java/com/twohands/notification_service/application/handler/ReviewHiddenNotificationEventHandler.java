package com.twohands.notification_service.application.handler;

import com.twohands.notification_service.application.delivery.ApplyNotificationDeliveryRulesCommand;
import com.twohands.notification_service.application.delivery.ApplyNotificationDeliveryRulesUseCase;
import com.twohands.notification_service.application.inapp.CreateInAppNotificationCommand;
import com.twohands.notification_service.application.inapp.CreateInAppNotificationUseCase;
import com.twohands.notification_service.application.worker.NotificationFailurePolicy;
import com.twohands.notification_service.domain.admin.ReviewHiddenNotificationContext;
import com.twohands.notification_service.domain.delivery.NotificationDeliveryDecision;
import com.twohands.notification_service.domain.inapp.InAppNotificationTemplatePolicy;
import com.twohands.notification_service.domain.notificationevent.NotificationEvent;
import com.twohands.notification_service.exception.AppException;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Order(43)
public class ReviewHiddenNotificationEventHandler implements NotificationEventHandler {

    private static final String REVIEW_HIDDEN = "REVIEW_HIDDEN";

    private final ReviewHiddenNotificationPayloadParser payloadParser;
    private final ApplyNotificationDeliveryRulesUseCase applyNotificationDeliveryRulesUseCase;
    private final CreateInAppNotificationUseCase createInAppNotificationUseCase;

    public ReviewHiddenNotificationEventHandler(
            ReviewHiddenNotificationPayloadParser payloadParser,
            ApplyNotificationDeliveryRulesUseCase applyNotificationDeliveryRulesUseCase,
            CreateInAppNotificationUseCase createInAppNotificationUseCase
    ) {
        this.payloadParser = payloadParser;
        this.applyNotificationDeliveryRulesUseCase = applyNotificationDeliveryRulesUseCase;
        this.createInAppNotificationUseCase = createInAppNotificationUseCase;
    }

    @Override
    public boolean supports(String eventType) {
        return REVIEW_HIDDEN.equals(eventType);
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
        NotificationDeliveryDecision deliveryDecision;
        try {
            deliveryDecision = applyNotificationDeliveryRulesUseCase.execute(
                    new ApplyNotificationDeliveryRulesCommand(recipientId, REVIEW_HIDDEN)
            );
        } catch (DataAccessException ex) {
            return RecipientDeliveryResult.failed(
                    NotificationEventHandlerResult.failure(
                            "Failed to load notification delivery settings",
                            NotificationFailurePolicy.RETRYABLE
                    )
            );
        }

        if (!deliveryDecision.inApp()) {
            return RecipientDeliveryResult.notDelivered();
        }

        String templateVariant = isSellerRecipient(context, recipientId)
                ? InAppNotificationTemplatePolicy.SELLER_TEMPLATE_VARIANT
                : null;

        try {
            createInAppNotificationUseCase.execute(new CreateInAppNotificationCommand(
                    event.id(),
                    recipientId,
                    null,
                    REVIEW_HIDDEN,
                    context.referenceType(),
                    context.referenceId(),
                    event.payload(),
                    templateVariant
            ));
            return RecipientDeliveryResult.delivered();
        } catch (AppException ex) {
            return RecipientDeliveryResult.failed(
                    NotificationEventHandlerResult.failure(ex.getMessage(), resolveFailurePolicy(ex))
            );
        }
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
