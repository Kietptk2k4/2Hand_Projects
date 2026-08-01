package com.twohands.auth_service.security.jwt;

import com.twohands.auth_service.domain.session.AccessTokenInvalidationStore;
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
import java.util.UUID;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenProvider jwtTokenProvider;
    private final AccessTokenInvalidationStore accessTokenInvalidationStore;

    public JwtAuthenticationFilter(
            JwtTokenProvider jwtTokenProvider,
            AccessTokenInvalidationStore accessTokenInvalidationStore
    ) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.accessTokenInvalidationStore = accessTokenInvalidationStore;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
            String token = authHeader.substring(BEARER_PREFIX.length()).trim();
            if (jwtTokenProvider.isValid(token) && !isAccessTokenRevoked(token)) {
                var authorities = jwtTokenProvider.getAuthorities(token);
                var authentication = new UsernamePasswordAuthenticationToken(
                        jwtTokenProvider.getSubject(token),
                        null,
                        authorities
                );
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean isAccessTokenRevoked(String token) {
        UUID userId = jwtTokenProvider.getUserId(token);
        if (userId == null) {
            return false;
        }
        return accessTokenInvalidationStore.isTokenInvalidated(
                userId,
                jwtTokenProvider.getIssuedAtEpochMilli(token)
        );
    }
}
