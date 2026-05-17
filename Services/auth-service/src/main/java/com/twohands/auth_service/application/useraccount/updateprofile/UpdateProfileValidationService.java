package com.twohands.auth_service.application.useraccount.updateprofile;

import com.twohands.auth_service.exception.AppException;
import com.twohands.auth_service.exception.ErrorCode;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.Map;

@Service
public class UpdateProfileValidationService {

    public void validate(UpdateProfileCommand command) {
        if (command.displayName() == null || command.displayName().isBlank()) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Display name is required", "display_name", "REQUIRED");
        }
        String displayName = command.displayName().trim();
        if (displayName.length() > 100) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Display name max length is 100", "display_name", "MAX_LENGTH");
        }
        if (command.bio() != null && command.bio().length() > 500) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Bio max length is 500", "bio", "MAX_LENGTH");
        }

        validateUrl(command.website(), "website");
        validateSocialLinks(command.socialLinks());
    }

    private void validateSocialLinks(Map<String, String> socialLinks) {
        if (socialLinks == null) {
            return;
        }
        if (socialLinks.size() > 10) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Social links max size is 10", "social_links", "MAX_SIZE");
        }
        for (Map.Entry<String, String> entry : socialLinks.entrySet()) {
            if (entry.getKey() == null || entry.getKey().isBlank()) {
                throw new AppException(ErrorCode.VALIDATION_ERROR, "Social link key is required", "social_links", "INVALID_KEY");
            }
            validateUrl(entry.getValue(), "social_links");
        }
    }

    private void validateUrl(String rawUrl, String field) {
        if (rawUrl == null || rawUrl.isBlank()) {
            return;
        }
        try {
            URI uri = URI.create(rawUrl.trim());
            String scheme = uri.getScheme();
            if (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme)) {
                throw new IllegalArgumentException("Only http/https are allowed");
            }
            if (uri.getHost() == null || uri.getHost().isBlank()) {
                throw new IllegalArgumentException("URL host is required");
            }
        } catch (Exception ex) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "URL format is invalid", field, "INVALID_FORMAT");
        }
    }
}
