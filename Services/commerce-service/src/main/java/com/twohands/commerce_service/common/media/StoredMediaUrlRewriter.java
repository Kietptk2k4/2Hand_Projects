package com.twohands.commerce_service.common.media;

import java.net.URI;
import java.util.regex.Pattern;

/**
 * Rewrites persisted dev MinIO URLs (e.g. {@code http://localhost:9000/2hands-commerce-product/...})
 * to the browser-reachable origin from {@code COMMERCE_MINIO_PUBLIC_URL} at response time.
 */
public final class StoredMediaUrlRewriter {

    private static final Pattern TWO_HANDS_OBJECT_PATH = Pattern.compile(
            "/2hands-(?:avatar|social-post|commerce-product|commerce-review|commerce-shop)(?:/|$)"
    );

    private StoredMediaUrlRewriter() {
    }

    public static boolean isTwoHandsObjectMediaUrl(String url) {
        if (url == null || url.isBlank()) {
            return false;
        }
        try {
            String path = URI.create(url.trim()).getPath();
            return path != null && TWO_HANDS_OBJECT_PATH.matcher(path).find();
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    public static String rewrite(String storedUrl, String configuredPublicUrl) {
        if (storedUrl == null || storedUrl.isBlank()) {
            return storedUrl;
        }
        if (configuredPublicUrl == null || configuredPublicUrl.isBlank()) {
            return storedUrl;
        }
        if (!isTwoHandsObjectMediaUrl(storedUrl)) {
            return storedUrl;
        }

        try {
            URI stored = URI.create(storedUrl.trim());
            String path = stored.getPath();
            if (path == null || path.isBlank()) {
                return storedUrl;
            }

            URI publicBase = URI.create(trimTrailingSlash(configuredPublicUrl.trim()));
            String scheme = publicBase.getScheme() != null ? publicBase.getScheme() : "https";
            String host = publicBase.getHost();
            if (host == null) {
                return storedUrl;
            }

            String authority = formatAuthority(scheme, host, publicBase.getPort());
            StringBuilder out = new StringBuilder()
                    .append(scheme)
                    .append("://")
                    .append(authority)
                    .append(path);
            if (stored.getRawQuery() != null) {
                out.append('?').append(stored.getRawQuery());
            }
            if (stored.getRawFragment() != null) {
                out.append('#').append(stored.getRawFragment());
            }
            return out.toString();
        } catch (IllegalArgumentException ex) {
            return storedUrl;
        }
    }

    private static String formatAuthority(String scheme, String host, int port) {
        if (port > 0 && !isDefaultPort(scheme, port)) {
            return host + ":" + port;
        }
        return host;
    }

    private static boolean isDefaultPort(String scheme, int port) {
        return ("https".equalsIgnoreCase(scheme) && port == 443)
                || ("http".equalsIgnoreCase(scheme) && port == 80);
    }

    private static String trimTrailingSlash(String value) {
        String trimmed = value;
        while (trimmed.endsWith("/")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        return trimmed;
    }
}
