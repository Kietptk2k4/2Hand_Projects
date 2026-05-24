package com.twohands.notification_service.delivery.http.notification;

import com.twohands.notification_service.application.read.ViewUserNotificationsCommand;
import com.twohands.notification_service.application.read.ViewUserNotificationsResult;
import com.twohands.notification_service.application.read.ViewUserNotificationsUseCase;
import com.twohands.notification_service.common.dto.ApiResponse;
import com.twohands.notification_service.delivery.http.notification.mapper.ViewUserNotificationsHttpMapper;
import com.twohands.notification_service.delivery.http.notification.response.ViewUserNotificationsResponse;
import com.twohands.notification_service.security.AuthenticationSupport;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notification/notifications")
public class NotificationController {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;

    private final ViewUserNotificationsUseCase viewUserNotificationsUseCase;
    private final ViewUserNotificationsHttpMapper viewUserNotificationsHttpMapper;

    public NotificationController(
            ViewUserNotificationsUseCase viewUserNotificationsUseCase,
            ViewUserNotificationsHttpMapper viewUserNotificationsHttpMapper
    ) {
        this.viewUserNotificationsUseCase = viewUserNotificationsUseCase;
        this.viewUserNotificationsHttpMapper = viewUserNotificationsHttpMapper;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<ViewUserNotificationsResponse>> viewUserNotifications(
            @RequestParam(name = "page", defaultValue = "" + DEFAULT_PAGE) int page,
            @RequestParam(name = "size", defaultValue = "" + DEFAULT_SIZE) int size,
            Authentication authentication
    ) {
        UUID userId = AuthenticationSupport.requireUserId(authentication);
        ViewUserNotificationsResult result = viewUserNotificationsUseCase.execute(
                new ViewUserNotificationsCommand(userId, page, size)
        );

        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                viewUserNotificationsUseCase.successMessage(),
                viewUserNotificationsHttpMapper.toResponse(result)
        ));
    }
}
