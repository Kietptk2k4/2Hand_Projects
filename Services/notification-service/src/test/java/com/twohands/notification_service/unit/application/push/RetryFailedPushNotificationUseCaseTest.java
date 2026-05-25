package com.twohands.notification_service.unit.application.push;

import com.twohands.notification_service.application.delivery.PushDeliveryRetryMetadataCodec;
import com.twohands.notification_service.application.push.RetryFailedPushNotificationUseCase;
import com.twohands.notification_service.application.push.SendPushNotificationCommand;
import com.twohands.notification_service.application.push.SendPushNotificationResult;
import com.twohands.notification_service.application.push.SendPushNotificationUseCase;
import com.twohands.notification_service.application.worker.NotificationFailurePolicy;
import com.twohands.notification_service.domain.delivery.PushDeliveryRetryPolicy;
import com.twohands.notification_service.domain.delivery.PushDeliveryRetryState;
import com.twohands.notification_service.domain.usernotification.NotificationDeliveryStatus;
import com.twohands.notification_service.domain.usernotification.UserNotification;
import com.twohands.notification_service.domain.usernotification.UserNotificationRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RetryFailedPushNotificationUseCaseTest {

    private static final UUID USER_ID = UUID.randomUUID();
    private static final UUID NOTIFICATION_ID = UUID.randomUUID();
    private static final UUID EVENT_ID = UUID.randomUUID();
    private static final Instant CREATED_AT = Instant.parse("2026-05-20T08:00:00Z");

    @Mock
    private UserNotificationRepository userNotificationRepository;

    @Mock
    private SendPushNotificationUseCase sendPushNotificationUseCase;

    private PushDeliveryRetryMetadataCodec codec;
    private RetryFailedPushNotificationUseCase useCase;

    @BeforeEach
    void setUp() {
        codec = new PushDeliveryRetryMetadataCodec(new ObjectMapper());
        useCase = new RetryFailedPushNotificationUseCase(
                userNotificationRepository,
                sendPushNotificationUseCase,
                codec,
                0,
                3600
        );
    }

    @Test
    void execute_marksDeliverySentWhenRetrySucceeds() {
        String metadata = codec.mergePushDeliveryState(
                "{}",
                new PushDeliveryRetryState(
                        NotificationFailurePolicy.RETRYABLE,
                        1,
                        5,
                        "FCM timeout",
                        Instant.parse("2020-01-01T00:00:00Z")
                )
        );
        UserNotification failed = sampleNotification(metadata, NotificationDeliveryStatus.FAILED);

        when(userNotificationRepository.findFailedDeliveryCandidates(anyInt())).thenReturn(List.of(failed));
        when(sendPushNotificationUseCase.execute(any(SendPushNotificationCommand.class)))
                .thenReturn(SendPushNotificationResult.sent(1, 0));
        when(userNotificationRepository.save(any(UserNotification.class))).thenAnswer(inv -> inv.getArgument(0));

        int processed = useCase.execute(10);

        assertEquals(1, processed);
        ArgumentCaptor<UserNotification> captor = ArgumentCaptor.forClass(UserNotification.class);
        verify(userNotificationRepository).save(captor.capture());
        assertEquals(NotificationDeliveryStatus.SENT, captor.getValue().deliveryStatus());
        assertFalse(captor.getValue().metadata().contains("pushDelivery"));
    }

    @Test
    void execute_incrementsRetryMetadataOnRetryableFailure() {
        String metadata = codec.mergePushDeliveryState(
                "{}",
                PushDeliveryRetryPolicy.initialFailure(
                        NotificationFailurePolicy.RETRYABLE,
                        "FCM timeout",
                        Instant.parse("2020-01-01T00:00:00Z"),
                        5
                )
        );
        UserNotification failed = sampleNotification(metadata, NotificationDeliveryStatus.FAILED);

        when(userNotificationRepository.findFailedDeliveryCandidates(anyInt())).thenReturn(List.of(failed));
        when(sendPushNotificationUseCase.execute(any(SendPushNotificationCommand.class)))
                .thenReturn(SendPushNotificationResult.failed(
                        NotificationFailurePolicy.RETRYABLE,
                        "FCM provider timeout.",
                        0
                ));
        when(userNotificationRepository.save(any(UserNotification.class))).thenAnswer(inv -> inv.getArgument(0));

        useCase.execute(10);

        ArgumentCaptor<UserNotification> captor = ArgumentCaptor.forClass(UserNotification.class);
        verify(userNotificationRepository).save(captor.capture());
        var parsed = codec.parse(captor.getValue().metadata());
        assertEquals(2, parsed.get().retryCount());
        assertEquals(NotificationDeliveryStatus.FAILED, captor.getValue().deliveryStatus());
    }

    private UserNotification sampleNotification(String metadata, NotificationDeliveryStatus status) {
        return new UserNotification(
                NOTIFICATION_ID,
                EVENT_ID,
                USER_ID,
                null,
                "POST_LIKED",
                "New like",
                "Someone liked your post.",
                "POST",
                "post-1",
                false,
                false,
                metadata,
                status,
                CREATED_AT,
                null
        );
    }

}
