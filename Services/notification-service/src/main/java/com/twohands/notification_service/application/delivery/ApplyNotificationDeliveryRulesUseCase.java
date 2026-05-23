package com.twohands.notification_service.application.delivery;

import com.twohands.notification_service.domain.delivery.NotificationCriticalOverridePolicy;
import com.twohands.notification_service.domain.delivery.NotificationDefaultChannelPolicy;
import com.twohands.notification_service.domain.delivery.NotificationDeliveryDecision;
import com.twohands.notification_service.domain.devicetoken.UserDeviceTokenRepository;
import org.springframework.stereotype.Service;

@Service
public class ApplyNotificationDeliveryRulesUseCase {

    private final RespectNotificationSettingsUseCase respectNotificationSettingsUseCase;
    private final UserDeviceTokenRepository userDeviceTokenRepository;

    public ApplyNotificationDeliveryRulesUseCase(
            RespectNotificationSettingsUseCase respectNotificationSettingsUseCase,
            UserDeviceTokenRepository userDeviceTokenRepository
    ) {
        this.respectNotificationSettingsUseCase = respectNotificationSettingsUseCase;
        this.userDeviceTokenRepository = userDeviceTokenRepository;
    }

    public NotificationDeliveryDecision execute(ApplyNotificationDeliveryRulesCommand command) {
        RespectNotificationSettingsResult settings = respectNotificationSettingsUseCase.execute(
                new RespectNotificationSettingsCommand(command.userId(), command.eventType())
        );

        var defaults = NotificationDefaultChannelPolicy.resolve(command.eventType()).orElseThrow();

        boolean inApp = resolveInApp(settings, command.eventType());
        boolean email = resolveEmail(settings, defaults, command.eventType());
        boolean push = resolvePush(settings, defaults, command.eventType(), command.userId());

        return new NotificationDeliveryDecision(inApp, push, email);
    }

    private boolean resolveInApp(RespectNotificationSettingsResult settings, String eventType) {
        if (NotificationCriticalOverridePolicy.isMandatoryInApp(eventType)) {
            return true;
        }
        return settings.allowInApp();
    }

    private boolean resolveEmail(
            RespectNotificationSettingsResult settings,
            com.twohands.notification_service.domain.delivery.DefaultChannelFlags defaults,
            String eventType
    ) {
        if (!defaults.email()) {
            return false;
        }
        if (NotificationCriticalOverridePolicy.forcesEmail(eventType)) {
            return true;
        }
        return settings.allowEmail();
    }

    private boolean resolvePush(
            RespectNotificationSettingsResult settings,
            com.twohands.notification_service.domain.delivery.DefaultChannelFlags defaults,
            String eventType,
            java.util.UUID userId
    ) {
        if (!defaults.push()) {
            return false;
        }
        boolean allowPush = settings.allowPush();
        if (NotificationCriticalOverridePolicy.forcesPush(eventType)) {
            allowPush = true;
        }
        if (allowPush && !userDeviceTokenRepository.existsActiveByUserId(userId)) {
            return false;
        }
        return allowPush;
    }
}
