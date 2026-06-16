package com.twohands.auth_service.config;

import java.net.URI;

/**
 * Chooses the MinIO endpoint used by the presign {@link io.minio.MinioClient}.
 * <ul>
 *   <li>bootRun on host: browser-reachable host from {@code public-url} (usually localhost)</li>
 *   <li>Docker: same — origin from {@code public-url}; compose maps {@code localhost:host-gateway}
 *       so the SDK can reach published MinIO while signing a browser URL</li>
 * </ul>
 */
public final class MinioPresignEndpointResolver {

    private MinioPresignEndpointResolver() {
    }

    public static String resolve(String internalEndpoint, String configuredPresignedEndpoint, String publicUrl) {
        if (configuredPresignedEndpoint != null && !configuredPresignedEndpoint.isBlank()) {
            return configuredPresignedEndpoint.trim();
        }
        return resolveAutomatic(internalEndpoint, publicUrl);
    }

    static String resolveAutomatic(String internalEndpoint, String publicUrl) {
        URI internal = URI.create(internalEndpoint.trim());
        String internalHost = internal.getHost();
        if (internalHost == null) {
            return internalEndpoint;
        }

        if (isLocalHost(internalHost)) {
            return internalEndpoint;
        }

        return browserOriginFromPublicUrl(publicUrl, internal);
    }

    private static String browserOriginFromPublicUrl(String publicUrl, URI internalFallback) {
        if (publicUrl == null || publicUrl.isBlank()) {
            return internalFallback.toString();
        }
        URI publicUri = URI.create(trimTrailingSlash(publicUrl.trim()));
        if (publicUri.getHost() == null) {
            return internalFallback.toString();
        }
        return buildOrigin(publicUri.getScheme(), publicUri.getHost(), portOf(publicUri));
    }

    private static String buildOrigin(String scheme, String host, int port) {
        String normalizedScheme = scheme != null && !scheme.isBlank() ? scheme : "http";
        return normalizedScheme + "://" + host + ":" + port;
    }

    private static int portOf(URI uri) {
        if (uri.getPort() > 0) {
            return uri.getPort();
        }
        return "https".equalsIgnoreCase(uri.getScheme()) ? 443 : 9000;
    }

    private static boolean isLocalHost(String host) {
        return "localhost".equalsIgnoreCase(host) || "127.0.0.1".equals(host);
    }

    private static String trimTrailingSlash(String value) {
        String trimmed = value;
        while (trimmed.endsWith("/")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        return trimmed;
    }
}
