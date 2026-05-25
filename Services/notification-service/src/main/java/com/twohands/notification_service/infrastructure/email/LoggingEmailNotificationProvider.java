package com.twohands.notification_service.infrastructure.email;

import com.twohands.notification_service.domain.email.EmailDeliveryFailureType;
import com.twohands.notification_service.domain.email.EmailNotificationContent;
import com.twohands.notification_service.domain.email.EmailNotificationProvider;
import com.twohands.notification_service.domain.email.EmailNotificationVariablesPolicy;
import com.twohands.notification_service.domain.email.EmailProviderException;
import com.twohands.notification_service.domain.email.EmailProviderSendResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class LoggingEmailNotificationProvider implements EmailNotificationProvider {

    private static final Logger log = LoggerFactory.getLogger(LoggingEmailNotificationProvider.class);

    @Override
    public EmailProviderSendResult send(EmailNotificationContent content) throws EmailProviderException {
        if (content.to() == null || content.to().isBlank()) {
            throw new EmailProviderException(EmailDeliveryFailureType.PERMANENT, "Recipient email is required.");
        }
        if (content.subject() == null || content.subject().isBlank()) {
            throw new EmailProviderException(EmailDeliveryFailureType.PERMANENT, "Email subject is required.");
        }
        if (content.body() == null || content.body().isBlank()) {
            throw new EmailProviderException(EmailDeliveryFailureType.PERMANENT, "Email body is required.");
        }

        String recipient = content.to();
        if (recipient.contains("retryable-email")) {
            throw new EmailProviderException(EmailDeliveryFailureType.RETRYABLE, "Email provider timeout.");
        }
        if (recipient.contains("invalid-email")) {
            throw new EmailProviderException(EmailDeliveryFailureType.PERMANENT, "Recipient email is invalid.");
        }

        String messageId = UUID.randomUUID().toString();
        log.info(
                "Email provider accepted message messageId={} to={} subject={}",
                messageId,
                EmailNotificationVariablesPolicy.maskEmail(content.to()),
                content.subject()
        );
        return new EmailProviderSendResult(messageId);
    }
}
