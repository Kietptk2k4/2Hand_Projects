package com.twohands.notification_service.unit.application.settings;

import com.twohands.notification_service.application.settings.UpdateNotificationSettingsCommand;
import com.twohands.notification_service.application.settings.UpdateNotificationSettingsUseCase;
import com.twohands.notification_service.domain.notificationevent.NotificationEventTypeAliasResolver;
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

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateNotificationSettingsUseCaseTest {

    private static final UUID USER_ID = UUID.randomUUID();
    private static final Instant CREATED_AT = Instant.parse("2026-05-24T10:00:00Z");
    private static final Instant UPDATED_AT = Instant.parse("2026-05-24T12:00:00Z");

    @Mock
    private UserNotificationSettingRepository userNotificationSettingRepository;

    private UpdateNotificationSettingsUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new UpdateNotificationSettingsUseCase(
                userNotificationSettingRepository,
                new NotificationEventTypeAliasResolver()
        );
    }

    @Test
    void execute_upsertsSettingForSupportedEventType() {
        when(userNotificationSettingRepository.findByUserIdAndEventType(USER_ID, "POST_LIKED"))
                .thenReturn(Optional.empty());
        when(userNotificationSettingRepository.save(any(UserNotificationSetting.class)))
                .thenAnswer(invocation -> {
                    UserNotificationSetting setting = invocation.getArgument(0);
                    return new UserNotificationSetting(
                            setting.userId(),
                            setting.eventType(),
                            setting.allowPush(),
                            setting.allowEmail(),
                            setting.allowInApp(),
                            setting.createdAt(),
                            UPDATED_AT
                    );
                });

        var result = useCase.execute(new UpdateNotificationSettingsCommand(
                USER_ID,
                "post_liked",
                false,
                false,
                true
        ));

        assertEquals("POST_LIKED", result.eventType());
        assertFalse(result.allowPush());
        assertFalse(result.allowEmail());
        assertTrue(result.allowInApp());

        ArgumentCaptor<UserNotificationSetting> captor = ArgumentCaptor.forClass(UserNotificationSetting.class);
        verify(userNotificationSettingRepository).save(captor.capture());
        assertEquals(USER_ID, captor.getValue().userId());
        assertEquals("POST_LIKED", captor.getValue().eventType());
    }

    @Test
    void execute_preservesCreatedAtWhenUpdatingExistingSetting() {
        when(userNotificationSettingRepository.findByUserIdAndEventType(USER_ID, "POST_LIKED"))
                .thenReturn(Optional.of(new UserNotificationSetting(
                        USER_ID,
                        "POST_LIKED",
                        true,
                        true,
                        false,
                        CREATED_AT,
                        CREATED_AT
                )));
        when(userNotificationSettingRepository.save(any(UserNotificationSetting.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        useCase.execute(new UpdateNotificationSettingsCommand(USER_ID, "POST_LIKED", false, true, true));

        ArgumentCaptor<UserNotificationSetting> captor = ArgumentCaptor.forClass(UserNotificationSetting.class);
        verify(userNotificationSettingRepository).save(captor.capture());
        assertEquals(CREATED_AT, captor.getValue().createdAt());
    }

    @Test
    void execute_resolvesCommerceAliasBeforePersisting() {
        when(userNotificationSettingRepository.findByUserIdAndEventType(USER_ID, "ORDER_CREATED"))
                .thenReturn(Optional.empty());
        when(userNotificationSettingRepository.save(any(UserNotificationSetting.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var result = useCase.execute(new UpdateNotificationSettingsCommand(
                USER_ID,
                "COMMERCE_ORDER_CREATED",
                true,
                false,
                true
        ));

        assertEquals("ORDER_CREATED", result.eventType());
    }

    @Test
    void execute_throwsForUnknownEventType() {
        AppException ex = assertThrows(AppException.class, () ->
                useCase.execute(new UpdateNotificationSettingsCommand(USER_ID, "UNKNOWN_EVENT", true, true, true))
        );

        assertEquals(ErrorCode.UNKNOWN_EVENT_TYPE, ex.getErrorCode());
    }

    @Test
    void execute_throwsUnauthorizedWhenUserIdMissing() {
        AppException ex = assertThrows(AppException.class, () ->
                useCase.execute(new UpdateNotificationSettingsCommand(null, "POST_LIKED", true, true, true))
        );

        assertEquals(ErrorCode.UNAUTHORIZED, ex.getErrorCode());
    }

    @Test
    void execute_throwsValidationErrorWhenEventTypeBlank() {
        AppException ex = assertThrows(AppException.class, () ->
                useCase.execute(new UpdateNotificationSettingsCommand(USER_ID, "  ", true, true, true))
        );

        assertEquals(ErrorCode.VALIDATION_ERROR, ex.getErrorCode());
    }
}
