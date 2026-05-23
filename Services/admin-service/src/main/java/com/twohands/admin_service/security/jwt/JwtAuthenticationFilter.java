package com.twohands.admin_service.security.jwt;

import com.twohands.admin_service.security.AuthenticatedUser;
import com.twohands.admin_service.security.BearerTokenDetails;
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

	public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
		this.jwtTokenProvider = jwtTokenProvider;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
		if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
			String token = authHeader.substring(BEARER_PREFIX.length()).trim();
			if (!token.isEmpty()
					&& jwtTokenProvider.isValid(token)
					&& SecurityContextHolder.getContext().getAuthentication() == null) {
				UUID userId = jwtTokenProvider.getUserId(token);
				if (userId != null) {
					List<String> roles = jwtTokenProvider.getRoles(token);
					List<String> permissions = jwtTokenProvider.getPermissions(token);
					var authentication = new UsernamePasswordAuthenticationToken(
							new AuthenticatedUser(userId, roles, permissions),
							null,
							List.of()
					);
					authentication.setDetails(new BearerTokenDetails(token));
					SecurityContextHolder.getContext().setAuthentication(authentication);
				}
			}
		}
		filterChain.doFilter(request, response);
	}
}
