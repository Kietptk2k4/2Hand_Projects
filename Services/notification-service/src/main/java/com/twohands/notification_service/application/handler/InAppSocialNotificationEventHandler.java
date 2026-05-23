package com.twohands.notification_service.application.handler;

import com.twohands.notification_service.application.delivery.ApplySkipSelfNotificationCommand;
import com.twohands.notification_service.application.delivery.ApplySkipSelfNotificationUseCase;
import com.twohands.notification_service.application.delivery.ApplyNotificationDeliveryRulesCommand;
import com.twohands.notification_service.application.delivery.ApplyNotificationDeliveryRulesUseCase;
import com.twohands.notification_service.application.idempotency.CreateIdempotentUserNotificationCommand;
import com.twohands.notification_service.application.idempotency.CreateIdempotentUserNotificationUseCase;
import com.twohands.notification_service.application.worker.NotificationFailurePolicy;
import com.twohands.notification_service.domain.delivery.NotificationDeliveryDecision;
import com.twohands.notification_service.domain.delivery.SkipSelfNotificationOutcome;
import com.twohands.notification_service.domain.notificationevent.NotificationEvent;
import com.twohands.notification_service.domain.usernotification.NotificationDeliveryStatus;
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
    private final NotificationContentTemplateService contentTemplateService;
    private final ApplyNotificationDeliveryRulesUseCase applyNotificationDeliveryRulesUseCase;
    private final CreateIdempotentUserNotificationUseCase createIdempotentUserNotificationUseCase;

    public InAppSocialNotificationEventHandler(
            NotificationDeliveryChannelPolicy deliveryChannelPolicy,
            NotificationRecipientResolver recipientResolver,
            ApplySkipSelfNotificationUseCase applySkipSelfNotificationUseCase,
            NotificationContentTemplateService contentTemplateService,
            ApplyNotificationDeliveryRulesUseCase applyNotificationDeliveryRulesUseCase,
            CreateIdempotentUserNotificationUseCase createIdempotentUserNotificationUseCase
    ) {
        this.deliveryChannelPolicy = deliveryChannelPolicy;
        this.recipientResolver = recipientResolver;
        this.applySkipSelfNotificationUseCase = applySkipSelfNotificationUseCase;
        this.contentTemplateService = contentTemplateService;
        this.applyNotificationDeliveryRulesUseCase = applyNotificationDeliveryRulesUseCase;
        this.createIdempotentUserNotificationUseCase = createIdempotentUserNotificationUseCase;
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

        NotificationContentTemplateService.NotificationContentTemplate template =
                contentTemplateService.render(event.eventType());
        String referenceType = normalizeReference(event.aggregateType());
        String referenceId = normalizeReference(event.aggregateId());

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

            createIdempotentUserNotificationUseCase.execute(new CreateIdempotentUserNotificationCommand(
                    event.id(),
                    recipientId,
                    event.actorId(),
                    event.eventType(),
                    template.title(),
                    template.content(),
                    referenceType,
                    referenceId,
                    event.payload(),
                    NotificationDeliveryStatus.SENT
            ));
            deliveredCount++;
        }

        if (deliveredCount == 0) {
            return NotificationEventHandlerResult.noOp();
        }

        return NotificationEventHandlerResult.success();
    }

    private String normalizeReference(String value) {
        return value == null ? "" : value;
    }
}
