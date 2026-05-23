package com.twohands.notification_service.unit.application.delivery;

import com.twohands.notification_service.application.delivery.ApplyNotificationDeliveryRulesCommand;
import com.twohands.notification_service.application.delivery.ApplyNotificationDeliveryRulesUseCase;
import com.twohands.notification_service.application.delivery.RespectNotificationSettingsUseCase;
import com.twohands.notification_service.domain.devicetoken.UserDeviceTokenRepository;
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
class ApplyNotificationDeliveryRulesUseCaseTest {

    @Mock
    private UserNotificationSettingRepository userNotificationSettingRepository;

    @Mock
    private UserDeviceTokenRepository userDeviceTokenRepository;

    private ApplyNotificationDeliveryRulesUseCase useCase;

    @BeforeEach
    void setUp() {
        RespectNotificationSettingsUseCase respectNotificationSettingsUseCase =
                new RespectNotificationSettingsUseCase(userNotificationSettingRepository);
        useCase = new ApplyNotificationDeliveryRulesUseCase(
                respectNotificationSettingsUseCase,
                userDeviceTokenRepository
        );
    }

    @Test
    void execute_usesDefaultPolicyWhenSettingMissing() {
        UUID userId = UUID.randomUUID();
        when(userNotificationSettingRepository.findByUserIdAndEventType(userId, "POST_LIKED"))
                .thenReturn(Optional.empty());
        when(userDeviceTokenRepository.existsActiveByUserId(userId)).thenReturn(true);

        var decision = useCase.execute(new ApplyNotificationDeliveryRulesCommand(userId, "POST_LIKED"));

        assertTrue(decision.inApp());
        assertTrue(decision.push());
        assertFalse(decision.email());
    }

    @Test
    void execute_respectsUserDisabledInAppSetting() {
        UUID userId = UUID.randomUUID();
        when(userNotificationSettingRepository.findByUserIdAndEventType(userId, "POST_LIKED"))
                .thenReturn(Optional.of(setting(userId, "POST_LIKED", false, false, false)));

        var decision = useCase.execute(new ApplyNotificationDeliveryRulesCommand(userId, "POST_LIKED"));

        assertFalse(decision.inApp());
        assertFalse(decision.push());
        assertFalse(decision.email());
    }

    @Test
    void execute_overridesDisabledEmailForSecurityCriticalEvent() {
        UUID userId = UUID.randomUUID();
        when(userNotificationSettingRepository.findByUserIdAndEventType(userId, "PASSWORD_RESET_REQUESTED"))
                .thenReturn(Optional.of(setting(userId, "PASSWORD_RESET_REQUESTED", false, false, false)));

        var decision = useCase.execute(new ApplyNotificationDeliveryRulesCommand(userId, "PASSWORD_RESET_REQUESTED"));

        assertFalse(decision.inApp());
        assertFalse(decision.push());
        assertTrue(decision.email());
    }

    @Test
    void execute_skipsPushWhenNoActiveDeviceToken() {
        UUID userId = UUID.randomUUID();
        when(userNotificationSettingRepository.findByUserIdAndEventType(userId, "POST_LIKED"))
                .thenReturn(Optional.empty());
        when(userDeviceTokenRepository.existsActiveByUserId(userId)).thenReturn(false);

        var decision = useCase.execute(new ApplyNotificationDeliveryRulesCommand(userId, "POST_LIKED"));

        assertTrue(decision.inApp());
        assertFalse(decision.push());
    }

    @Test
    void execute_forcesPushForAccountCriticalEventEvenWhenUserDisabledPush() {
        UUID userId = UUID.randomUUID();
        when(userNotificationSettingRepository.findByUserIdAndEventType(userId, "USER_SUSPENDED"))
                .thenReturn(Optional.of(setting(userId, "USER_SUSPENDED", false, false, true)));
        when(userDeviceTokenRepository.existsActiveByUserId(userId)).thenReturn(true);

        var decision = useCase.execute(new ApplyNotificationDeliveryRulesCommand(userId, "USER_SUSPENDED"));

        assertTrue(decision.inApp());
        assertTrue(decision.push());
        assertTrue(decision.email());
    }

    @Test
    void execute_throwsForUnknownEventType() {
        UUID userId = UUID.randomUUID();

        AppException ex = assertThrows(AppException.class, () -> useCase.execute(
                new ApplyNotificationDeliveryRulesCommand(userId, "UNKNOWN_EVENT")
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
