package com.twohands.notification_service.unit.application.read;

import com.twohands.notification_service.application.read.MarkAllNotificationsAsReadCommand;
import com.twohands.notification_service.application.read.MarkAllNotificationsAsReadUseCase;
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
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MarkAllNotificationsAsReadUseCaseTest {

    @Mock
    private UserNotificationRepository userNotificationRepository;

    private MarkAllNotificationsAsReadUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new MarkAllNotificationsAsReadUseCase(userNotificationRepository);
    }

    @Test
    void execute_marksAllUnreadNotificationsAsRead() {
        UUID userId = UUID.randomUUID();
        when(userNotificationRepository.markAllUnreadVisibleAsRead(eq(userId), org.mockito.ArgumentMatchers.any()))
                .thenReturn(3);

        var result = useCase.execute(new MarkAllNotificationsAsReadCommand(userId));

        assertEquals(3L, result.updatedCount());

        ArgumentCaptor<Instant> readAtCaptor = ArgumentCaptor.forClass(Instant.class);
        verify(userNotificationRepository).markAllUnreadVisibleAsRead(eq(userId), readAtCaptor.capture());
    }

    @Test
    void execute_returnsZeroWhenNoUnreadNotifications() {
        UUID userId = UUID.randomUUID();
        when(userNotificationRepository.markAllUnreadVisibleAsRead(eq(userId), org.mockito.ArgumentMatchers.any()))
                .thenReturn(0);

        var result = useCase.execute(new MarkAllNotificationsAsReadCommand(userId));

        assertEquals(0L, result.updatedCount());
    }

    @Test
    void execute_throwsUnauthorizedWhenUserIdMissing() {
        AppException ex = assertThrows(AppException.class, () -> useCase.execute(
                new MarkAllNotificationsAsReadCommand(null)
        ));

        assertEquals(ErrorCode.UNAUTHORIZED, ex.getErrorCode());
    }
}
