package com.twohands.notification_service.delivery.http.notification;

import com.twohands.notification_service.application.read.CountUnreadNotificationsCommand;
import com.twohands.notification_service.application.read.CountUnreadNotificationsResult;
import com.twohands.notification_service.application.read.CountUnreadNotificationsUseCase;
import com.twohands.notification_service.application.read.ViewUnreadNotificationsCommand;
import com.twohands.notification_service.application.read.ViewUnreadNotificationsUseCase;
import com.twohands.notification_service.application.read.ViewUserNotificationsCommand;
import com.twohands.notification_service.application.read.ViewUserNotificationsResult;
import com.twohands.notification_service.application.read.ViewUserNotificationsUseCase;
import com.twohands.notification_service.common.dto.ApiResponse;
import com.twohands.notification_service.delivery.http.notification.mapper.ViewUserNotificationsHttpMapper;
import com.twohands.notification_service.delivery.http.notification.response.CountUnreadNotificationsResponse;
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
    private final ViewUnreadNotificationsUseCase viewUnreadNotificationsUseCase;
    private final CountUnreadNotificationsUseCase countUnreadNotificationsUseCase;
    private final ViewUserNotificationsHttpMapper viewUserNotificationsHttpMapper;

    public NotificationController(
            ViewUserNotificationsUseCase viewUserNotificationsUseCase,
            ViewUnreadNotificationsUseCase viewUnreadNotificationsUseCase,
            CountUnreadNotificationsUseCase countUnreadNotificationsUseCase,
            ViewUserNotificationsHttpMapper viewUserNotificationsHttpMapper
    ) {
        this.viewUserNotificationsUseCase = viewUserNotificationsUseCase;
        this.viewUnreadNotificationsUseCase = viewUnreadNotificationsUseCase;
        this.countUnreadNotificationsUseCase = countUnreadNotificationsUseCase;
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

    @GetMapping("/unread")
    public ResponseEntity<ApiResponse<ViewUserNotificationsResponse>> viewUnreadNotifications(
            @RequestParam(name = "page", defaultValue = "" + DEFAULT_PAGE) int page,
            @RequestParam(name = "size", defaultValue = "" + DEFAULT_SIZE) int size,
            Authentication authentication
    ) {
        UUID userId = AuthenticationSupport.requireUserId(authentication);
        ViewUserNotificationsResult result = viewUnreadNotificationsUseCase.execute(
                new ViewUnreadNotificationsCommand(userId, page, size)
        );

        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                viewUnreadNotificationsUseCase.successMessage(),
                viewUserNotificationsHttpMapper.toResponse(result)
        ));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<CountUnreadNotificationsResponse>> countUnreadNotifications(
            Authentication authentication
    ) {
        UUID userId = AuthenticationSupport.requireUserId(authentication);
        CountUnreadNotificationsResult result = countUnreadNotificationsUseCase.execute(
                new CountUnreadNotificationsCommand(userId)
        );

        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                countUnreadNotificationsUseCase.successMessage(),
                new CountUnreadNotificationsResponse(result.count())
        ));
    }
}
