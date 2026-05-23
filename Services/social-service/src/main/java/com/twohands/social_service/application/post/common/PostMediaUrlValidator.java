package com.twohands.social_service.application.post.common;

import com.twohands.social_service.config.SocialObjectStorageProperties;
import com.twohands.social_service.exception.AppException;
import com.twohands.social_service.exception.ErrorCode;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class PostMediaUrlValidator {

    private final SocialObjectStorageProperties properties;

    public PostMediaUrlValidator(SocialObjectStorageProperties properties) {
        this.properties = properties;
    }

    public void validateMediaUrls(UUID authorId, List<String> mediaUrls) {
        if (!properties.isEnabled() || mediaUrls == null || mediaUrls.isEmpty()) {
            return;
        }

        String allowedPrefix = buildAllowedUrlPrefix(authorId);
        for (int index = 0; index < mediaUrls.size(); index++) {
            String url = mediaUrls.get(index);
            if (url == null || url.isBlank()) {
                continue;
            }
            if (!url.startsWith(allowedPrefix)) {
                throw new AppException(
                        ErrorCode.VALIDATION_ERROR,
                        "Validation failed",
                        "media[" + index + "].url",
                        "URL media khong hop le hoac khong thuoc nguoi dung."
                );
            }
        }
    }

    public String buildAllowedUrlPrefix(UUID userId) {
        String base = properties.getPublicUrl();
        if (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        String prefix = properties.getPublicPathPrefix();
        if (prefix.startsWith("/")) {
            prefix = prefix.substring(1);
        }
        if (prefix.endsWith("/")) {
            prefix = prefix.substring(0, prefix.length() - 1);
        }
        return base + "/" + prefix + "/posts/" + userId + "/";
    }
}
