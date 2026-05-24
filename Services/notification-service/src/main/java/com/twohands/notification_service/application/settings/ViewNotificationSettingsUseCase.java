package com.twohands.notification_service.application.settings;

import com.twohands.notification_service.domain.notificationsetting.EffectiveNotificationSetting;
import com.twohands.notification_service.domain.notificationsetting.UserNotificationSetting;
import com.twohands.notification_service.domain.notificationsetting.UserNotificationSettingRepository;
import com.twohands.notification_service.domain.notificationsetting.ViewNotificationSettingsPolicy;
import com.twohands.notification_service.exception.AppException;
import com.twohands.notification_service.exception.ErrorCode;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ViewNotificationSettingsUseCase {

    private final UserNotificationSettingRepository userNotificationSettingRepository;

    public ViewNotificationSettingsUseCase(UserNotificationSettingRepository userNotificationSettingRepository) {
        this.userNotificationSettingRepository = userNotificationSettingRepository;
    }

    public ViewNotificationSettingsResult execute(ViewNotificationSettingsCommand command) {
        validateCommand(command);

        UUID userId = command.userId();
        Map<String, UserNotificationSetting> explicitSettingsByEventType =
                userNotificationSettingRepository.findByUserId(userId).stream()
                        .collect(Collectors.toMap(UserNotificationSetting::eventType, Function.identity()));

        List<EffectiveNotificationSetting> settings =
                ViewNotificationSettingsPolicy.resolve(explicitSettingsByEventType);

        return new ViewNotificationSettingsResult(userId, settings);
    }

    public String successMessage() {
        return "Notification settings retrieved successfully";
    }

    private void validateCommand(ViewNotificationSettingsCommand command) {
        if (command.userId() == null) {
            throw new AppException(ErrorCode.UNAUTHORIZED, "Authentication required");
        }
    }
}
