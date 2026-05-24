package com.twohands.notification_service.delivery.http.settings;

import com.twohands.notification_service.application.settings.ViewNotificationSettingsCommand;
import com.twohands.notification_service.application.settings.ViewNotificationSettingsResult;
import com.twohands.notification_service.application.settings.ViewNotificationSettingsUseCase;
import com.twohands.notification_service.common.dto.ApiResponse;
import com.twohands.notification_service.delivery.http.settings.mapper.ViewNotificationSettingsHttpMapper;
import com.twohands.notification_service.delivery.http.settings.response.ViewNotificationSettingsResponse;
import com.twohands.notification_service.security.AuthenticationSupport;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notification/notification-settings")
public class NotificationSettingsController {

    private final ViewNotificationSettingsUseCase viewNotificationSettingsUseCase;
    private final ViewNotificationSettingsHttpMapper viewNotificationSettingsHttpMapper;

    public NotificationSettingsController(
            ViewNotificationSettingsUseCase viewNotificationSettingsUseCase,
            ViewNotificationSettingsHttpMapper viewNotificationSettingsHttpMapper
    ) {
        this.viewNotificationSettingsUseCase = viewNotificationSettingsUseCase;
        this.viewNotificationSettingsHttpMapper = viewNotificationSettingsHttpMapper;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<ViewNotificationSettingsResponse>> viewNotificationSettings(
            Authentication authentication
    ) {
        UUID userId = AuthenticationSupport.requireUserId(authentication);
        ViewNotificationSettingsResult result = viewNotificationSettingsUseCase.execute(
                new ViewNotificationSettingsCommand(userId)
        );

        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                viewNotificationSettingsUseCase.successMessage(),
                viewNotificationSettingsHttpMapper.toResponse(result)
        ));
    }
}
