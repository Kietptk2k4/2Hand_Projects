package com.twohands.auth_service.application.useraccount.updateavatar;

import com.twohands.auth_service.exception.AppException;
import com.twohands.auth_service.exception.ErrorCode;
import org.springframework.stereotype.Service;

import java.net.URI;

@Service
public class UpdateAvatarValidationService {

    public void validate(UpdateAvatarCommand command) {
        String avatarUrl = command.avatarUrl();
        if (avatarUrl == null || avatarUrl.isBlank()) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Avatar URL is required", "avatar_url", "REQUIRED");
        }

        try {
            URI uri = URI.create(avatarUrl.trim());
            String scheme = uri.getScheme();
            if (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme)) {
                throw new IllegalArgumentException("Only http/https are allowed");
            }
            if (uri.getHost() == null || uri.getHost().isBlank()) {
                throw new IllegalArgumentException("URL host is required");
            }
        } catch (Exception ex) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Avatar URL format is invalid", "avatar_url", "INVALID_FORMAT");
        }
    }
}
