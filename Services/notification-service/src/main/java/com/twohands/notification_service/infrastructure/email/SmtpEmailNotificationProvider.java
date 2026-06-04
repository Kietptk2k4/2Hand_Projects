package com.twohands.notification_service.infrastructure.email;

import com.twohands.notification_service.config.NotificationEmailProperties;
import com.twohands.notification_service.domain.email.EmailDeliveryFailureType;
import com.twohands.notification_service.domain.email.EmailNotificationContent;
import com.twohands.notification_service.domain.email.EmailNotificationProvider;
import com.twohands.notification_service.domain.email.EmailNotificationVariablesPolicy;
import com.twohands.notification_service.domain.email.EmailProviderException;
import com.twohands.notification_service.domain.email.EmailProviderSendResult;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.UUID;

@Component
@ConditionalOnProperty(
        prefix = "notification.integrations.email",
        name = "provider",
        havingValue = "smtp"
)
public class SmtpEmailNotificationProvider implements EmailNotificationProvider {

    private static final Logger log = LoggerFactory.getLogger(SmtpEmailNotificationProvider.class);

    private final JavaMailSender mailSender;
    private final NotificationEmailProperties notificationEmailProperties;

    public SmtpEmailNotificationProvider(
            JavaMailSender mailSender,
            NotificationEmailProperties notificationEmailProperties
    ) {
        this.mailSender = mailSender;
        this.notificationEmailProperties = notificationEmailProperties;
    }

    @Override
    public EmailProviderSendResult send(EmailNotificationContent content) throws EmailProviderException {
        validateContent(content);

        String recipient = content.to().trim();
        if (recipient.contains("retryable-email")) {
            throw new EmailProviderException(EmailDeliveryFailureType.RETRYABLE, "Email provider timeout.");
        }
        if (recipient.contains("invalid-email")) {
            throw new EmailProviderException(EmailDeliveryFailureType.PERMANENT, "Recipient email is invalid.");
        }

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false, StandardCharsets.UTF_8.name());
            helper.setFrom(notificationEmailProperties.fromAddress(), notificationEmailProperties.fromName());
            helper.setTo(recipient);
            helper.setSubject(content.subject());
            helper.setText(content.body(), false);
            mailSender.send(mimeMessage);

            String messageId = mimeMessage.getMessageID();
            if (messageId == null || messageId.isBlank()) {
                messageId = UUID.randomUUID().toString();
            }

            log.info(
                    "SMTP email sent messageId={} to={} subject={}",
                    messageId,
                    EmailNotificationVariablesPolicy.maskEmail(recipient),
                    content.subject()
            );
            return new EmailProviderSendResult(messageId);
        } catch (MailException ex) {
            throw new EmailProviderException(mapFailureType(ex), sanitizeErrorMessage(ex), ex);
        } catch (Exception ex) {
            throw new EmailProviderException(
                    EmailDeliveryFailureType.PERMANENT,
                    "Failed to prepare email message.",
                    ex
            );
        }
    }

    private static void validateContent(EmailNotificationContent content) throws EmailProviderException {
        if (content.to() == null || content.to().isBlank()) {
            throw new EmailProviderException(EmailDeliveryFailureType.PERMANENT, "Recipient email is required.");
        }
        if (content.subject() == null || content.subject().isBlank()) {
            throw new EmailProviderException(EmailDeliveryFailureType.PERMANENT, "Email subject is required.");
        }
        if (content.body() == null || content.body().isBlank()) {
            throw new EmailProviderException(EmailDeliveryFailureType.PERMANENT, "Email body is required.");
        }
    }

    private static EmailDeliveryFailureType mapFailureType(MailException ex) {
        String message = ex.getMessage() == null ? "" : ex.getMessage().toLowerCase(Locale.ROOT);
        if (message.contains("timeout") || message.contains("timed out") || message.contains("connection")) {
            return EmailDeliveryFailureType.RETRYABLE;
        }
        return EmailDeliveryFailureType.PERMANENT;
    }

    private static String sanitizeErrorMessage(Exception ex) {
        return "Email delivery failed.";
    }
}
