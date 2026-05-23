package com.twohands.notification_service.unit.application.settings;

import com.twohands.notification_service.application.settings.InitializeDefaultNotificationSettingsCommand;
import com.twohands.notification_service.application.settings.InitializeDefaultNotificationSettingsUseCase;
import com.twohands.notification_service.domain.delivery.NotificationDefaultChannelPolicy;
import com.twohands.notification_service.domain.notificationsetting.UserNotificationSetting;
import com.twohands.notification_service.domain.notificationsetting.UserNotificationSettingRepository;
import com.twohands.notification_service.exception.AppException;
import com.twohands.notification_service.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InitializeDefaultNotificationSettingsUseCaseTest {

    @Mock
    private UserNotificationSettingRepository userNotificationSettingRepository;

    private InitializeDefaultNotificationSettingsUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new InitializeDefaultNotificationSettingsUseCase(userNotificationSettingRepository);
    }

    @Test
    void execute_createsMissingDefaultSettings() {
        UUID userId = UUID.randomUUID();
        when(userNotificationSettingRepository.findEventTypesByUserId(userId)).thenReturn(Set.of("POST_LIKED"));
        when(userNotificationSettingRepository.save(any(UserNotificationSetting.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var result = useCase.execute(new InitializeDefaultNotificationSettingsCommand(userId));

        assertEquals(userId, result.userId());
        assertEquals(NotificationDefaultChannelPolicy.supportedEventTypes().size() - 1, result.createdCount());
        assertEquals(1, result.skippedCount());

        ArgumentCaptor<UserNotificationSetting> captor = ArgumentCaptor.forClass(UserNotificationSetting.class);
        verify(userNotificationSettingRepository, times(NotificationDefaultChannelPolicy.supportedEventTypes().size() - 1))
                .save(captor.capture());
        assertFalse(captor.getAllValues().stream().anyMatch(setting -> "POST_LIKED".equals(setting.eventType())));
    }

    @Test
    void execute_isIdempotentWhenSettingsAlreadyExist() {
        UUID userId = UUID.randomUUID();
        when(userNotificationSettingRepository.findEventTypesByUserId(userId))
                .thenReturn(NotificationDefaultChannelPolicy.supportedEventTypes());

        var result = useCase.execute(new InitializeDefaultNotificationSettingsCommand(userId));

        assertEquals(0, result.createdCount());
        assertEquals(NotificationDefaultChannelPolicy.supportedEventTypes().size(), result.skippedCount());
        verify(userNotificationSettingRepository, never()).save(any());
    }

    @Test
    void execute_treatsUniqueConflictAsSkippedRow() {
        UUID userId = UUID.randomUUID();
        when(userNotificationSettingRepository.findEventTypesByUserId(userId)).thenReturn(Set.of());
        when(userNotificationSettingRepository.save(any(UserNotificationSetting.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate key"));

        var result = useCase.execute(new InitializeDefaultNotificationSettingsCommand(userId));

        assertEquals(0, result.createdCount());
        assertEquals(NotificationDefaultChannelPolicy.supportedEventTypes().size(), result.skippedCount());
    }

    @Test
    void execute_throwsWhenUserIdMissing() {
        AppException ex = assertThrows(AppException.class, () -> useCase.execute(
                new InitializeDefaultNotificationSettingsCommand(null)
        ));

        assertEquals(ErrorCode.VALIDATION_ERROR, ex.getErrorCode());
    }
}
