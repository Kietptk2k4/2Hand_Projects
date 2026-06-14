package com.twohands.notification_service.unit.application.inapp;

import com.twohands.notification_service.application.idempotency.CreateIdempotentUserNotificationCommand;
import com.twohands.notification_service.application.idempotency.CreateIdempotentUserNotificationResult;
import com.twohands.notification_service.application.idempotency.CreateIdempotentUserNotificationUseCase;
import com.twohands.notification_service.application.inapp.CreateInAppNotificationCommand;
import com.twohands.notification_service.application.inapp.CreateInAppNotificationUseCase;
import com.twohands.notification_service.domain.notificationevent.NotificationEventPayloadSanitizer;
import com.twohands.notification_service.domain.usernotification.NotificationDeliveryStatus;
import com.twohands.notification_service.exception.AppException;
import com.twohands.notification_service.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateInAppNotificationUseCaseTest {

    @Mock
    private NotificationEventPayloadSanitizer metadataSanitizer;

    @Mock
    private CreateIdempotentUserNotificationUseCase createIdempotentUserNotificationUseCase;

    private CreateInAppNotificationUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new CreateInAppNotificationUseCase(metadataSanitizer, createIdempotentUserNotificationUseCase);
    }

    @Test
    void execute_createsInAppNotificationWithTemplateAndSentStatus() {
        UUID eventId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();
        UUID savedId = UUID.randomUUID();

        when(metadataSanitizer.sanitize("{\"postId\":\"post-1\"}"))
                .thenReturn("{\"postId\":\"post-1\"}");
        when(createIdempotentUserNotificationUseCase.execute(any(CreateIdempotentUserNotificationCommand.class)))
                .thenReturn(new CreateIdempotentUserNotificationResult(savedId, false));

        var result = useCase.execute(new CreateInAppNotificationCommand(
                eventId,
                userId,
                actorId,
                "POST_LIKED",
                "POST",
                "post-1",
                "{\"postId\":\"post-1\"}"
        ));

        assertFalse(result.duplicate());
        assertEquals(savedId, result.userNotificationId());

        ArgumentCaptor<CreateIdempotentUserNotificationCommand> captor =
                ArgumentCaptor.forClass(CreateIdempotentUserNotificationCommand.class);
        verify(createIdempotentUserNotificationUseCase).execute(captor.capture());
        assertEquals("Thích bài viết", captor.getValue().title());
        assertEquals("Có người đã thích bài viết của bạn.", captor.getValue().content());
        assertEquals(NotificationDeliveryStatus.SENT, captor.getValue().deliveryStatus());
        assertEquals("{\"postId\":\"post-1\"}", captor.getValue().metadata());
    }

    @Test
    void execute_returnsDuplicateWhenIdempotencyKeyExists() {
        UUID eventId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID existingId = UUID.randomUUID();

        when(metadataSanitizer.sanitize("{}")).thenReturn("{}");
        when(createIdempotentUserNotificationUseCase.execute(any(CreateIdempotentUserNotificationCommand.class)))
                .thenReturn(new CreateIdempotentUserNotificationResult(existingId, true));

        var result = useCase.execute(new CreateInAppNotificationCommand(
                eventId,
                userId,
                UUID.randomUUID(),
                "POST_LIKED",
                "POST",
                "post-1",
                "{}"
        ));

        assertTrue(result.duplicate());
        assertEquals(existingId, result.userNotificationId());
    }

    @Test
    void execute_appendsCancelReasonToTemplateContent() {
        UUID eventId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(metadataSanitizer.sanitize("{}")).thenReturn("{}");
        when(createIdempotentUserNotificationUseCase.execute(any(CreateIdempotentUserNotificationCommand.class)))
                .thenReturn(new CreateIdempotentUserNotificationResult(UUID.randomUUID(), false));

        useCase.execute(new CreateInAppNotificationCommand(
                eventId,
                userId,
                UUID.randomUUID(),
                "ORDER_CANCELLED",
                "ORDER",
                "order-1",
                "{}",
                null,
                "Hết hàng"
        ));

        ArgumentCaptor<CreateIdempotentUserNotificationCommand> captor =
                ArgumentCaptor.forClass(CreateIdempotentUserNotificationCommand.class);
        verify(createIdempotentUserNotificationUseCase).execute(captor.capture());
        assertEquals(
                "Người bán đã hủy đơn hàng của bạn. Lý do: Hết hàng",
                captor.getValue().content()
        );
    }

    @Test
    void execute_throwsForUnknownEventTypeWithoutTemplate() {
        UUID eventId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        AppException ex = assertThrows(AppException.class, () -> useCase.execute(
                new CreateInAppNotificationCommand(
                        eventId,
                        userId,
                        UUID.randomUUID(),
                        "UNKNOWN_EVENT",
                        "POST",
                        "post-1",
                        "{}"
                )
        ));

        assertEquals(ErrorCode.UNKNOWN_EVENT_TYPE, ex.getErrorCode());
    }

    @Test
    void execute_normalizesNullReferenceFields() {
        UUID eventId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(metadataSanitizer.sanitize(null)).thenReturn("{}");
        when(createIdempotentUserNotificationUseCase.execute(any(CreateIdempotentUserNotificationCommand.class)))
                .thenReturn(new CreateIdempotentUserNotificationResult(UUID.randomUUID(), false));

        useCase.execute(new CreateInAppNotificationCommand(
                eventId,
                userId,
                UUID.randomUUID(),
                "POST_LIKED",
                null,
                null,
                null
        ));

        ArgumentCaptor<CreateIdempotentUserNotificationCommand> captor =
                ArgumentCaptor.forClass(CreateIdempotentUserNotificationCommand.class);
        verify(createIdempotentUserNotificationUseCase).execute(captor.capture());
        assertEquals("", captor.getValue().referenceType());
        assertEquals("", captor.getValue().referenceId());
    }
}
