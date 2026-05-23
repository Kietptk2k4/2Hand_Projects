package com.twohands.notification_service.application.delivery;

import com.twohands.notification_service.domain.delivery.DefaultChannelFlags;
import com.twohands.notification_service.domain.delivery.NotificationChannelPreferences;
import com.twohands.notification_service.domain.delivery.NotificationDefaultChannelPolicy;
import com.twohands.notification_service.domain.delivery.RespectNotificationSettingsPolicy;
import com.twohands.notification_service.domain.notificationsetting.UserNotificationSetting;
import com.twohands.notification_service.domain.notificationsetting.UserNotificationSettingRepository;
import com.twohands.notification_service.exception.AppException;
import com.twohands.notification_service.exception.ErrorCode;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class RespectNotificationSettingsUseCase {

    private final UserNotificationSettingRepository userNotificationSettingRepository;

    public RespectNotificationSettingsUseCase(UserNotificationSettingRepository userNotificationSettingRepository) {
        this.userNotificationSettingRepository = userNotificationSettingRepository;
    }

    public RespectNotificationSettingsResult execute(RespectNotificationSettingsCommand command) {
        validateCommand(command);

        DefaultChannelFlags defaults = NotificationDefaultChannelPolicy.resolve(command.eventType())
                .orElseThrow(() -> new AppException(
                        ErrorCode.UNKNOWN_EVENT_TYPE,
                        "Unknown event type for notification settings",
                        "eventType",
                        "Event type is not configured for delivery."
                ));

        Optional<UserNotificationSetting> userSetting = userNotificationSettingRepository.findByUserIdAndEventType(
                command.userId(),
                command.eventType()
        );

        NotificationChannelPreferences preferences = RespectNotificationSettingsPolicy.resolve(userSetting, defaults);

        return new RespectNotificationSettingsResult(
                command.userId(),
                command.eventType(),
                preferences.inApp(),
                preferences.push(),
                preferences.email(),
                userSetting.isPresent()
        );
    }

    private void validateCommand(RespectNotificationSettingsCommand command) {
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
