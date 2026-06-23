package com.twohands.social_service.application.post.uploadpostmedia;

import com.twohands.social_service.config.SocialObjectStorageProperties;
import com.twohands.social_service.exception.AppException;
import com.twohands.social_service.exception.ErrorCode;
import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Locale;

@Service
public class UploadPostMediaValidationService {

    private final SocialObjectStorageProperties properties;

    public UploadPostMediaValidationService(SocialObjectStorageProperties properties) {
        this.properties = properties;
    }

    public String validateMediaKind(String rawMediaKind) {
        if (rawMediaKind == null || rawMediaKind.isBlank()) {
            throw fieldError("media_kind", "REQUIRED");
        }
        String normalized = rawMediaKind.trim().toUpperCase(Locale.ROOT);
        if (!"IMAGE".equals(normalized) && !"VIDEO".equals(normalized)) {
            throw fieldError("media_kind", "INVALID_VALUE");
        }
        return normalized;
    }

    public String validateContentType(String rawContentType, String mediaKind) {
        if (rawContentType == null || rawContentType.isBlank()) {
            throw fieldError("content_type", "REQUIRED");
        }
        String normalized = rawContentType.trim().toLowerCase(Locale.ROOT);
        List<String> allowed = allowedTypesFor(mediaKind);
        if (!allowed.contains(normalized)) {
            throw fieldError("content_type", "INVALID_VALUE");
        }
        return normalized;
    }

    public void validateFileSize(long fileSizeBytes, String mediaKind) {
        if (fileSizeBytes <= 0) {
            throw fieldError("file_size_bytes", "INVALID_VALUE");
        }
        long maxSize = maxSizeFor(mediaKind);
        if (fileSizeBytes > maxSize) {
            throw fieldError("file_size_bytes", "MAX_SIZE_EXCEEDED");
        }
    }

    /**
     * Optional dev-only override so mobile can presign for a LAN-reachable MinIO host.
     * Returns normalized origin (scheme + host + port) or null when omitted.
     */
    public String validateClientUploadOrigin(String rawClientUploadOrigin) {
        if (rawClientUploadOrigin == null || rawClientUploadOrigin.isBlank()) {
            return null;
        }

        if (!properties.isAllowClientUploadOrigin()) {
            throw fieldError("client_upload_origin", "NOT_ALLOWED");
        }

        URI uri;
        try {
            uri = URI.create(rawClientUploadOrigin.trim());
        } catch (IllegalArgumentException ex) {
            throw fieldError("client_upload_origin", "INVALID_VALUE");
        }

        String scheme = uri.getScheme();
        if (scheme == null || (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme))) {
            throw fieldError("client_upload_origin", "INVALID_VALUE");
        }

        String host = uri.getHost();
        if (host == null || host.isBlank() || !isAllowedDevUploadHost(host)) {
            throw fieldError("client_upload_origin", "INVALID_VALUE");
        }

        int port = uri.getPort() > 0
                ? uri.getPort()
                : ("https".equalsIgnoreCase(scheme) ? 443 : 9000);

        return scheme.toLowerCase(Locale.ROOT) + "://" + host.toLowerCase(Locale.ROOT) + ":" + port;
    }

    private boolean isAllowedDevUploadHost(String host) {
        String normalized = host.toLowerCase(Locale.ROOT);
        if ("localhost".equals(normalized) || "127.0.0.1".equals(normalized) || "10.0.2.2".equals(normalized)) {
            return true;
        }

        try {
            InetAddress address = InetAddress.getByName(host);
            return address.isLoopbackAddress() || address.isLinkLocalAddress() || address.isSiteLocalAddress();
        } catch (UnknownHostException ex) {
            return false;
        }
    }

    public long maxSizeFor(String mediaKind) {
        return "VIDEO".equals(mediaKind)
                ? properties.getVideoMaxFileSizeBytes()
                : properties.getImageMaxFileSizeBytes();
    }

    public List<String> allowedTypesFor(String mediaKind) {
        return "VIDEO".equals(mediaKind)
                ? properties.getAllowedVideoContentTypes()
                : properties.getAllowedImageContentTypes();
    }

    private AppException fieldError(String field, String reason) {
        return new AppException(ErrorCode.VALIDATION_ERROR, "Validation failed", field, reason);
    }
}
