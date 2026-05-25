package com.twohands.notification_service.application.handler;

import com.twohands.notification_service.application.email.SendEmailNotificationCommand;
import com.twohands.notification_service.application.email.SendEmailNotificationOutcome;
import com.twohands.notification_service.application.email.SendEmailNotificationResult;
import com.twohands.notification_service.application.email.SendEmailNotificationUseCase;
import com.twohands.notification_service.application.worker.NotificationFailurePolicy;
import com.twohands.notification_service.domain.commerce.PaymentSuccessNotificationContext;
import com.twohands.notification_service.domain.notificationevent.NotificationEvent;
import com.twohands.notification_service.domain.notificationevent.NotificationEventTypeAliasResolver;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@Order(37)
public class PaymentSuccessEmailNotificationEventHandler implements NotificationEventHandler {

    private static final String PAYMENT_SUCCESS = "PAYMENT_SUCCESS";
    private static final Set<String> SUPPORTED_EVENT_TYPES = Set.of(PAYMENT_SUCCESS, "COMMERCE_PAYMENT_PAID");

    private final NotificationEventTypeAliasResolver eventTypeAliasResolver;
    private final PaymentSuccessNotificationPayloadParser payloadParser;
    private final SendEmailNotificationUseCase sendEmailNotificationUseCase;

    public PaymentSuccessEmailNotificationEventHandler(
            NotificationEventTypeAliasResolver eventTypeAliasResolver,
            PaymentSuccessNotificationPayloadParser payloadParser,
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
        if (!PAYMENT_SUCCESS.equals(eventTypeAliasResolver.resolve(event.eventType()))) {
            return NotificationEventHandlerResult.failure(
                    "Unsupported payment success email event type",
                    NotificationFailurePolicy.PERMANENT
            );
        }

        PaymentSuccessNotificationContext context;
        try {
            context = payloadParser.parse(event);
        } catch (IllegalArgumentException ex) {
            return NotificationEventHandlerResult.failure(ex.getMessage(), NotificationFailurePolicy.PERMANENT);
        }

        SendEmailNotificationResult result = sendEmailNotificationUseCase.execute(
                new SendEmailNotificationCommand(context.buyerId(), PAYMENT_SUCCESS, event.payload())
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
