package com.twohands.notification_service.application.announcement;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.notification_service.application.delivery.ApplyNotificationDeliveryRulesCommand;
import com.twohands.notification_service.application.delivery.ApplyNotificationDeliveryRulesUseCase;
import com.twohands.notification_service.application.handler.NotificationEventHandlerResult;
import com.twohands.notification_service.application.idempotency.CreateIdempotentUserNotificationCommand;
import com.twohands.notification_service.application.idempotency.CreateIdempotentUserNotificationUseCase;
import com.twohands.notification_service.application.push.PushNotificationHandlerSupport;
import com.twohands.notification_service.application.push.SendPushNotificationCommand;
import com.twohands.notification_service.application.push.SendPushNotificationOutcome;
import com.twohands.notification_service.application.push.SendPushNotificationResult;
import com.twohands.notification_service.application.push.SendPushNotificationUseCase;
import com.twohands.notification_service.application.worker.NotificationFailurePolicy;
import com.twohands.notification_service.domain.admin.SystemAnnouncementFanOutContext;
import com.twohands.notification_service.domain.admin.SystemAnnouncementNotificationMetadataPolicy;
import com.twohands.notification_service.domain.admin.SystemAnnouncementSeverityPolicy;
import com.twohands.notification_service.domain.delivery.NotificationDeliveryDecision;
import com.twohands.notification_service.domain.usernotification.NotificationDeliveryStatus;
import com.twohands.notification_service.exception.AppException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class FanOutSystemAnnouncementUseCase {

    private static final String SYSTEM_ANNOUNCEMENT_SENT = "SYSTEM_ANNOUNCEMENT_SENT";

    private final ApplyNotificationDeliveryRulesUseCase applyNotificationDeliveryRulesUseCase;
    private final CreateIdempotentUserNotificationUseCase createIdempotentUserNotificationUseCase;
    private final SendPushNotificationUseCase sendPushNotificationUseCase;
    private final ObjectMapper objectMapper;
    private final int fanOutBatchSize;

    public FanOutSystemAnnouncementUseCase(
            ApplyNotificationDeliveryRulesUseCase applyNotificationDeliveryRulesUseCase,
            CreateIdempotentUserNotificationUseCase createIdempotentUserNotificationUseCase,
            SendPushNotificationUseCase sendPushNotificationUseCase,
            ObjectMapper objectMapper,
            @Value("${notification.system-announcement.fan-out-batch-size:100}") int fanOutBatchSize
    ) {
        this.applyNotificationDeliveryRulesUseCase = applyNotificationDeliveryRulesUseCase;
        this.createIdempotentUserNotificationUseCase = createIdempotentUserNotificationUseCase;
        this.sendPushNotificationUseCase = sendPushNotificationUseCase;
        this.objectMapper = objectMapper;
        this.fanOutBatchSize = Math.max(fanOutBatchSize, 1);
    }

    @Transactional
    public FanOutSystemAnnouncementResult execute(FanOutSystemAnnouncementCommand command) {
        if (command.recipientUserIds() == null || command.recipientUserIds().isEmpty()) {
            throw new IllegalArgumentException("At least one recipient is required for system announcement fan-out.");
        }

        SystemAnnouncementFanOutContext context = command.context();
        String metadata = SystemAnnouncementNotificationMetadataPolicy.build(
                objectMapper,
                context.announcementId(),
                context.severity(),
                context.isPinned(),
                context.dismissible()
        );

        int created = 0;
        int duplicates = 0;
        boolean delivered = false;

        List<UUID> recipients = command.recipientUserIds();
        for (int offset = 0; offset < recipients.size(); offset += fanOutBatchSize) {
            int end = Math.min(offset + fanOutBatchSize, recipients.size());
            for (UUID recipientId : recipients.subList(offset, end)) {
                RecipientFanOutResult result = fanOutToRecipient(
                        command.notificationEventId(),
                        context,
                        metadata,
                        recipientId
                );
                if (result.optionalHandlerFailure().isPresent()) {
                    return new FanOutSystemAnnouncementResult(
                            created,
                            duplicates,
                            delivered,
                            result.optionalHandlerFailure().get()
                    );
                }
                created += result.createdCount();
                duplicates += result.duplicateCount();
                if (result.delivered()) {
                    delivered = true;
                }
            }
        }

        return new FanOutSystemAnnouncementResult(created, duplicates, delivered, null);
    }

    private RecipientFanOutResult fanOutToRecipient(
            UUID notificationEventId,
            SystemAnnouncementFanOutContext context,
            String metadata,
            UUID recipientId
    ) {
        NotificationDeliveryDecision deliveryDecision;
        try {
            deliveryDecision = applyNotificationDeliveryRulesUseCase.execute(
                    new ApplyNotificationDeliveryRulesCommand(recipientId, SYSTEM_ANNOUNCEMENT_SENT)
            );
        } catch (DataAccessException ex) {
            return RecipientFanOutResult.failed(
                    NotificationEventHandlerResult.failure(
                            "Failed to load notification delivery settings",
                            NotificationFailurePolicy.RETRYABLE
                    )
            );
        }

        boolean recipientDelivered = false;
        int created = 0;
        int duplicates = 0;

        if (deliveryDecision.inApp()) {
            try {
                var inAppResult = createIdempotentUserNotificationUseCase.execute(
                        new CreateIdempotentUserNotificationCommand(
                                notificationEventId,
                                recipientId,
                                null,
                                SYSTEM_ANNOUNCEMENT_SENT,
                                context.title(),
                                context.content(),
                                context.referenceType(),
                                context.referenceId(),
                                metadata,
                                NotificationDeliveryStatus.SENT
                        )
                );
                if (inAppResult.duplicate()) {
                    duplicates++;
                } else {
                    created++;
                }
                recipientDelivered = true;
            } catch (AppException ex) {
                return RecipientFanOutResult.failed(
                        NotificationEventHandlerResult.failure(ex.getMessage(), resolveFailurePolicy(ex))
                );
            }
        }

        if (deliveryDecision.push() && SystemAnnouncementSeverityPolicy.requiresPush(context.severity())) {
            SendPushNotificationResult pushResult = sendPushNotificationUseCase.execute(
                    new SendPushNotificationCommand(
                            recipientId,
                            SYSTEM_ANNOUNCEMENT_SENT,
                            context.referenceType(),
                            context.referenceId(),
                            notificationEventId
                    )
            );

            Optional<NotificationEventHandlerResult> pushFailure = PushNotificationHandlerSupport.mapFailure(pushResult);
            if (pushFailure.isPresent()) {
                return RecipientFanOutResult.failed(pushFailure.get());
            }
            if (pushResult.outcome() == SendPushNotificationOutcome.SENT) {
                recipientDelivered = true;
            }
        }

        return RecipientFanOutResult.success(created, duplicates, recipientDelivered);
    }

    private NotificationFailurePolicy resolveFailurePolicy(AppException ex) {
        return switch (ex.getErrorCode()) {
            case UNKNOWN_EVENT_TYPE, INVALID_EVENT_PAYLOAD, VALIDATION_ERROR -> NotificationFailurePolicy.PERMANENT;
            default -> NotificationFailurePolicy.RETRYABLE;
        };
    }

    private record RecipientFanOutResult(
            int createdCount,
            int duplicateCount,
            boolean delivered,
            NotificationEventHandlerResult handlerFailure
    ) {

        static RecipientFanOutResult success(int createdCount, int duplicateCount, boolean delivered) {
            return new RecipientFanOutResult(createdCount, duplicateCount, delivered, null);
        }

        static RecipientFanOutResult failed(NotificationEventHandlerResult handlerFailure) {
            return new RecipientFanOutResult(0, 0, false, handlerFailure);
        }

        Optional<NotificationEventHandlerResult> optionalHandlerFailure() {
            return Optional.ofNullable(handlerFailure);
        }
    }
}
