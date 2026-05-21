package com.twohands.notification_service.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.notification_service.common.dto.ApiResponse;
import com.twohands.notification_service.exception.ErrorCode;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class InternalApiKeyFilter extends OncePerRequestFilter {

    public static final String INTERNAL_API_KEY_HEADER = "X-Internal-Api-Key";
    private static final String INTERNAL_PATH_PREFIX = "/api/v1/notification/internal/";

    private final ObjectMapper objectMapper;
    private final boolean internalEnabled;
    private final String internalApiKey;

    public InternalApiKeyFilter(
            ObjectMapper objectMapper,
            @Value("${notification.ingest.internal-enabled:true}") boolean internalEnabled,
            @Value("${notification.ingest.internal-api-key:dev-internal-key}") String internalApiKey
    ) {
        this.objectMapper = objectMapper;
        this.internalEnabled = internalEnabled;
        this.internalApiKey = internalApiKey;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith(INTERNAL_PATH_PREFIX);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (!internalEnabled) {
            writeError(response, ErrorCode.INTERNAL_API_DISABLED);
            return;
        }

        String providedKey = request.getHeader(INTERNAL_API_KEY_HEADER);
        if (providedKey == null || !providedKey.equals(internalApiKey)) {
            writeError(response, ErrorCode.INVALID_INTERNAL_API_KEY);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void writeError(HttpServletResponse response, ErrorCode errorCode) throws IOException {
        response.setStatus(errorCode.status().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ApiResponse<Void> body = ApiResponse.error(
                errorCode.status().value(),
                errorCode.defaultMessage(),
                null
        );
        objectMapper.writeValue(response.getOutputStream(), body);
    }
}
