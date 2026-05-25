package com.twohands.notification_service.application.handler;

import com.twohands.notification_service.application.email.SendEmailNotificationCommand;
import com.twohands.notification_service.application.email.SendEmailNotificationOutcome;
import com.twohands.notification_service.application.email.SendEmailNotificationResult;
import com.twohands.notification_service.application.email.SendEmailNotificationUseCase;
import com.twohands.notification_service.application.worker.NotificationFailurePolicy;
import com.twohands.notification_service.domain.commerce.OrderCreatedNotificationContext;
import com.twohands.notification_service.domain.notificationevent.NotificationEvent;
import com.twohands.notification_service.domain.notificationevent.NotificationEventTypeAliasResolver;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@Order(35)
public class OrderConfirmationNotificationEventHandler implements NotificationEventHandler {

    private static final String ORDER_CREATED = "ORDER_CREATED";
    private static final Set<String> SUPPORTED_EVENT_TYPES = Set.of(ORDER_CREATED, "COMMERCE_ORDER_CREATED");

    private final NotificationEventTypeAliasResolver eventTypeAliasResolver;
    private final OrderCreatedNotificationPayloadParser payloadParser;
    private final SendEmailNotificationUseCase sendEmailNotificationUseCase;

    public OrderConfirmationNotificationEventHandler(
            NotificationEventTypeAliasResolver eventTypeAliasResolver,
            OrderCreatedNotificationPayloadParser payloadParser,
            SendEmailNotificationUseCase sendEmailNotificationUseCase
    ) {
        this.eventTypeAliasResolver = eventTypeAliasResolver;
        this.payloadParser = payloadParser;
        this.sendEmailNotificationUseCase = sendEmailNotificationUseCase;
    }

    @Override
    public boolean supports(String eventType) {
        return SUPPORTED_EVENT_TYPES.contains(eventType);
    }

    @Override
    public NotificationEventHandlerResult handle(NotificationEvent event) {
        if (!ORDER_CREATED.equals(eventTypeAliasResolver.resolve(event.eventType()))) {
            return NotificationEventHandlerResult.failure(
                    "Unsupported order confirmation event type",
                    NotificationFailurePolicy.PERMANENT
            );
        }

        OrderCreatedNotificationContext context;
        try {
            context = payloadParser.parse(event);
        } catch (IllegalArgumentException ex) {
            return NotificationEventHandlerResult.failure(ex.getMessage(), NotificationFailurePolicy.PERMANENT);
        }

        SendEmailNotificationResult result = sendEmailNotificationUseCase.execute(
                new SendEmailNotificationCommand(context.buyerId(), ORDER_CREATED, event.payload())
        );

        if (result.outcome() == SendEmailNotificationOutcome.FAILED) {
            return NotificationEventHandlerResult.failure(
                    result.failureReason(),
                    result.failurePolicy()
            );
        }
        if (result.outcome() == SendEmailNotificationOutcome.SENT) {
            return NotificationEventHandlerResult.success();
        }

        return NotificationEventHandlerResult.noOp();
    }
}
