package com.twohands.admin_service.delivery.http.support;

import com.twohands.admin_service.application.support.viewwebhooklogs.ViewWebhookLogsForSupportQuery;
import com.twohands.admin_service.application.support.viewwebhooklogs.ViewWebhookLogsForSupportResult;
import com.twohands.admin_service.application.support.viewwebhooklogs.ViewWebhookLogsForSupportUseCase;
import com.twohands.admin_service.common.dto.ApiResponse;
import com.twohands.admin_service.constant.AdminPermission;
import com.twohands.admin_service.security.annotation.RequireAdminPermission;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/api/v1/support/webhook-logs")
public class WebhookSupportController {

	private final ViewWebhookLogsForSupportUseCase viewWebhookLogsForSupportUseCase;

	public WebhookSupportController(ViewWebhookLogsForSupportUseCase viewWebhookLogsForSupportUseCase) {
		this.viewWebhookLogsForSupportUseCase = viewWebhookLogsForSupportUseCase;
	}

	@GetMapping
	@RequireAdminPermission(AdminPermission.WEBHOOK_SUPPORT_READ)
	public ResponseEntity<ApiResponse<ViewWebhookLogsForSupportResponse>> listWebhookLogs(
			@RequestParam(required = false) String provider,
			@RequestParam(name = "reference_id", required = false) String referenceId,
			@RequestParam(required = false) String status,
			@RequestParam(required = false) String from,
			@RequestParam(required = false) String to,
			@RequestParam(required = false) Integer page,
			@RequestParam(required = false) Integer size,
			HttpServletRequest httpServletRequest
	) {
		ViewWebhookLogsForSupportResult result = viewWebhookLogsForSupportUseCase.execute(
				new ViewWebhookLogsForSupportQuery(
						provider,
						referenceId,
						status,
						from,
						to,
						page,
						size,
						resolveBearerToken(httpServletRequest)
				)
		);

		ViewWebhookLogsForSupportResponse data = ViewWebhookLogsForSupportResponse.from(
				result.page(),
				result.size(),
				result.totalElements(),
				result.totalPages(),
				result.logs()
		);

		return ResponseEntity.ok(ApiResponse.success(
				HttpStatus.OK.value(),
				viewWebhookLogsForSupportUseCase.successMessage(),
				data
		));
	}

	private String resolveBearerToken(HttpServletRequest request) {
		String authorization = request.getHeader("Authorization");
		if (authorization == null || !authorization.startsWith("Bearer ")) {
			return "";
		}
		return authorization.substring(7).trim();
	}
}
