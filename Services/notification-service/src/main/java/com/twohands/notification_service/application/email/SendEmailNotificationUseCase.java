package com.twohands.notification_service.application.email;

import com.twohands.notification_service.application.delivery.ApplyNotificationDeliveryRulesCommand;
import com.twohands.notification_service.application.delivery.ApplyNotificationDeliveryRulesUseCase;
import com.twohands.notification_service.config.NotificationEmailProperties;
import com.twohands.notification_service.domain.delivery.NotificationDeliveryDecision;
import com.twohands.notification_service.domain.email.EmailNotificationChannelPolicy;
import com.twohands.notification_service.domain.email.EmailNotificationContent;
import com.twohands.notification_service.domain.email.EmailNotificationContentRenderer;
import com.twohands.notification_service.domain.email.EmailNotificationProvider;
import com.twohands.notification_service.domain.email.EmailNotificationTemplate;
import com.twohands.notification_service.domain.email.EmailNotificationTemplatePolicy;
import com.twohands.notification_service.domain.email.EmailNotificationVariablesPolicy;
import com.twohands.notification_service.domain.email.EmailProviderException;
import com.twohands.notification_service.domain.email.EmailProviderSendResult;
import com.twohands.notification_service.application.worker.NotificationFailurePolicy;
import com.twohands.notification_service.exception.AppException;
import com.twohands.notification_service.exception.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
public class SendEmailNotificationUseCase {

    private static final Logger log = LoggerFactory.getLogger(SendEmailNotificationUseCase.class);

    private final ApplyNotificationDeliveryRulesUseCase applyNotificationDeliveryRulesUseCase;
    private final EmailNotificationPayloadExtractor emailNotificationPayloadExtractor;
    private final EmailNotificationProvider emailNotificationProvider;
    private final NotificationEmailProperties notificationEmailProperties;

    public SendEmailNotificationUseCase(
            ApplyNotificationDeliveryRulesUseCase applyNotificationDeliveryRulesUseCase,
            EmailNotificationPayloadExtractor emailNotificationPayloadExtractor,
            EmailNotificationProvider emailNotificationProvider,
            NotificationEmailProperties notificationEmailProperties
    ) {
        this.applyNotificationDeliveryRulesUseCase = applyNotificationDeliveryRulesUseCase;
        this.emailNotificationPayloadExtractor = emailNotificationPayloadExtractor;
        this.emailNotificationProvider = emailNotificationProvider;
        this.notificationEmailProperties = notificationEmailProperties;
    }

    public SendEmailNotificationResult execute(SendEmailNotificationCommand command) {
        validateCommand(command);

        if (!EmailNotificationChannelPolicy.supportsEmailChannel(command.eventType())) {
            return SendEmailNotificationResult.skipped("Event type does not support email channel.");
        }

        NotificationDeliveryDecision deliveryDecision;
        try {
            deliveryDecision = applyNotificationDeliveryRulesUseCase.execute(
                    new ApplyNotificationDeliveryRulesCommand(command.recipientUserId(), command.eventType())
            );
        } catch (DataAccessException ex) {
            return SendEmailNotificationResult.failed(
                    NotificationFailurePolicy.RETRYABLE,
                    "Failed to load notification delivery settings."
            );
        }

        if (!deliveryDecision.email()) {
            return SendEmailNotificationResult.skipped("Email channel disabled by delivery policy.");
        }

        if (!notificationEmailProperties.enabled()) {
            return SendEmailNotificationResult.skipped("Email integration is disabled.");
        }

        EmailNotificationTemplate template = EmailNotificationTemplatePolicy.resolve(command.eventType())
                .orElse(null);
        if (template == null) {
            return SendEmailNotificationResult.failed(
                    NotificationFailurePolicy.PERMANENT,
                    "Email template is not configured for event type."
            );
        }

        Map<String, String> variables;
        try {
            variables = EmailNotificationVariablesPolicy.extract(
                    emailNotificationPayloadExtractor.extract(command.payload())
            );
            EmailNotificationVariablesPolicy.validateRequired(template, variables);
        } catch (IllegalArgumentException ex) {
            return SendEmailNotificationResult.failed(
                    NotificationFailurePolicy.PERMANENT,
                    ex.getMessage()
            );
        }

        EmailNotificationContent content = EmailNotificationContentRenderer.render(template, variables);

        try {
            EmailProviderSendResult providerResult = emailNotificationProvider.send(content);
            log.info(
                    "Email notification sent eventType={} recipientUserId={} to={} messageId={}",
                    command.eventType(),
                    command.recipientUserId(),
                    EmailNotificationVariablesPolicy.maskEmail(content.to()),
                    providerResult.providerMessageId()
            );
            return SendEmailNotificationResult.sent(providerResult.providerMessageId());
        } catch (EmailProviderException ex) {
            log.warn(
                    "Email notification failed eventType={} recipientUserId={} to={} reason={}",
                    command.eventType(),
                    command.recipientUserId(),
                    EmailNotificationVariablesPolicy.maskEmail(content.to()),
                    ex.getMessage()
            );
            return SendEmailNotificationResult.failed(
                    mapFailurePolicy(ex),
                    ex.getMessage()
            );
        }
    }

    private void validateCommand(SendEmailNotificationCommand command) {
        if (command.recipientUserId() == null) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Validation failed", "recipientUserId", "Recipient user id is required.");
        }
        if (command.eventType() == null || command.eventType().isBlank()) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Validation failed", "eventType", "Event type must not be blank.");
        }
    }

    private NotificationFailurePolicy mapFailurePolicy(EmailProviderException ex) {
        return switch (ex.failureType()) {
            case RETRYABLE -> NotificationFailurePolicy.RETRYABLE;
            case PERMANENT -> NotificationFailurePolicy.PERMANENT;
        };
    }
}
