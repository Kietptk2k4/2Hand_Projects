package com.twohands.notification_service.delivery.http.devicetoken;

import com.twohands.notification_service.application.devicetoken.RegisterDeviceTokenCommand;
import com.twohands.notification_service.application.devicetoken.RegisterDeviceTokenResult;
import com.twohands.notification_service.application.devicetoken.RegisterDeviceTokenUseCase;
import com.twohands.notification_service.common.dto.ApiResponse;
import com.twohands.notification_service.delivery.http.devicetoken.request.RegisterDeviceTokenRequest;
import com.twohands.notification_service.delivery.http.devicetoken.response.RegisterDeviceTokenResponse;
import com.twohands.notification_service.security.AuthenticationSupport;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notification/device-tokens")
public class DeviceTokenController {

    private final RegisterDeviceTokenUseCase registerDeviceTokenUseCase;

    public DeviceTokenController(RegisterDeviceTokenUseCase registerDeviceTokenUseCase) {
        this.registerDeviceTokenUseCase = registerDeviceTokenUseCase;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<RegisterDeviceTokenResponse>> registerDeviceToken(
            @Valid @RequestBody RegisterDeviceTokenRequest request,
            Authentication authentication
    ) {
        UUID userId = AuthenticationSupport.requireUserId(authentication);
        RegisterDeviceTokenResult result = registerDeviceTokenUseCase.execute(
                new RegisterDeviceTokenCommand(userId, request.deviceType(), request.deviceToken())
        );

        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                registerDeviceTokenUseCase.successMessage(),
                toResponse(result)
        ));
    }

    private RegisterDeviceTokenResponse toResponse(RegisterDeviceTokenResult result) {
        return new RegisterDeviceTokenResponse(
                result.id(),
                result.deviceType(),
                result.active(),
                result.createdAt(),
                result.updatedAt(),
                result.lastUsedAt(),
                result.alreadyRegistered()
        );
    }
}
