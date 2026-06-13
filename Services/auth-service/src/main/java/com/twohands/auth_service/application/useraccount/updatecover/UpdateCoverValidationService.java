package com.twohands.auth_service.application.useraccount.updatecover;

import com.twohands.auth_service.exception.AppException;
import com.twohands.auth_service.exception.ErrorCode;
import org.springframework.stereotype.Service;

import java.net.URI;

@Service
public class UpdateCoverValidationService {

    public void validate(UpdateCoverCommand command) {
        String coverUrl = command.coverUrl();
        if (coverUrl == null || coverUrl.isBlank()) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Cover URL is required", "cover_url", "REQUIRED");
        }

        try {
            URI uri = URI.create(coverUrl.trim());
            String scheme = uri.getScheme();
            if (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme)) {
                throw new IllegalArgumentException("Only http/https are allowed");
            }
            if (uri.getHost() == null || uri.getHost().isBlank()) {
                throw new IllegalArgumentException("URL host is required");
            }
        } catch (Exception ex) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Cover URL format is invalid", "cover_url", "INVALID_FORMAT");
        }
    }
}
