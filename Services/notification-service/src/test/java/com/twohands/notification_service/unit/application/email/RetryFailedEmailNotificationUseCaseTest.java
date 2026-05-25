package com.twohands.notification_service.unit.application.email;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.notification_service.application.delivery.EmailDeliveryRetryMetadataCodec;
import com.twohands.notification_service.application.email.RetryFailedEmailNotificationUseCase;
import com.twohands.notification_service.application.email.SendEmailNotificationCommand;
import com.twohands.notification_service.application.email.SendEmailNotificationResult;
import com.twohands.notification_service.application.email.SendEmailNotificationUseCase;
import com.twohands.notification_service.application.worker.NotificationFailurePolicy;
import com.twohands.notification_service.domain.delivery.EmailDeliveryRetryPolicy;
import com.twohands.notification_service.domain.delivery.EmailDeliveryRetryState;
import com.twohands.notification_service.domain.notificationevent.NotificationEvent;
import com.twohands.notification_service.domain.notificationevent.NotificationEventRepository;
import com.twohands.notification_service.domain.notificationevent.NotificationEventStatus;
import com.twohands.notification_service.domain.notificationevent.NotificationSourceService;
import com.twohands.notification_service.domain.usernotification.NotificationDeliveryStatus;
import com.twohands.notification_service.domain.usernotification.UserNotification;
import com.twohands.notification_service.domain.usernotification.UserNotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RetryFailedEmailNotificationUseCaseTest {

    private static final UUID USER_ID = UUID.randomUUID();
    private static final UUID NOTIFICATION_ID = UUID.randomUUID();
    private static final UUID EVENT_ID = UUID.randomUUID();
    private static final Instant CREATED_AT = Instant.parse("2026-05-20T08:00:00Z");

    @Mock
    private UserNotificationRepository userNotificationRepository;

    @Mock
    private NotificationEventRepository notificationEventRepository;

    @Mock
    private SendEmailNotificationUseCase sendEmailNotificationUseCase;

    private EmailDeliveryRetryMetadataCodec codec;
    private RetryFailedEmailNotificationUseCase useCase;

    @BeforeEach
    void setUp() {
        codec = new EmailDeliveryRetryMetadataCodec(new ObjectMapper());
        useCase = new RetryFailedEmailNotificationUseCase(
                userNotificationRepository,
                notificationEventRepository,
                sendEmailNotificationUseCase,
                codec,
                0,
                3600
        );
    }

    @Test
    void execute_marksDeliverySentWhenRetrySucceeds() {
        String metadata = codec.mergeEmailDeliveryState(
                "{}",
                new EmailDeliveryRetryState(
                        NotificationFailurePolicy.RETRYABLE,
                        1,
                        5,
                        "Email provider timeout.",
                        Instant.parse("2020-01-01T00:00:00Z")
                )
        );
        UserNotification failed = sampleNotification(metadata);

        when(userNotificationRepository.findFailedDeliveryCandidates(anyInt())).thenReturn(List.of(failed));
        when(notificationEventRepository.findById(EVENT_ID)).thenReturn(Optional.of(sampleEvent()));
        when(sendEmailNotificationUseCase.execute(any(SendEmailNotificationCommand.class)))
                .thenReturn(SendEmailNotificationResult.sent("msg-1"));
        when(userNotificationRepository.save(any(UserNotification.class))).thenAnswer(inv -> inv.getArgument(0));

        int processed = useCase.execute(10);

        assertEquals(1, processed);
        ArgumentCaptor<UserNotification> captor = ArgumentCaptor.forClass(UserNotification.class);
        verify(userNotificationRepository).save(captor.capture());
        assertEquals(NotificationDeliveryStatus.SENT, captor.getValue().deliveryStatus());
        assertFalse(captor.getValue().metadata().contains("emailDelivery"));
    }

    @Test
    void execute_incrementsRetryMetadataOnRetryableFailure() {
        String metadata = codec.mergeEmailDeliveryState(
                "{}",
                EmailDeliveryRetryPolicy.initialFailure(
                        NotificationFailurePolicy.RETRYABLE,
                        "Email provider timeout.",
                        Instant.parse("2020-01-01T00:00:00Z"),
                        5
                )
        );
        UserNotification failed = sampleNotification(metadata);

        when(userNotificationRepository.findFailedDeliveryCandidates(anyInt())).thenReturn(List.of(failed));
        when(notificationEventRepository.findById(EVENT_ID)).thenReturn(Optional.of(sampleEvent()));
        when(sendEmailNotificationUseCase.execute(any(SendEmailNotificationCommand.class)))
                .thenReturn(SendEmailNotificationResult.failed(
                        NotificationFailurePolicy.RETRYABLE,
                        "Email provider timeout."
                ));
        when(userNotificationRepository.save(any(UserNotification.class))).thenAnswer(inv -> inv.getArgument(0));

        useCase.execute(10);

        ArgumentCaptor<UserNotification> captor = ArgumentCaptor.forClass(UserNotification.class);
        verify(userNotificationRepository).save(captor.capture());
        var parsed = codec.parse(captor.getValue().metadata());
        assertEquals(2, parsed.get().retryCount());
        assertEquals(NotificationFailurePolicy.RETRYABLE, parsed.get().failurePolicy());
    }

    private UserNotification sampleNotification(String metadata) {
        return new UserNotification(
                NOTIFICATION_ID,
                EVENT_ID,
                USER_ID,
                null,
                "ORDER_CREATED",
                "Order confirmed",
                "Your order has been created.",
                "ORDER",
                "ord-1",
                false,
                false,
                metadata,
                NotificationDeliveryStatus.FAILED,
                CREATED_AT,
                null
        );
    }

    private NotificationEvent sampleEvent() {
        return new NotificationEvent(
                EVENT_ID,
                UUID.randomUUID(),
                null,
                "ORDER_CREATED",
                NotificationSourceService.COMMERCE,
                "ORDER",
                "ord-1",
                null,
                USER_ID,
                """
                        {"recipient_email":"buyer@example.com","order_code":"ORD-1"}
                        """,
                NotificationEventStatus.COMPLETED,
                0,
                5,
                null,
                CREATED_AT,
                null,
                CREATED_AT,
                CREATED_AT
        );
    }
}
