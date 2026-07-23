package com.twohands.admin_service.delivery.http.support;

import com.twohands.admin_service.application.support.exportwebhooklogs.ExportWebhookLogsForSupportUseCase;
import com.twohands.admin_service.application.support.viewwebhooklogdetail.ViewWebhookLogDetailForSupportUseCase;
import com.twohands.admin_service.application.support.viewwebhooklogs.ViewWebhookLogsForSupportQuery;
import com.twohands.admin_service.application.support.viewwebhooklogs.ViewWebhookLogsForSupportResult;
import com.twohands.admin_service.application.support.viewwebhooklogs.ViewWebhookLogsForSupportStatsQuery;
import com.twohands.admin_service.application.support.viewwebhooklogs.ViewWebhookLogsForSupportStatsUseCase;
import com.twohands.admin_service.application.support.viewwebhooklogs.ViewWebhookLogsForSupportUseCase;
import com.twohands.admin_service.common.dto.ApiResponse;
import com.twohands.admin_service.constant.AdminPermission;
import com.twohands.admin_service.domain.support.WebhookSupportLogEntry;
import com.twohands.admin_service.domain.support.WebhookSupportLogStats;
import com.twohands.admin_service.security.annotation.RequireAdminPermission;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@RestController
@RequestMapping("/admin/api/v1/support/webhook-logs")
public class WebhookSupportController {

	private final ViewWebhookLogsForSupportUseCase viewWebhookLogsForSupportUseCase;
	private final ViewWebhookLogsForSupportStatsUseCase viewWebhookLogsForSupportStatsUseCase;
	private final ViewWebhookLogDetailForSupportUseCase viewWebhookLogDetailForSupportUseCase;
	private final ExportWebhookLogsForSupportUseCase exportWebhookLogsForSupportUseCase;

	public WebhookSupportController(
			ViewWebhookLogsForSupportUseCase viewWebhookLogsForSupportUseCase,
			ViewWebhookLogsForSupportStatsUseCase viewWebhookLogsForSupportStatsUseCase,
			ViewWebhookLogDetailForSupportUseCase viewWebhookLogDetailForSupportUseCase,
			ExportWebhookLogsForSupportUseCase exportWebhookLogsForSupportUseCase
	) {
		this.viewWebhookLogsForSupportUseCase = viewWebhookLogsForSupportUseCase;
		this.viewWebhookLogsForSupportStatsUseCase = viewWebhookLogsForSupportStatsUseCase;
		this.viewWebhookLogDetailForSupportUseCase = viewWebhookLogDetailForSupportUseCase;
		this.exportWebhookLogsForSupportUseCase = exportWebhookLogsForSupportUseCase;
	}

	@GetMapping
	@RequireAdminPermission(AdminPermission.WEBHOOK_SUPPORT_READ)
	public ResponseEntity<ApiResponse<ViewWebhookLogsForSupportResponse>> listWebhookLogs(
			@RequestParam(required = false) String provider,
			@RequestParam(name = "reference_id", required = false) String referenceId,
			@RequestParam(required = false) String q,
			@RequestParam(name = "event_type", required = false) String eventType,
			@RequestParam(required = false) String status,
			@RequestParam(required = false) String from,
			@RequestParam(required = false) String to,
			@RequestParam(required = false) Integer page,
			@RequestParam(required = false) Integer size,
			HttpServletRequest httpServletRequest
	) {
		ViewWebhookLogsForSupportResult result = viewWebhookLogsForSupportUseCase.execute(
				buildListQuery(provider, referenceId, q, eventType, status, from, to, page, size, httpServletRequest)
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

	@GetMapping("/stats")
	@RequireAdminPermission(AdminPermission.WEBHOOK_SUPPORT_READ)
	public ResponseEntity<ApiResponse<ViewWebhookLogsForSupportStatsResponse>> webhookLogStats(
			@RequestParam(required = false) String provider,
			@RequestParam(name = "reference_id", required = false) String referenceId,
			@RequestParam(required = false) String q,
			@RequestParam(name = "event_type", required = false) String eventType,
			@RequestParam(required = false) String status,
			@RequestParam(required = false) String from,
			@RequestParam(required = false) String to,
			HttpServletRequest httpServletRequest
	) {
		WebhookSupportLogStats stats = viewWebhookLogsForSupportStatsUseCase.execute(
				new ViewWebhookLogsForSupportStatsQuery(
						provider,
						referenceId,
						q,
						eventType,
						status,
						from,
						to,
						resolveBearerToken(httpServletRequest)
				)
		);

		return ResponseEntity.ok(ApiResponse.success(
				HttpStatus.OK.value(),
				viewWebhookLogsForSupportStatsUseCase.successMessage(),
				ViewWebhookLogsForSupportStatsResponse.from(stats)
		));
	}

	@GetMapping("/export")
	@RequireAdminPermission(AdminPermission.WEBHOOK_SUPPORT_READ)
	public ResponseEntity<byte[]> exportWebhookLogs(
			@RequestParam(required = false) String provider,
			@RequestParam(name = "reference_id", required = false) String referenceId,
			@RequestParam(required = false) String q,
			@RequestParam(name = "event_type", required = false) String eventType,
			@RequestParam(required = false) String status,
			@RequestParam(required = false) String from,
			@RequestParam(required = false) String to,
			@RequestParam(defaultValue = "csv") String format,
			HttpServletRequest httpServletRequest
	) {
		if (!"csv".equalsIgnoreCase(format)) {
			return ResponseEntity.badRequest().build();
		}

		byte[] csv = exportWebhookLogsForSupportUseCase.execute(
				buildListQuery(provider, referenceId, q, eventType, status, from, to, null, null, httpServletRequest)
		);

		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"webhook-logs.csv\"")
				.contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
				.body(csv);
	}

	@GetMapping("/{logId}")
	@RequireAdminPermission(AdminPermission.WEBHOOK_SUPPORT_READ)
	public ResponseEntity<ApiResponse<ViewWebhookLogsForSupportResponse.WebhookLogEntryResponse>> getWebhookLog(
			@PathVariable UUID logId,
			@RequestParam String provider,
			HttpServletRequest httpServletRequest
	) {
		WebhookSupportLogEntry entry = viewWebhookLogDetailForSupportUseCase.execute(
				logId,
				provider,
				resolveBearerToken(httpServletRequest)
		);

		return ResponseEntity.ok(ApiResponse.success(
				HttpStatus.OK.value(),
				viewWebhookLogDetailForSupportUseCase.successMessage(),
				ViewWebhookLogsForSupportResponse.WebhookLogEntryResponse.from(entry)
		));
	}

	private ViewWebhookLogsForSupportQuery buildListQuery(
			String provider,
			String referenceId,
			String searchQuery,
			String eventType,
			String status,
			String from,
			String to,
			Integer page,
			Integer size,
			HttpServletRequest httpServletRequest
	) {
		return new ViewWebhookLogsForSupportQuery(
				provider,
				referenceId,
				searchQuery,
				eventType,
				status,
				from,
				to,
				page,
				size,
				resolveBearerToken(httpServletRequest)
		);
	}

	private String resolveBearerToken(HttpServletRequest request) {
		String authorization = request.getHeader("Authorization");
		if (authorization == null || !authorization.startsWith("Bearer ")) {
			return "";
		}
		return authorization.substring(7).trim();
	}
}
