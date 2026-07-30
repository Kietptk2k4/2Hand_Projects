package com.twohands.commerce_service.security.jwt;

import com.twohands.commerce_service.security.AuthenticatedUser;
import com.twohands.commerce_service.security.session.AccessTokenInvalidationChecker;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenProvider jwtTokenProvider;
    private final AccessTokenInvalidationChecker accessTokenInvalidationChecker;

    public JwtAuthenticationFilter(
            JwtTokenProvider jwtTokenProvider,
            AccessTokenInvalidationChecker accessTokenInvalidationChecker
    ) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.accessTokenInvalidationChecker = accessTokenInvalidationChecker;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
            String token = authHeader.substring(BEARER_PREFIX.length());
            if (jwtTokenProvider.isValid(token)
                    && SecurityContextHolder.getContext().getAuthentication() == null
                    && !isAccessTokenRevoked(token)) {
                UUID userId = jwtTokenProvider.getUserId(token);
                if (userId != null) {
                    List<String> roles = jwtTokenProvider.getRoles(token);
                    List<String> permissions = jwtTokenProvider.getPermissions(token);
                    var authentication = new UsernamePasswordAuthenticationToken(
                            new AuthenticatedUser(userId, roles, permissions),
                            null,
                            List.of()
                    );
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        }
        filterChain.doFilter(request, response);
    }

    private boolean isAccessTokenRevoked(String token) {
        UUID userId = jwtTokenProvider.getUserId(token);
        if (userId == null) {
            return false;
        }
        return accessTokenInvalidationChecker.isTokenInvalidated(
                userId,
                jwtTokenProvider.getIssuedAtEpochMilli(token)
        );
    }
}
