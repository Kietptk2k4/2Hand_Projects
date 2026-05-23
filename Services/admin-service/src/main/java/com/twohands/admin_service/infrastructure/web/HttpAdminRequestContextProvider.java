package com.twohands.admin_service.infrastructure.web;

import com.twohands.admin_service.domain.audit.AdminRequestContextProvider;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
public class HttpAdminRequestContextProvider implements AdminRequestContextProvider {

	private static final String UNKNOWN = "unknown";

	@Override
	public String clientIpAddress() {
		HttpServletRequest request = currentRequest();
		if (request == null) {
			return null;
		}
		String forwarded = request.getHeader("X-Forwarded-For");
		if (forwarded != null && !forwarded.isBlank()) {
			return forwarded.split(",")[0].trim();
		}
		String realIp = request.getHeader("X-Real-IP");
		if (realIp != null && !realIp.isBlank()) {
			return realIp.trim();
		}
		return request.getRemoteAddr();
	}

	@Override
	public String userAgent() {
		HttpServletRequest request = currentRequest();
		if (request == null) {
			return null;
		}
		String userAgent = request.getHeader("User-Agent");
		return userAgent == null || userAgent.isBlank() ? UNKNOWN : userAgent;
	}

	private HttpServletRequest currentRequest() {
		var attributes = RequestContextHolder.getRequestAttributes();
		if (attributes instanceof ServletRequestAttributes servletAttributes) {
			return servletAttributes.getRequest();
		}
		return null;
	}
}
