package com.twohands.auth_service.infrastructure.security.oauth;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class OAuthRedirectUriValidator {

    public static final String MOBILE_COMPLETE_PATH = "/api/v1/auth/oauth/mobile-complete";

    private static final Set<String> WEB_CALLBACK_PATHS = Set.of("/oauth/success", "/oauth/failure");
    private static final Set<String> APP_CALLBACK_PATHS = Set.of("/oauth/success", "/oauth/failure");

    private final List<String> allowedWebOrigins;
    private final Set<String> allowedAppSchemes;

    public OAuthRedirectUriValidator(
            @Value("${app.cors.allowed-origins}") String corsOrigins,
            @Value("${auth.oauth2.redirect.allowed-app-schemes:twohands}") String appSchemes
    ) {
        this.allowedWebOrigins = parseOrigins(corsOrigins);
        this.allowedAppSchemes = Arrays.stream(appSchemes.split(","))
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .map(value -> value.toLowerCase(Locale.ROOT))
                .collect(Collectors.toSet());
    }

    public boolean isAllowed(String redirectUri, HttpServletRequest request) {
        if (redirectUri == null || redirectUri.isBlank()) {
            return false;
        }

        try {
            URI uri = URI.create(redirectUri.trim());
            String scheme = uri.getScheme();
            if (scheme == null) {
                return false;
            }

            if (allowedAppSchemes.contains(scheme.toLowerCase(Locale.ROOT))) {
                return isAllowedAppSchemeUri(uri);
            }

            if ("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme)) {
                if (isSameOriginMobileBridge(uri, request)) {
                    return isAllowedMobileBridge(uri);
                }
                return isAllowedWebCallback(uri);
            }

            return false;
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    public String defaultAppReturnUrl() {
        if (allowedAppSchemes.isEmpty()) {
            return "twohands://oauth/success";
        }
        String scheme = allowedAppSchemes.iterator().next();
        return scheme + "://oauth/success";
    }

    public String defaultAppFailureReturnUrl() {
        if (allowedAppSchemes.isEmpty()) {
            return "twohands://oauth/failure";
        }
        String scheme = allowedAppSchemes.iterator().next();
        return scheme + "://oauth/failure";
    }

    private boolean isAllowedMobileBridge(URI uri) {
        String appReturn = decodeQueryValue(
                UriComponentsBuilder.fromUri(uri).build().getQueryParams().getFirst("app_return")
        );
        if (appReturn == null || appReturn.isBlank()) {
            return true;
        }
        try {
            URI appReturnUri = URI.create(appReturn.trim());
            String scheme = appReturnUri.getScheme();
            return scheme != null
                    && allowedAppSchemes.contains(scheme.toLowerCase(Locale.ROOT))
                    && isAllowedAppSchemeUri(appReturnUri);
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    private boolean isSameOriginMobileBridge(URI uri, HttpServletRequest request) {
        if (!MOBILE_COMPLETE_PATH.equals(normalizePath(uri.getPath()))) {
            return false;
        }
        return sameAuthority(uri, request);
    }

    private boolean isAllowedWebCallback(URI uri) {
        String origin = uri.getScheme() + "://" + uri.getAuthority();
        if (!allowedWebOrigins.contains(origin)) {
            return false;
        }
        return WEB_CALLBACK_PATHS.contains(normalizePath(uri.getPath()));
    }

    private boolean isAllowedAppSchemeUri(URI uri) {
        return isAllowedAppPath(toAppCallbackPath(uri));
    }

    private static String toAppCallbackPath(URI uri) {
        String host = uri.getHost();
        String path = uri.getPath();
        if (host != null && !host.isBlank()) {
            String suffix = path != null ? path : "";
            if (!suffix.startsWith("/")) {
                suffix = "/" + suffix;
            }
            return "/" + host + suffix;
        }
        return path;
    }

    private boolean isAllowedAppPath(String path) {
        return APP_CALLBACK_PATHS.contains(normalizePath(path));
    }

    private static boolean sameAuthority(URI uri, HttpServletRequest request) {
        int requestPort = request.getServerPort();
        int uriPort = uri.getPort();
        if (uriPort < 0) {
            uriPort = "https".equalsIgnoreCase(uri.getScheme()) ? 443 : 80;
        }
        if (requestPort < 0) {
            requestPort = "https".equalsIgnoreCase(request.getScheme()) ? 443 : 80;
        }

        return uri.getHost() != null
                && request.getServerName() != null
                && uri.getHost().equalsIgnoreCase(request.getServerName())
                && uriPort == requestPort
                && uri.getScheme() != null
                && request.getScheme() != null
                && uri.getScheme().equalsIgnoreCase(request.getScheme());
    }

    private static String normalizePath(String path) {
        if (path == null || path.isBlank()) {
            return "/";
        }
        return path.endsWith("/") && path.length() > 1 ? path.substring(0, path.length() - 1) : path;
    }

    private static String decodeQueryValue(String value) {
        if (value == null || value.isBlank()) {
            return value;
        }
        return URLDecoder.decode(value.trim(), StandardCharsets.UTF_8);
    }

    private static List<String> parseOrigins(String origins) {
        return Arrays.stream(origins.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isEmpty())
                .toList();
    }
}
