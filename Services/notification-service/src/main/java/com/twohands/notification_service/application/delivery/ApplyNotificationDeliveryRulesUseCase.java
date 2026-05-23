package com.twohands.notification_service.application.delivery;

import com.twohands.notification_service.domain.delivery.DefaultChannelFlags;
import com.twohands.notification_service.domain.delivery.NotificationCriticalOverridePolicy;
import com.twohands.notification_service.domain.delivery.NotificationDefaultChannelPolicy;
import com.twohands.notification_service.domain.delivery.NotificationDeliveryDecision;
import com.twohands.notification_service.domain.devicetoken.UserDeviceTokenRepository;
import com.twohands.notification_service.domain.notificationsetting.UserNotificationSetting;
import com.twohands.notification_service.domain.notificationsetting.UserNotificationSettingRepository;
import com.twohands.notification_service.exception.AppException;
import com.twohands.notification_service.exception.ErrorCode;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class ApplyNotificationDeliveryRulesUseCase {

    private final UserNotificationSettingRepository userNotificationSettingRepository;
    private final UserDeviceTokenRepository userDeviceTokenRepository;

    public ApplyNotificationDeliveryRulesUseCase(
            UserNotificationSettingRepository userNotificationSettingRepository,
            UserDeviceTokenRepository userDeviceTokenRepository
    ) {
        this.userNotificationSettingRepository = userNotificationSettingRepository;
        this.userDeviceTokenRepository = userDeviceTokenRepository;
    }

    public NotificationDeliveryDecision execute(ApplyNotificationDeliveryRulesCommand command) {
        validateCommand(command);

        DefaultChannelFlags defaults = NotificationDefaultChannelPolicy.resolve(command.eventType())
                .orElseThrow(() -> new AppException(
                        ErrorCode.UNKNOWN_EVENT_TYPE,
                        "Unknown event type for delivery rules",
                        "eventType",
                        "Event type is not configured for delivery."
                ));

        Optional<UserNotificationSetting> userSetting = userNotificationSettingRepository.findByUserIdAndEventType(
                command.userId(),
                command.eventType()
        );

        boolean inApp = resolveInApp(userSetting, defaults, command.eventType());
        boolean email = resolveEmail(userSetting, defaults, command.eventType());
        boolean push = resolvePush(userSetting, defaults, command.eventType(), command.userId());

        return new NotificationDeliveryDecision(inApp, push, email);
    }

    private boolean resolveInApp(
            Optional<UserNotificationSetting> userSetting,
            DefaultChannelFlags defaults,
            String eventType
    ) {
        if (NotificationCriticalOverridePolicy.isMandatoryInApp(eventType)) {
            return true;
        }
        return userSetting.map(UserNotificationSetting::allowInApp).orElse(defaults.inApp());
    }

    private boolean resolveEmail(
            Optional<UserNotificationSetting> userSetting,
            DefaultChannelFlags defaults,
            String eventType
    ) {
        if (!defaults.email()) {
            return false;
        }
        if (NotificationCriticalOverridePolicy.forcesEmail(eventType)) {
            return true;
        }
        return userSetting.map(UserNotificationSetting::allowEmail).orElse(defaults.email());
    }

    private boolean resolvePush(
            Optional<UserNotificationSetting> userSetting,
            DefaultChannelFlags defaults,
            String eventType,
            UUID userId
    ) {
        if (!defaults.push()) {
            return false;
        }
        boolean allowPush = userSetting.map(UserNotificationSetting::allowPush).orElse(defaults.push());
        if (NotificationCriticalOverridePolicy.forcesPush(eventType)) {
            allowPush = true;
        }
        if (allowPush && !userDeviceTokenRepository.existsActiveByUserId(userId)) {
            return false;
        }
        return allowPush;
    }

    private void validateCommand(ApplyNotificationDeliveryRulesCommand command) {
        if (command.userId() == null) {
            throw validationError("userId", "User id is required.");
        }
        if (command.eventType() == null || command.eventType().isBlank()) {
            throw validationError("eventType", "Event type must not be blank.");
        }
    }

    private AppException validationError(String field, String reason) {
        return new AppException(ErrorCode.VALIDATION_ERROR, "Validation failed", field, reason);
    }
}
