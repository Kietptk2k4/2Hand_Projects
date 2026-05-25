package com.twohands.notification_service.unit.application.push;

import com.twohands.notification_service.application.delivery.ApplyNotificationDeliveryRulesCommand;
import com.twohands.notification_service.application.delivery.ApplyNotificationDeliveryRulesUseCase;
import com.twohands.notification_service.application.push.SendPushNotificationCommand;
import com.twohands.notification_service.application.push.SendPushNotificationOutcome;
import com.twohands.notification_service.application.push.SendPushNotificationUseCase;
import com.twohands.notification_service.application.worker.NotificationFailurePolicy;
import com.twohands.notification_service.config.NotificationFcmProperties;
import com.twohands.notification_service.domain.delivery.NotificationDeliveryDecision;
import com.twohands.notification_service.domain.devicetoken.DeviceType;
import com.twohands.notification_service.domain.devicetoken.UserDeviceToken;
import com.twohands.notification_service.domain.devicetoken.UserDeviceTokenRepository;
import com.twohands.notification_service.domain.push.PushDeliveryFailureType;
import com.twohands.notification_service.domain.push.PushNotificationPayload;
import com.twohands.notification_service.domain.push.PushNotificationProvider;
import com.twohands.notification_service.domain.push.PushProviderException;
import com.twohands.notification_service.domain.push.PushProviderSendResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SendPushNotificationUseCaseTest {

    private static final UUID USER_ID = UUID.randomUUID();
    private static final UUID EVENT_ID = UUID.randomUUID();
    private static final Instant NOW = Instant.parse("2026-05-20T08:00:00Z");

    @Mock
    private ApplyNotificationDeliveryRulesUseCase applyNotificationDeliveryRulesUseCase;

    @Mock
    private UserDeviceTokenRepository userDeviceTokenRepository;

    @Mock
    private PushNotificationProvider pushNotificationProvider;

    private NotificationFcmProperties notificationFcmProperties;
    private SendPushNotificationUseCase useCase;

    @BeforeEach
    void setUp() {
        notificationFcmProperties = new NotificationFcmProperties();
        useCase = new SendPushNotificationUseCase(
                applyNotificationDeliveryRulesUseCase,
                userDeviceTokenRepository,
                pushNotificationProvider,
                notificationFcmProperties
        );
    }

    @Test
    void execute_skipsWhenEventTypeDoesNotSupportPush() throws Exception {
        var result = useCase.execute(new SendPushNotificationCommand(
                USER_ID,
                "EMAIL_VERIFICATION_REQUESTED",
                null,
                null,
                EVENT_ID
        ));

        assertEquals(SendPushNotificationOutcome.SKIPPED, result.outcome());
        verify(pushNotificationProvider, never()).send(any(), any());
    }

    @Test
    void execute_skipsWhenPushDisabledByDeliveryPolicy() throws Exception {
        when(applyNotificationDeliveryRulesUseCase.execute(
                new ApplyNotificationDeliveryRulesCommand(USER_ID, "POST_LIKED")
        )).thenReturn(new NotificationDeliveryDecision(true, false, false));

        var result = useCase.execute(new SendPushNotificationCommand(
                USER_ID,
                "POST_LIKED",
                "POST",
                "post-1",
                EVENT_ID
        ));

        assertEquals(SendPushNotificationOutcome.SKIPPED, result.outcome());
        verify(pushNotificationProvider, never()).send(any(), any());
    }

    @Test
    void execute_skipsWhenFcmIntegrationDisabled() throws Exception {
        when(applyNotificationDeliveryRulesUseCase.execute(
                new ApplyNotificationDeliveryRulesCommand(USER_ID, "POST_LIKED")
        )).thenReturn(new NotificationDeliveryDecision(true, true, false));
        notificationFcmProperties.setEnabled(false);

        var result = useCase.execute(new SendPushNotificationCommand(
                USER_ID,
                "POST_LIKED",
                "POST",
                "post-1",
                EVENT_ID
        ));

        assertEquals(SendPushNotificationOutcome.SKIPPED, result.outcome());
        verify(pushNotificationProvider, never()).send(any(), any());
    }

    @Test
    void execute_skipsWhenNoActiveDeviceTokens() throws Exception {
        notificationFcmProperties.setEnabled(true);
        when(applyNotificationDeliveryRulesUseCase.execute(
                new ApplyNotificationDeliveryRulesCommand(USER_ID, "POST_LIKED")
        )).thenReturn(new NotificationDeliveryDecision(true, true, false));
        when(userDeviceTokenRepository.findActiveByUserId(USER_ID)).thenReturn(List.of());

        var result = useCase.execute(new SendPushNotificationCommand(
                USER_ID,
                "POST_LIKED",
                "POST",
                "post-1",
                EVENT_ID
        ));

        assertEquals(SendPushNotificationOutcome.SKIPPED, result.outcome());
        verify(pushNotificationProvider, never()).send(any(), any());
    }

    @Test
    void execute_sendsToAllActiveTokens() throws Exception {
        notificationFcmProperties.setEnabled(true);
        when(applyNotificationDeliveryRulesUseCase.execute(
                new ApplyNotificationDeliveryRulesCommand(USER_ID, "POST_LIKED")
        )).thenReturn(new NotificationDeliveryDecision(true, true, false));
        when(userDeviceTokenRepository.findActiveByUserId(USER_ID)).thenReturn(List.of(
                activeToken("token-a"),
                activeToken("token-b")
        ));
        when(pushNotificationProvider.send(any(PushNotificationPayload.class), any(UserDeviceToken.class)))
                .thenReturn(new PushProviderSendResult("msg-1"));

        var result = useCase.execute(new SendPushNotificationCommand(
                USER_ID,
                "POST_LIKED",
                "POST",
                "post-1",
                EVENT_ID
        ));

        assertEquals(SendPushNotificationOutcome.SENT, result.outcome());
        assertEquals(2, result.sentTokenCount());
    }

    @Test
    void execute_deactivatesInvalidTokenAndContinues() throws Exception {
        notificationFcmProperties.setEnabled(true);
        when(applyNotificationDeliveryRulesUseCase.execute(
                new ApplyNotificationDeliveryRulesCommand(USER_ID, "POST_LIKED")
        )).thenReturn(new NotificationDeliveryDecision(true, true, false));
        when(userDeviceTokenRepository.findActiveByUserId(USER_ID)).thenReturn(List.of(
                activeToken("invalid-token-abc"),
                activeToken("valid-token-xyz")
        ));
        when(pushNotificationProvider.send(any(PushNotificationPayload.class), any(UserDeviceToken.class)))
                .thenThrow(new PushProviderException(PushDeliveryFailureType.INVALID_TOKEN, "Invalid"))
                .thenReturn(new PushProviderSendResult("msg-ok"));
        when(userDeviceTokenRepository.save(any(UserDeviceToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var result = useCase.execute(new SendPushNotificationCommand(
                USER_ID,
                "POST_LIKED",
                "POST",
                "post-1",
                EVENT_ID
        ));

        assertEquals(SendPushNotificationOutcome.SENT, result.outcome());
        assertEquals(1, result.sentTokenCount());
        assertEquals(1, result.deactivatedTokenCount());
        verify(userDeviceTokenRepository).save(any(UserDeviceToken.class));
    }

    @Test
    void execute_returnsRetryableFailureWhenAllTokensFailRetryable() throws Exception {
        notificationFcmProperties.setEnabled(true);
        when(applyNotificationDeliveryRulesUseCase.execute(
                new ApplyNotificationDeliveryRulesCommand(USER_ID, "ORDER_CREATED")
        )).thenReturn(new NotificationDeliveryDecision(true, true, true));
        when(userDeviceTokenRepository.findActiveByUserId(USER_ID)).thenReturn(List.of(
                activeToken("retryable-token-1")
        ));
        when(pushNotificationProvider.send(any(PushNotificationPayload.class), any(UserDeviceToken.class)))
                .thenThrow(new PushProviderException(PushDeliveryFailureType.RETRYABLE, "FCM timeout"));

        var result = useCase.execute(new SendPushNotificationCommand(
                USER_ID,
                "ORDER_CREATED",
                "ORDER",
                "ord-1",
                EVENT_ID
        ));

        assertEquals(SendPushNotificationOutcome.FAILED, result.outcome());
        assertEquals(NotificationFailurePolicy.RETRYABLE, result.failurePolicy());
    }

    private UserDeviceToken activeToken(String deviceToken) {
        return new UserDeviceToken(
                UUID.randomUUID(),
                USER_ID,
                DeviceType.ANDROID,
                deviceToken,
                true,
                NOW,
                NOW,
                NOW
        );
    }
}
