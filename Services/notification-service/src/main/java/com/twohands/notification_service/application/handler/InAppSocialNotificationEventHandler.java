package com.twohands.notification_service.application.handler;

import com.twohands.notification_service.application.idempotency.CreateIdempotentUserNotificationCommand;
import com.twohands.notification_service.application.idempotency.CreateIdempotentUserNotificationUseCase;
import com.twohands.notification_service.application.worker.NotificationFailurePolicy;
import com.twohands.notification_service.domain.notificationevent.NotificationEvent;
import com.twohands.notification_service.domain.usernotification.NotificationDeliveryStatus;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class InAppSocialNotificationEventHandler implements NotificationEventHandler {

    private final NotificationDeliveryChannelPolicy deliveryChannelPolicy;
    private final NotificationRecipientResolver recipientResolver;
    private final SkipSelfNotificationPolicy skipSelfNotificationPolicy;
    private final NotificationContentTemplateService contentTemplateService;
    private final CreateIdempotentUserNotificationUseCase createIdempotentUserNotificationUseCase;

    public InAppSocialNotificationEventHandler(
            NotificationDeliveryChannelPolicy deliveryChannelPolicy,
            NotificationRecipientResolver recipientResolver,
            SkipSelfNotificationPolicy skipSelfNotificationPolicy,
            NotificationContentTemplateService contentTemplateService,
            CreateIdempotentUserNotificationUseCase createIdempotentUserNotificationUseCase
    ) {
        this.deliveryChannelPolicy = deliveryChannelPolicy;
        this.recipientResolver = recipientResolver;
        this.skipSelfNotificationPolicy = skipSelfNotificationPolicy;
        this.contentTemplateService = contentTemplateService;
        this.createIdempotentUserNotificationUseCase = createIdempotentUserNotificationUseCase;
    }

    @Override
    public boolean supports(String eventType) {
        return deliveryChannelPolicy.allowsInApp(eventType);
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
            if (skipSelfNotificationPolicy.shouldSkip(
                    event.sourceService(),
                    event.eventType(),
                    event.actorId(),
                    recipientId
            )) {
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
