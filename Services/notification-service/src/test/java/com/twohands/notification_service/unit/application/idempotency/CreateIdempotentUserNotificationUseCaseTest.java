package com.twohands.notification_service.unit.application.idempotency;

import com.twohands.notification_service.application.idempotency.CreateIdempotentUserNotificationCommand;
import com.twohands.notification_service.application.idempotency.CreateIdempotentUserNotificationUseCase;
import com.twohands.notification_service.domain.idempotency.UserNotificationIdempotencyKey;
import com.twohands.notification_service.domain.usernotification.NotificationDeliveryStatus;
import com.twohands.notification_service.domain.usernotification.UserNotification;
import com.twohands.notification_service.domain.usernotification.UserNotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateIdempotentUserNotificationUseCaseTest {

    @Mock
    private UserNotificationRepository userNotificationRepository;

    private CreateIdempotentUserNotificationUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new CreateIdempotentUserNotificationUseCase(userNotificationRepository);
    }

    @Test
    void execute_createsUserNotificationWhenNotExists() {
        UUID eventId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID savedId = UUID.randomUUID();
        UserNotificationIdempotencyKey key = UserNotificationIdempotencyKey.of(
                eventId, userId, "POST_LIKED", "POST", "post-id"
        );

        when(userNotificationRepository.findByIdempotencyKey(key)).thenReturn(Optional.empty());
        when(userNotificationRepository.save(any(UserNotification.class))).thenAnswer(invocation -> {
            UserNotification notification = invocation.getArgument(0);
            return new UserNotification(
                    savedId,
                    notification.notificationEventId(),
                    notification.userId(),
                    notification.actorId(),
                    notification.type(),
                    notification.title(),
                    notification.content(),
                    notification.referenceType(),
                    notification.referenceId(),
                    notification.read(),
                    notification.deleted(),
                    notification.metadata(),
                    notification.deliveryStatus(),
                    notification.createdAt(),
                    notification.readAt()
            );
        });

        var result = useCase.execute(command(eventId, userId));

        assertFalse(result.duplicate());
        assertEquals(savedId, result.userNotificationId());

        ArgumentCaptor<UserNotification> captor = ArgumentCaptor.forClass(UserNotification.class);
        verify(userNotificationRepository).save(captor.capture());
        assertEquals(NotificationDeliveryStatus.PENDING, captor.getValue().deliveryStatus());
    }

    @Test
    void execute_returnsDuplicateWhenIdempotencyKeyExists() {
        UUID eventId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID existingId = UUID.randomUUID();
        UserNotificationIdempotencyKey key = UserNotificationIdempotencyKey.of(
                eventId, userId, "POST_LIKED", "POST", "post-id"
        );

        when(userNotificationRepository.findByIdempotencyKey(key))
                .thenReturn(Optional.of(existingNotification(existingId, eventId, userId)));

        var result = useCase.execute(command(eventId, userId));

        assertTrue(result.duplicate());
        assertEquals(existingId, result.userNotificationId());
        verify(userNotificationRepository, never()).save(any());
    }

    @Test
    void execute_treatsUniqueConflictAsDuplicate() {
        UUID eventId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID existingId = UUID.randomUUID();
        UserNotificationIdempotencyKey key = UserNotificationIdempotencyKey.of(
                eventId, userId, "POST_LIKED", "POST", "post-id"
        );

        when(userNotificationRepository.findByIdempotencyKey(key))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(existingNotification(existingId, eventId, userId)));
        when(userNotificationRepository.save(any(UserNotification.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate key"));

        var result = useCase.execute(command(eventId, userId));

        assertTrue(result.duplicate());
        assertEquals(existingId, result.userNotificationId());
    }

    private CreateIdempotentUserNotificationCommand command(UUID eventId, UUID userId) {
        return new CreateIdempotentUserNotificationCommand(
                eventId,
                userId,
                UUID.randomUUID(),
                "POST_LIKED",
                "Someone liked your post",
                "Alice liked your post",
                "POST",
                "post-id",
                "{}"
        );
    }

    private UserNotification existingNotification(UUID id, UUID eventId, UUID userId) {
        return new UserNotification(
                id,
                eventId,
                userId,
                null,
                "POST_LIKED",
                "title",
                "content",
                "POST",
                "post-id",
                false,
                false,
                "{}",
                NotificationDeliveryStatus.PENDING,
                Instant.now(),
                null
        );
    }
}
