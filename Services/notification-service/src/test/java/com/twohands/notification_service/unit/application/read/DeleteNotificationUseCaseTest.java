package com.twohands.notification_service.unit.application.read;

import com.twohands.notification_service.application.read.DeleteNotificationCommand;
import com.twohands.notification_service.application.read.DeleteNotificationUseCase;
import com.twohands.notification_service.domain.usernotification.NotificationDeliveryStatus;
import com.twohands.notification_service.domain.usernotification.UserNotification;
import com.twohands.notification_service.domain.usernotification.UserNotificationRepository;
import com.twohands.notification_service.exception.AppException;
import com.twohands.notification_service.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeleteNotificationUseCaseTest {

    @Mock
    private UserNotificationRepository userNotificationRepository;

    private DeleteNotificationUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new DeleteNotificationUseCase(userNotificationRepository);
    }

    @Test
    void execute_softDeletesNotification() {
        UUID userId = UUID.randomUUID();
        UUID notificationId = UUID.randomUUID();
        UserNotification notification = notification(notificationId, userId, false);

        when(userNotificationRepository.findByIdAndUserId(notificationId, userId))
                .thenReturn(Optional.of(notification));
        when(userNotificationRepository.save(any(UserNotification.class))).thenAnswer(invocation -> {
            UserNotification saved = invocation.getArgument(0);
            return new UserNotification(
                    saved.id(),
                    saved.notificationEventId(),
                    saved.userId(),
                    saved.actorId(),
                    saved.type(),
                    saved.title(),
                    saved.content(),
                    saved.referenceType(),
                    saved.referenceId(),
                    saved.read(),
                    saved.deleted(),
                    saved.metadata(),
                    saved.deliveryStatus(),
                    saved.createdAt(),
                    saved.readAt()
            );
        });

        var result = useCase.execute(new DeleteNotificationCommand(userId, notificationId));

        assertTrue(result.deleted());
        assertEquals(notificationId, result.notificationId());
        verify(userNotificationRepository).save(any(UserNotification.class));
    }

    @Test
    void execute_returnsSuccessWithoutUpdateWhenAlreadyDeleted() {
        UUID userId = UUID.randomUUID();
        UUID notificationId = UUID.randomUUID();
        UserNotification notification = notification(notificationId, userId, true);

        when(userNotificationRepository.findByIdAndUserId(notificationId, userId))
                .thenReturn(Optional.of(notification));

        var result = useCase.execute(new DeleteNotificationCommand(userId, notificationId));

        assertTrue(result.deleted());
        assertTrue(result.alreadyDeleted());
        verify(userNotificationRepository, never()).save(any());
    }

    @Test
    void execute_throwsNotFoundWhenNotificationMissingOrOwnedByOtherUser() {
        UUID userId = UUID.randomUUID();
        UUID notificationId = UUID.randomUUID();

        when(userNotificationRepository.findByIdAndUserId(notificationId, userId))
                .thenReturn(Optional.empty());

        AppException ex = assertThrows(AppException.class, () -> useCase.execute(
                new DeleteNotificationCommand(userId, notificationId)
        ));

        assertEquals(ErrorCode.USER_NOTIFICATION_NOT_FOUND, ex.getErrorCode());
    }

    private UserNotification notification(UUID id, UUID userId, boolean deleted) {
        return new UserNotification(
                id,
                UUID.randomUUID(),
                userId,
                UUID.randomUUID(),
                "POST_LIKED",
                "Title",
                "Content",
                "POST",
                "post-1",
                false,
                deleted,
                "{}",
                NotificationDeliveryStatus.SENT,
                Instant.now(),
                null
        );
    }
}
