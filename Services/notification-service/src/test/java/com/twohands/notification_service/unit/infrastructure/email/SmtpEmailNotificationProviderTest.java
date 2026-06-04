package com.twohands.notification_service.unit.infrastructure.email;

import com.twohands.notification_service.config.NotificationEmailProperties;
import com.twohands.notification_service.domain.email.EmailDeliveryFailureType;
import com.twohands.notification_service.domain.email.EmailNotificationContent;
import com.twohands.notification_service.domain.email.EmailProviderException;
import com.twohands.notification_service.infrastructure.email.SmtpEmailNotificationProvider;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SmtpEmailNotificationProviderTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private MimeMessage mimeMessage;

    private SmtpEmailNotificationProvider provider;

    @BeforeEach
    void setUp() {
        NotificationEmailProperties properties = new NotificationEmailProperties();
        properties.setFromAddress("noreply@2hands.local");
        properties.setFromName("2Hands");
        provider = new SmtpEmailNotificationProvider(mailSender, properties);
    }

    @Test
    void send_dispatchesMimeMessageWithOtpBody() throws Exception {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(mimeMessage.getMessageID()).thenReturn("<test-message-id@2hands.local>");
        var content = new EmailNotificationContent(
                "user@example.com",
                "Verify your 2Hands email",
                "Ma xac thuc cua ban: 482917"
        );

        var result = provider.send(content);

        assertNotNull(result.providerMessageId());
        verify(mailSender).send(mimeMessage);
    }

    @Test
    void send_mapsMailFailureToRetryableWhenTimeout() throws Exception {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doThrow(new MailSendException("Connection timed out")).when(mailSender).send(any(MimeMessage.class));

        EmailProviderException ex = assertThrows(
                EmailProviderException.class,
                () -> provider.send(new EmailNotificationContent(
                        "user@example.com",
                        "Subject",
                        "Body"
                ))
        );

        assertEquals(EmailDeliveryFailureType.RETRYABLE, ex.failureType());
        assertEquals("Email delivery failed.", ex.getMessage());
    }

    @Test
    void send_rejectsInvalidRecipientWithoutSending() {
        EmailProviderException ex = assertThrows(
                EmailProviderException.class,
                () -> provider.send(new EmailNotificationContent(
                        "invalid-email@example.com",
                        "Subject",
                        "Body"
                ))
        );

        assertEquals(EmailDeliveryFailureType.PERMANENT, ex.failureType());
    }
}
