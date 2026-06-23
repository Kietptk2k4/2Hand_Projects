package com.twohands.social_service.application.post.common;

import com.twohands.social_service.config.SocialObjectStorageProperties;
import com.twohands.social_service.exception.AppException;
import com.twohands.social_service.exception.ErrorCode;
import org.springframework.stereotype.Service;

import java.net.URI;
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
            String normalizedUrl = properties.rewriteLegacyMediaUrl(url);
            if (isAllowedMediaUrl(authorId, allowedPrefix, url, normalizedUrl)) {
                continue;
            }
            throw new AppException(
                    ErrorCode.VALIDATION_ERROR,
                    "Validation failed",
                    "media[" + index + "].url",
                    "URL media khong hop le hoac khong thuoc nguoi dung."
            );
        }
    }

    private boolean isAllowedMediaUrl(
            UUID authorId,
            String allowedPrefix,
            String rawUrl,
            String normalizedUrl
    ) {
        if (normalizedUrl.startsWith(allowedPrefix) || rawUrl.startsWith(allowedPrefix)) {
            return true;
        }
        return matchesUserPostObjectPath(authorId, normalizedUrl)
                || matchesUserPostObjectPath(authorId, rawUrl);
    }

    /**
     * Accepts alternate dev hosts (e.g. LAN IP from client_upload_origin presign) when the object
     * path still points at this author's post media.
     */
    private boolean matchesUserPostObjectPath(UUID authorId, String url) {
        if (url == null || url.isBlank()) {
            return false;
        }
        try {
            String path = URI.create(url.trim()).getPath();
            if (path == null || path.isBlank()) {
                return false;
            }
            String pathPrefix = properties.getPublicPathPrefix();
            String marker = pathPrefix == null || pathPrefix.isBlank()
                    ? "/posts/" + authorId + "/"
                    : "/" + pathPrefix.replaceAll("^/+|/+$", "") + "/posts/" + authorId + "/";
            return path.contains(marker);
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    public String buildAllowedUrlPrefix(UUID userId) {
        return properties.buildAllowedMediaUrlPrefix(userId);
    }
}
