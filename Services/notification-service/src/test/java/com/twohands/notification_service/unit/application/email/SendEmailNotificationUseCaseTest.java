package com.twohands.notification_service.unit.application.email;

import com.twohands.notification_service.application.delivery.ApplyNotificationDeliveryRulesCommand;
import com.twohands.notification_service.application.delivery.ApplyNotificationDeliveryRulesUseCase;
import com.twohands.notification_service.application.email.EmailNotificationPayloadExtractor;
import com.twohands.notification_service.application.email.SendEmailNotificationCommand;
import com.twohands.notification_service.application.email.SendEmailNotificationOutcome;
import com.twohands.notification_service.application.email.SendEmailNotificationUseCase;
import com.twohands.notification_service.application.worker.NotificationFailurePolicy;
import com.twohands.notification_service.config.NotificationEmailProperties;
import com.twohands.notification_service.domain.delivery.NotificationDeliveryDecision;
import com.twohands.notification_service.domain.email.EmailDeliveryFailureType;
import com.twohands.notification_service.domain.email.EmailNotificationContent;
import com.twohands.notification_service.domain.email.EmailNotificationProvider;
import com.twohands.notification_service.domain.email.EmailProviderException;
import com.twohands.notification_service.domain.email.EmailProviderSendResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SendEmailNotificationUseCaseTest {

    private static final UUID USER_ID = UUID.randomUUID();

    @Mock
    private ApplyNotificationDeliveryRulesUseCase applyNotificationDeliveryRulesUseCase;

    @Mock
    private EmailNotificationPayloadExtractor emailNotificationPayloadExtractor;

    @Mock
    private EmailNotificationProvider emailNotificationProvider;

    private NotificationEmailProperties notificationEmailProperties;
    private SendEmailNotificationUseCase useCase;

    @BeforeEach
    void setUp() {
        notificationEmailProperties = new NotificationEmailProperties();
        useCase = new SendEmailNotificationUseCase(
                applyNotificationDeliveryRulesUseCase,
                emailNotificationPayloadExtractor,
                emailNotificationProvider,
                notificationEmailProperties
        );
    }

    @Test
    void execute_skipsWhenEmailChannelDisabledByPolicy() throws Exception {
        var result = useCase.execute(new SendEmailNotificationCommand(
                USER_ID,
                "POST_LIKED",
                "{}"
        ));

        assertEquals(SendEmailNotificationOutcome.SKIPPED, result.outcome());
        verify(emailNotificationProvider, never()).send(any());
    }

    @Test
    void execute_skipsWhenEmailIntegrationDisabled() throws Exception {
        when(applyNotificationDeliveryRulesUseCase.execute(
                new ApplyNotificationDeliveryRulesCommand(USER_ID, "EMAIL_VERIFICATION_REQUESTED")
        )).thenReturn(new NotificationDeliveryDecision(false, false, true));
        notificationEmailProperties.setEnabled(false);

        var result = useCase.execute(new SendEmailNotificationCommand(
                USER_ID,
                "EMAIL_VERIFICATION_REQUESTED",
                "{}"
        ));

        assertEquals(SendEmailNotificationOutcome.SKIPPED, result.outcome());
        verify(emailNotificationProvider, never()).send(any());
    }

    @Test
    void execute_sendsEmailWhenEligible() throws Exception {
        notificationEmailProperties.setEnabled(true);
        when(applyNotificationDeliveryRulesUseCase.execute(
                new ApplyNotificationDeliveryRulesCommand(USER_ID, "EMAIL_VERIFICATION_REQUESTED")
        )).thenReturn(new NotificationDeliveryDecision(false, false, true));
        when(emailNotificationPayloadExtractor.extract(any())).thenReturn(Map.of(
                "recipient_email", "user@example.com",
                "verification_code", "123456"
        ));
        when(emailNotificationProvider.send(any(EmailNotificationContent.class)))
                .thenReturn(new EmailProviderSendResult("msg-1"));

        var result = useCase.execute(new SendEmailNotificationCommand(
                USER_ID,
                "EMAIL_VERIFICATION_REQUESTED",
                """
                        {"recipient_email":"user@example.com","verification_code":"123456"}
                        """
        ));

        assertEquals(SendEmailNotificationOutcome.SENT, result.outcome());
        assertEquals("msg-1", result.providerMessageId());
    }

    @Test
    void execute_returnsPermanentFailureWhenTemplateVariableMissing() {
        notificationEmailProperties.setEnabled(true);
        when(applyNotificationDeliveryRulesUseCase.execute(
                new ApplyNotificationDeliveryRulesCommand(USER_ID, "EMAIL_VERIFICATION_REQUESTED")
        )).thenReturn(new NotificationDeliveryDecision(false, false, true));
        when(emailNotificationPayloadExtractor.extract(any())).thenReturn(Map.of(
                "recipient_email", "user@example.com"
        ));

        var result = useCase.execute(new SendEmailNotificationCommand(
                USER_ID,
                "EMAIL_VERIFICATION_REQUESTED",
                "{}"
        ));

        assertEquals(SendEmailNotificationOutcome.FAILED, result.outcome());
        assertEquals(NotificationFailurePolicy.PERMANENT, result.failurePolicy());
    }

    @Test
    void execute_classifiesRetryableProviderFailure() throws Exception {
        notificationEmailProperties.setEnabled(true);
        when(applyNotificationDeliveryRulesUseCase.execute(
                new ApplyNotificationDeliveryRulesCommand(USER_ID, "ORDER_CREATED")
        )).thenReturn(new NotificationDeliveryDecision(true, false, true));
        when(emailNotificationPayloadExtractor.extract(any())).thenReturn(Map.of(
                "recipient_email", "buyer@example.com",
                "order_code", "ORD-1001"
        ));
        when(emailNotificationProvider.send(any(EmailNotificationContent.class)))
                .thenThrow(new EmailProviderException(EmailDeliveryFailureType.RETRYABLE, "Provider timeout"));

        var result = useCase.execute(new SendEmailNotificationCommand(
                USER_ID,
                "ORDER_CREATED",
                "{}"
        ));

        assertEquals(SendEmailNotificationOutcome.FAILED, result.outcome());
        assertEquals(NotificationFailurePolicy.RETRYABLE, result.failurePolicy());
    }
}
