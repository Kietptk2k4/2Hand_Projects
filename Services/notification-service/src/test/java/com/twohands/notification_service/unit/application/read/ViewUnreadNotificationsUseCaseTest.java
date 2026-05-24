package com.twohands.notification_service.unit.application.read;

import com.twohands.notification_service.application.read.ViewUnreadNotificationsCommand;
import com.twohands.notification_service.application.read.ViewUnreadNotificationsUseCase;
import com.twohands.notification_service.domain.common.PageResult;
import com.twohands.notification_service.domain.notificationevent.NotificationEventPayloadSanitizer;
import com.twohands.notification_service.domain.usernotification.NotificationDeliveryStatus;
import com.twohands.notification_service.domain.usernotification.UserNotification;
import com.twohands.notification_service.domain.usernotification.UserNotificationListQuery;
import com.twohands.notification_service.domain.usernotification.UserNotificationRepository;
import com.twohands.notification_service.exception.AppException;
import com.twohands.notification_service.exception.ErrorCode;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ViewUnreadNotificationsUseCaseTest {

    @Mock
    private UserNotificationRepository userNotificationRepository;

    @Mock
    private NotificationEventPayloadSanitizer metadataSanitizer;

    private ViewUnreadNotificationsUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new ViewUnreadNotificationsUseCase(userNotificationRepository, metadataSanitizer);
    }

    @Test
    void execute_returnsUnreadNotificationsForUser() {
        UUID userId = UUID.randomUUID();
        UUID notificationId = UUID.randomUUID();

        when(userNotificationRepository.findUnreadVisibleByUserId(any(UserNotificationListQuery.class)))
                .thenReturn(new PageResult<>(
                        List.of(notification(userId, notificationId, false)),
                        0,
                        20,
                        1,
                        1,
                        false
                ));
        when(metadataSanitizer.sanitize("{}")).thenReturn("{}");

        var result = useCase.execute(new ViewUnreadNotificationsCommand(userId, 0, 20));

        assertEquals(1, result.items().size());
        assertEquals(notificationId, result.items().getFirst().id());
        assertFalse(result.items().getFirst().read());

        ArgumentCaptor<UserNotificationListQuery> captor = ArgumentCaptor.forClass(UserNotificationListQuery.class);
        verify(userNotificationRepository).findUnreadVisibleByUserId(captor.capture());
        assertEquals(userId, captor.getValue().userId());
    }

    @Test
    void execute_throwsUnauthorizedWhenUserIdMissing() {
        AppException ex = assertThrows(AppException.class, () -> useCase.execute(
                new ViewUnreadNotificationsCommand(null, 0, 20)
        ));

        assertEquals(ErrorCode.UNAUTHORIZED, ex.getErrorCode());
    }

    @Test
    void execute_throwsForInvalidPagination() {
        UUID userId = UUID.randomUUID();

        AppException ex = assertThrows(AppException.class, () -> useCase.execute(
                new ViewUnreadNotificationsCommand(userId, 0, 0)
        ));

        assertEquals(ErrorCode.INVALID_PAGINATION, ex.getErrorCode());
    }

    private UserNotification notification(UUID userId, UUID id, boolean read) {
        return new UserNotification(
                id,
                UUID.randomUUID(),
                userId,
                UUID.randomUUID(),
                "POST_LIKED",
                "New like",
                "Someone liked your post.",
                "POST",
                "post-1",
                read,
                false,
                "{}",
                NotificationDeliveryStatus.SENT,
                Instant.now(),
                null
        );
    }
}
