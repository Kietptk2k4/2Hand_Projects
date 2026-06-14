package com.twohands.notification_service.application.push;

import com.twohands.notification_service.application.delivery.ApplyNotificationDeliveryRulesCommand;
import com.twohands.notification_service.application.delivery.ApplyNotificationDeliveryRulesUseCase;
import com.twohands.notification_service.application.devicetoken.DeactivateInvalidDeviceTokenCommand;
import com.twohands.notification_service.application.devicetoken.DeactivateInvalidDeviceTokenResult;
import com.twohands.notification_service.application.devicetoken.DeactivateInvalidDeviceTokenUseCase;
import com.twohands.notification_service.application.worker.NotificationFailurePolicy;
import com.twohands.notification_service.config.NotificationFcmProperties;
import com.twohands.notification_service.domain.commerce.OrderCancelNotificationContentPolicy;
import com.twohands.notification_service.domain.delivery.NotificationDeliveryDecision;
import com.twohands.notification_service.domain.devicetoken.DeactivateInvalidDeviceTokenOutcome;
import com.twohands.notification_service.domain.devicetoken.RegisterDeviceTokenPolicy;
import com.twohands.notification_service.domain.devicetoken.UserDeviceToken;
import com.twohands.notification_service.domain.devicetoken.UserDeviceTokenRepository;
import com.twohands.notification_service.domain.push.PushDeliveryFailureType;
import com.twohands.notification_service.domain.push.PushNotificationChannelPolicy;
import com.twohands.notification_service.domain.push.PushNotificationPayload;
import com.twohands.notification_service.domain.push.PushNotificationPayloadPolicy;
import com.twohands.notification_service.domain.push.PushNotificationProvider;
import com.twohands.notification_service.domain.push.PushNotificationTemplate;
import com.twohands.notification_service.domain.push.PushNotificationTemplatePolicy;
import com.twohands.notification_service.domain.push.PushProviderException;
import com.twohands.notification_service.domain.push.PushProviderSendResult;
import com.twohands.notification_service.exception.AppException;
import com.twohands.notification_service.exception.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class SendPushNotificationUseCase {

    private static final Logger log = LoggerFactory.getLogger(SendPushNotificationUseCase.class);

    private final ApplyNotificationDeliveryRulesUseCase applyNotificationDeliveryRulesUseCase;
    private final UserDeviceTokenRepository userDeviceTokenRepository;
    private final PushNotificationProvider pushNotificationProvider;
    private final NotificationFcmProperties notificationFcmProperties;
    private final DeactivateInvalidDeviceTokenUseCase deactivateInvalidDeviceTokenUseCase;

    public SendPushNotificationUseCase(
            ApplyNotificationDeliveryRulesUseCase applyNotificationDeliveryRulesUseCase,
            UserDeviceTokenRepository userDeviceTokenRepository,
            PushNotificationProvider pushNotificationProvider,
            NotificationFcmProperties notificationFcmProperties,
            DeactivateInvalidDeviceTokenUseCase deactivateInvalidDeviceTokenUseCase
    ) {
        this.applyNotificationDeliveryRulesUseCase = applyNotificationDeliveryRulesUseCase;
        this.userDeviceTokenRepository = userDeviceTokenRepository;
        this.pushNotificationProvider = pushNotificationProvider;
        this.notificationFcmProperties = notificationFcmProperties;
        this.deactivateInvalidDeviceTokenUseCase = deactivateInvalidDeviceTokenUseCase;
    }

    public SendPushNotificationResult execute(SendPushNotificationCommand command) {
        validateCommand(command);

        if (!PushNotificationChannelPolicy.supportsPushChannel(command.eventType())) {
            return SendPushNotificationResult.skipped("Event type does not support push channel.");
        }

        NotificationDeliveryDecision deliveryDecision;
        try {
            deliveryDecision = applyNotificationDeliveryRulesUseCase.execute(
                    new ApplyNotificationDeliveryRulesCommand(command.recipientUserId(), command.eventType())
            );
        } catch (DataAccessException ex) {
            return SendPushNotificationResult.failed(
                    NotificationFailurePolicy.RETRYABLE,
                    "Failed to load notification delivery settings.",
                    0
            );
        }

        if (!deliveryDecision.push()) {
            return SendPushNotificationResult.skipped("Push channel disabled by delivery policy.");
        }

        if (!notificationFcmProperties.enabled()) {
            return SendPushNotificationResult.skipped("FCM integration is disabled.");
        }

        PushNotificationTemplate template = PushNotificationTemplatePolicy.resolve(
                        command.eventType(),
                        command.templateVariant(),
                        command.actorDisplayName()
                )
                .orElse(null);
        if (template == null) {
            return SendPushNotificationResult.failed(
                    NotificationFailurePolicy.PERMANENT,
                    "Push template is not configured for event type.",
                    0
            );
        }

        PushNotificationPayload payload = PushNotificationPayloadPolicy.build(
                new PushNotificationTemplate(
                        template.title(),
                        OrderCancelNotificationContentPolicy.supportsReasonInContent(command.eventType())
                                ? OrderCancelNotificationContentPolicy.appendReason(template.body(), command.reason())
                                : template.body()
                ),
                command.eventType(),
                command.referenceType(),
                command.referenceId(),
                command.notificationEventId()
        );

        List<UserDeviceToken> activeTokens;
        try {
            activeTokens = userDeviceTokenRepository.findActiveByUserId(command.recipientUserId());
        } catch (DataAccessException ex) {
            return SendPushNotificationResult.failed(
                    NotificationFailurePolicy.RETRYABLE,
                    "Failed to load active device tokens.",
                    0
            );
        }

        if (activeTokens.isEmpty()) {
            return SendPushNotificationResult.skipped("No active device tokens.");
        }

        int sentCount = 0;
        int deactivatedCount = 0;
        boolean retryableFailure = false;
        boolean permanentFailure = false;
        String lastFailureReason = null;
        NotificationFailurePolicy lastFailurePolicy = null;

        for (UserDeviceToken token : activeTokens) {
            try {
                PushProviderSendResult providerResult = pushNotificationProvider.send(payload, token);
                sentCount++;
                log.info(
                        "Push notification sent eventType={} recipientUserId={} token={} messageId={}",
                        command.eventType(),
                        command.recipientUserId(),
                        RegisterDeviceTokenPolicy.maskDeviceToken(token.deviceToken()),
                        providerResult.providerMessageId()
                );
            } catch (PushProviderException ex) {
                if (ex.failureType() == PushDeliveryFailureType.INVALID_TOKEN) {
                    DeactivateInvalidDeviceTokenResult deactivateResult = deactivateInvalidDeviceTokenUseCase.execute(
                            new DeactivateInvalidDeviceTokenCommand(token.deviceToken())
                    );
                    if (deactivateResult.outcome() == DeactivateInvalidDeviceTokenOutcome.DEACTIVATED) {
                        deactivatedCount++;
                    }
                    continue;
                }

                lastFailureReason = ex.getMessage();
                lastFailurePolicy = mapFailurePolicy(ex);
                if (ex.failureType() == PushDeliveryFailureType.RETRYABLE) {
                    retryableFailure = true;
                } else {
                    permanentFailure = true;
                }
                log.warn(
                        "Push notification failed eventType={} recipientUserId={} token={} reason={}",
                        command.eventType(),
                        command.recipientUserId(),
                        RegisterDeviceTokenPolicy.maskDeviceToken(token.deviceToken()),
                        ex.getMessage()
                );
            }
        }

        if (sentCount > 0) {
            return SendPushNotificationResult.sent(sentCount, deactivatedCount);
        }
        if (retryableFailure) {
            return SendPushNotificationResult.failed(
                    NotificationFailurePolicy.RETRYABLE,
                    lastFailureReason != null ? lastFailureReason : "Push provider failed with retryable error.",
                    deactivatedCount
            );
        }
        if (permanentFailure) {
            return SendPushNotificationResult.failed(
                    lastFailurePolicy != null ? lastFailurePolicy : NotificationFailurePolicy.PERMANENT,
                    lastFailureReason != null ? lastFailureReason : "Push provider failed with permanent error.",
                    deactivatedCount
            );
        }
        return SendPushNotificationResult.skipped("No push sent to active device tokens.", deactivatedCount);
    }

    private void validateCommand(SendPushNotificationCommand command) {
        if (command.recipientUserId() == null) {
            throw new AppException(
                    ErrorCode.VALIDATION_ERROR,
                    "Validation failed",
                    "recipientUserId",
                    "Recipient user id is required."
            );
        }
        if (command.eventType() == null || command.eventType().isBlank()) {
            throw new AppException(
                    ErrorCode.VALIDATION_ERROR,
                    "Validation failed",
                    "eventType",
                    "Event type must not be blank."
            );
        }
    }

    private NotificationFailurePolicy mapFailurePolicy(PushProviderException ex) {
        return switch (ex.failureType()) {
            case RETRYABLE -> NotificationFailurePolicy.RETRYABLE;
            case PERMANENT, INVALID_TOKEN -> NotificationFailurePolicy.PERMANENT;
        };
    }
}
