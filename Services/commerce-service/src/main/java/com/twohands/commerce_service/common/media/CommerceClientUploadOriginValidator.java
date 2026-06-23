package com.twohands.commerce_service.common.media;

import com.twohands.commerce_service.config.CommerceObjectStorageProperties;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.Locale;

@Component
public class CommerceClientUploadOriginValidator {

    private final CommerceObjectStorageProperties properties;

    public CommerceClientUploadOriginValidator(CommerceObjectStorageProperties properties) {
        this.properties = properties;
    }

    /**
     * Optional dev-only override so mobile can presign for a LAN-reachable MinIO host.
     * Returns normalized origin (scheme + host + port) or null when omitted.
     */
    public String validate(String rawClientUploadOrigin) {
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

    private AppException fieldError(String field, String reason) {
        return new AppException(ErrorCode.VALIDATION_ERROR, "Validation failed", field, reason);
    }
}
