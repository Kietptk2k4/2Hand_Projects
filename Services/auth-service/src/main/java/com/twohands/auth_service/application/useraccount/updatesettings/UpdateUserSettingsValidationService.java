package com.twohands.auth_service.application.useraccount.updatesettings;

import com.twohands.auth_service.domain.user.AppearanceMode;
import com.twohands.auth_service.exception.AppException;
import com.twohands.auth_service.exception.ErrorCode;
import org.springframework.stereotype.Service;

@Service
public class UpdateUserSettingsValidationService {

    public AppearanceMode validateAndParseAppearanceMode(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Appearance mode is required", "appearance_mode", "REQUIRED");
        }
        try {
            return AppearanceMode.valueOf(rawValue.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new AppException(
                    ErrorCode.VALIDATION_ERROR,
                    "Appearance mode must be LIGHT, DARK or SYSTEM",
                    "appearance_mode",
                    "INVALID_ENUM"
            );
        }
    }
}
