package com.twohands.commerce_service.common.vnpay;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
public class VnpayFrontendReturnUrlValidator {

    public static final String FRONTEND_RETURN_PATH = "/commerce/checkout/vnpay-return";
    public static final String BACKEND_RETURN_PATH = "/commerce/api/v1/payments/vnpay/return";

    private final List<String> allowedWebOrigins;
    private final List<Pattern> allowedWebOriginPatterns;
    private final Set<String> allowedAppSchemes;

    public VnpayFrontendReturnUrlValidator(
            @Value("${app.cors.allowed-origins}") String corsOrigins,
            @Value("${app.cors.allowed-origin-patterns:}") String corsOriginPatterns,
            @Value("${commerce.integrations.vnpay.allowed-app-schemes:twohands,exp}") String appSchemes
    ) {
        this.allowedWebOrigins = parseOrigins(corsOrigins);
        this.allowedWebOriginPatterns = parseOriginPatterns(corsOriginPatterns);
        this.allowedAppSchemes = Arrays.stream(appSchemes.split(","))
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .map(value -> value.toLowerCase(Locale.ROOT))
                .collect(Collectors.toSet());
    }

    public boolean isAllowedFrontendReturnUrl(String returnUrl) {
        if (returnUrl == null || returnUrl.isBlank()) {
            return false;
        }

        try {
            URI uri = URI.create(returnUrl.trim());
            String scheme = uri.getScheme();
            if (scheme == null) {
                return false;
            }

            if (allowedAppSchemes.contains(scheme.toLowerCase(Locale.ROOT))) {
                return hasFrontendReturnPath(uri);
            }

            if ("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme)) {
                return isAllowedWebFrontendReturn(uri);
            }

            return false;
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    public boolean isAllowedBackendReturnUrl(String returnUrl, HttpServletRequest request) {
        if (returnUrl == null || returnUrl.isBlank()) {
            return false;
        }

        try {
            URI uri = URI.create(returnUrl.trim());
            if (!hasBackendReturnPath(uri)) {
                return false;
            }

            String origin = normalizeOrigin(uri.getScheme(), uri.getHost(), uri.getPort());
            if (origin.equals(normalizeRequestOrigin(request))) {
                return true;
            }

            return allowedWebOrigins.contains(origin) || matchesAllowedOriginPattern(origin);
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    private boolean isAllowedWebOrigin(String origin) {
        return allowedWebOrigins.contains(origin) || matchesAllowedOriginPattern(origin);
    }

    private boolean matchesAllowedOriginPattern(String origin) {
        for (Pattern pattern : allowedWebOriginPatterns) {
            if (pattern.matcher(origin).matches()) {
                return true;
            }
        }
        return false;
    }

    private boolean isAllowedWebFrontendReturn(URI uri) {
        if (!hasFrontendReturnPath(uri)) {
            return false;
        }
        String origin = normalizeOrigin(uri.getScheme(), uri.getHost(), uri.getPort());
        return isAllowedWebOrigin(origin);
    }

    private static boolean hasFrontendReturnPath(URI uri) {
        return uri.toString().toLowerCase(Locale.ROOT).contains("commerce/checkout/vnpay-return");
    }

    private static boolean hasBackendReturnPath(URI uri) {
        String path = uri.getPath();
        if (path == null) {
            return false;
        }
        String normalized = path.endsWith("/") ? path.substring(0, path.length() - 1) : path;
        return normalized.endsWith(BACKEND_RETURN_PATH);
    }

    private static String normalizeRequestOrigin(HttpServletRequest request) {
        String forwardedProto = request.getHeader("X-Forwarded-Proto");
        String forwardedHost = request.getHeader("X-Forwarded-Host");
        if (forwardedProto != null && !forwardedProto.isBlank()
                && forwardedHost != null && !forwardedHost.isBlank()) {
            return forwardedProto.trim().toLowerCase(Locale.ROOT) + "://" + forwardedHost.trim().toLowerCase(Locale.ROOT);
        }

        return ServletUriComponentsBuilder.fromRequest(request)
                .replacePath(null)
                .replaceQuery(null)
                .build()
                .toUriString()
                .replaceAll("/$", "");
    }

    private static String normalizeOrigin(String scheme, String host, int port) {
        if (scheme == null || host == null) {
            return "";
        }
        String lowerScheme = scheme.toLowerCase(Locale.ROOT);
        String lowerHost = host.toLowerCase(Locale.ROOT);
        boolean defaultPort = ("http".equals(lowerScheme) && port == 80)
                || ("https".equals(lowerScheme) && port == 443)
                || port < 0;
        if (defaultPort) {
            return lowerScheme + "://" + lowerHost;
        }
        return lowerScheme + "://" + lowerHost + ":" + port;
    }

    private static List<String> parseOrigins(String corsOrigins) {
        return Arrays.stream(corsOrigins.split(","))
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .map(value -> value.endsWith("/") ? value.substring(0, value.length() - 1) : value)
                .map(value -> value.toLowerCase(Locale.ROOT))
                .toList();
    }

    private static List<Pattern> parseOriginPatterns(String corsOriginPatterns) {
        return Arrays.stream(corsOriginPatterns.split(","))
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .map(value -> value.endsWith("/") ? value.substring(0, value.length() - 1) : value)
                .map(value -> value.toLowerCase(Locale.ROOT))
                .map(VnpayFrontendReturnUrlValidator::toOriginPatternRegex)
                .map(regex -> Pattern.compile(regex, Pattern.CASE_INSENSITIVE))
                .toList();
    }

    private static String toOriginPatternRegex(String pattern) {
        StringBuilder regex = new StringBuilder("^");
        int start = 0;
        int wildcardIndex = pattern.indexOf('*');
        while (wildcardIndex >= 0) {
            if (wildcardIndex > start) {
                regex.append(Pattern.quote(pattern.substring(start, wildcardIndex)));
            }
            regex.append(".*");
            start = wildcardIndex + 1;
            wildcardIndex = pattern.indexOf('*', start);
        }
        if (start < pattern.length()) {
            regex.append(Pattern.quote(pattern.substring(start)));
        }
        regex.append("$");
        return regex.toString();
    }
}
