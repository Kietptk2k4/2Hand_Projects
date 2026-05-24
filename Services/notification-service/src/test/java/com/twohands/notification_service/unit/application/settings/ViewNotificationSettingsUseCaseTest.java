package com.twohands.notification_service.unit.application.settings;

import com.twohands.notification_service.application.settings.ViewNotificationSettingsCommand;
import com.twohands.notification_service.application.settings.ViewNotificationSettingsUseCase;
import com.twohands.notification_service.domain.delivery.NotificationDefaultChannelPolicy;
import com.twohands.notification_service.domain.notificationsetting.EffectiveNotificationSetting;
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
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ViewNotificationSettingsUseCaseTest {

    private static final UUID USER_ID = UUID.randomUUID();
    private static final Instant NOW = Instant.parse("2026-05-24T12:00:00Z");

    @Mock
    private UserNotificationSettingRepository userNotificationSettingRepository;

    private ViewNotificationSettingsUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new ViewNotificationSettingsUseCase(userNotificationSettingRepository);
    }

    @Test
    void execute_returnsEffectiveSettingsMergedWithDefaults() {
        when(userNotificationSettingRepository.findByUserId(USER_ID)).thenReturn(List.of(
                new UserNotificationSetting(USER_ID, "POST_LIKED", false, false, true, NOW, NOW)
        ));

        var result = useCase.execute(new ViewNotificationSettingsCommand(USER_ID));

        assertEquals(USER_ID, result.userId());
        assertEquals(NotificationDefaultChannelPolicy.supportedEventTypes().size(), result.settings().size());

        EffectiveNotificationSetting postLiked = findByEventType(result.settings(), "POST_LIKED");
        assertTrue(postLiked.explicitSetting());
        assertTrue(postLiked.allowInApp());

        EffectiveNotificationSetting userFollowed = findByEventType(result.settings(), "USER_FOLLOWED");
        assertFalse(userFollowed.explicitSetting());
    }

    @Test
    void execute_throwsUnauthorizedWhenUserIdMissing() {
        AppException ex = assertThrows(AppException.class, () ->
                useCase.execute(new ViewNotificationSettingsCommand(null))
        );

        assertEquals(ErrorCode.UNAUTHORIZED, ex.getErrorCode());
    }

    private EffectiveNotificationSetting findByEventType(
            List<EffectiveNotificationSetting> settings,
            String eventType
    ) {
        return settings.stream()
                .filter(setting -> setting.eventType().equals(eventType))
                .findFirst()
                .orElseThrow();
    }
}
