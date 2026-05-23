package com.twohands.admin_service.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.admin_service.common.dto.ApiResponse;
import com.twohands.admin_service.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class RestAccessDeniedHandler implements AccessDeniedHandler {

	private final ObjectMapper objectMapper;

	public RestAccessDeniedHandler(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	@Override
	public void handle(
			HttpServletRequest request,
			HttpServletResponse response,
			AccessDeniedException accessDeniedException
	) throws IOException {
		response.setStatus(HttpServletResponse.SC_FORBIDDEN);
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		ApiResponse<Void> body = ApiResponse.error(
				ErrorCode.FORBIDDEN.status().value(),
				ErrorCode.FORBIDDEN.defaultMessage(),
				null
		);
		objectMapper.writeValue(response.getOutputStream(), body);
	}
}
