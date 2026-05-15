package com.twohands.auth_service.application.auth.refresh;

import com.twohands.auth_service.exception.AppException;
import com.twohands.auth_service.exception.ErrorCode;
import org.springframework.stereotype.Service;

@Service
public class RefreshAccessTokenValidationService {

    public void validateRefreshToken(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Refresh token is required", "refresh_token", "REQUIRED");
        }
    }
}
