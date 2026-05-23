package com.twohands.admin_service.application.audit;

import com.twohands.admin_service.domain.auth.AdminAuthorizationService;
import com.twohands.admin_service.exception.AppException;
import com.twohands.admin_service.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
public class AdminAccessDeniedAuditAspect {

	private static final Logger log = LoggerFactory.getLogger(AdminAccessDeniedAuditAspect.class);

	private final AdminActionAuditLogger adminActionAuditLogger;
	private final AdminAuthorizationService adminAuthorizationService;

	public AdminAccessDeniedAuditAspect(
			AdminActionAuditLogger adminActionAuditLogger,
			AdminAuthorizationService adminAuthorizationService
	) {
		this.adminActionAuditLogger = adminActionAuditLogger;
		this.adminAuthorizationService = adminAuthorizationService;
	}

	@AfterThrowing(
			pointcut = "within(com.twohands.admin_service.delivery..*) && @within(org.springframework.web.bind.annotation.RestController)",
			throwing = "ex"
	)
	public void logForbiddenAccess(AppException ex) {
		if (ex.getErrorCode() != ErrorCode.FORBIDDEN) {
			return;
		}
		try {
			var adminId = adminAuthorizationService.requireCurrentAdminId();
			String endpoint = resolveEndpoint();
			String required = extractRequiredDetail(ex.getMessage());
			adminActionAuditLogger.logAccessDenied(adminId, required, endpoint, ex.getMessage());
		} catch (Exception auditEx) {
			log.warn("Failed to write admin access denied audit log: {}", auditEx.getMessage());
		}
	}

	private String extractRequiredDetail(String message) {
		if (message == null) {
			return "UNKNOWN";
		}
		if (message.startsWith("Missing permission: ")) {
			return message.substring("Missing permission: ".length());
		}
		return message;
	}

	private String resolveEndpoint() {
		var attributes = RequestContextHolder.getRequestAttributes();
		if (attributes instanceof ServletRequestAttributes servletAttributes) {
			HttpServletRequest request = servletAttributes.getRequest();
			return request.getMethod() + " " + request.getRequestURI();
		}
		return "";
	}
}
