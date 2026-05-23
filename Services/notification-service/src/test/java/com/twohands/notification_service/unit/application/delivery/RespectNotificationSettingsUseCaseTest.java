package com.twohands.notification_service.unit.application.delivery;

import com.twohands.notification_service.application.delivery.RespectNotificationSettingsCommand;
import com.twohands.notification_service.application.delivery.RespectNotificationSettingsUseCase;
import com.twohands.notification_service.domain.notificationsetting.UserNotificationSetting;
import com.twohands.notification_service.domain.notificationsetting.UserNotificationSettingRepository;
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RespectNotificationSettingsUseCaseTest {

    @Mock
    private UserNotificationSettingRepository userNotificationSettingRepository;

    private RespectNotificationSettingsUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new RespectNotificationSettingsUseCase(userNotificationSettingRepository);
    }

    @Test
    void execute_usesDefaultPolicyWhenSettingMissing() {
        UUID userId = UUID.randomUUID();
        when(userNotificationSettingRepository.findByUserIdAndEventType(userId, "POST_LIKED"))
                .thenReturn(Optional.empty());

        var result = useCase.execute(new RespectNotificationSettingsCommand(userId, "POST_LIKED"));

        assertEquals(userId, result.userId());
        assertEquals("POST_LIKED", result.eventType());
        assertTrue(result.allowInApp());
        assertTrue(result.allowPush());
        assertFalse(result.allowEmail());
        assertFalse(result.explicitSetting());
    }

    @Test
    void execute_respectsExplicitUserDisabledChannels() {
        UUID userId = UUID.randomUUID();
        when(userNotificationSettingRepository.findByUserIdAndEventType(userId, "POST_LIKED"))
                .thenReturn(Optional.of(setting(userId, "POST_LIKED", false, false, false)));

        var result = useCase.execute(new RespectNotificationSettingsCommand(userId, "POST_LIKED"));

        assertFalse(result.allowInApp());
        assertFalse(result.allowPush());
        assertFalse(result.allowEmail());
        assertTrue(result.explicitSetting());
    }

    @Test
    void execute_throwsForUnknownEventType() {
        UUID userId = UUID.randomUUID();

        AppException ex = assertThrows(AppException.class, () -> useCase.execute(
                new RespectNotificationSettingsCommand(userId, "UNKNOWN_EVENT")
        ));

        assertEquals(ErrorCode.UNKNOWN_EVENT_TYPE, ex.getErrorCode());
    }

    private UserNotificationSetting setting(
            UUID userId,
            String eventType,
            boolean allowPush,
            boolean allowEmail,
            boolean allowInApp
    ) {
        return new UserNotificationSetting(
                userId,
                eventType,
                allowPush,
                allowEmail,
                allowInApp,
                Instant.now(),
                Instant.now()
        );
    }
}
