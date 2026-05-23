package com.twohands.admin_service.delivery.http.audit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.admin_service.application.audit.viewlogs.AdminActionLogItem;
import com.twohands.admin_service.application.audit.viewlogs.ViewAdminActionLogDetailQuery;
import com.twohands.admin_service.application.audit.viewlogs.ViewAdminActionLogDetailUseCase;
import com.twohands.admin_service.application.audit.viewlogs.ViewAdminActionLogsQuery;
import com.twohands.admin_service.application.audit.viewlogs.ViewAdminActionLogsResult;
import com.twohands.admin_service.application.audit.viewlogs.ViewAdminActionLogsUseCase;
import com.twohands.admin_service.common.dto.ApiResponse;
import com.twohands.admin_service.constant.AdminPermission;
import com.twohands.admin_service.security.annotation.RequireAdminPermission;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/admin/api/v1/admin-action-logs")
public class AdminActionLogController {

	private final ViewAdminActionLogsUseCase viewAdminActionLogsUseCase;
	private final ViewAdminActionLogDetailUseCase viewAdminActionLogDetailUseCase;
	private final ObjectMapper objectMapper;

	public AdminActionLogController(
			ViewAdminActionLogsUseCase viewAdminActionLogsUseCase,
			ViewAdminActionLogDetailUseCase viewAdminActionLogDetailUseCase,
			ObjectMapper objectMapper
	) {
		this.viewAdminActionLogsUseCase = viewAdminActionLogsUseCase;
		this.viewAdminActionLogDetailUseCase = viewAdminActionLogDetailUseCase;
		this.objectMapper = objectMapper;
	}

	@GetMapping
	@RequireAdminPermission(AdminPermission.ADMIN_AUDIT_VIEW)
	public ResponseEntity<ApiResponse<ViewAdminActionLogsResponse>> list(
			@RequestParam(name = "admin_id", required = false) UUID adminId,
			@RequestParam(required = false) String action,
			@RequestParam(name = "target_type", required = false) String targetType,
			@RequestParam(name = "target_id", required = false) String targetId,
			@RequestParam(required = false) String status,
			@RequestParam(required = false) String from,
			@RequestParam(required = false) String to,
			@RequestParam(required = false) Integer page,
			@RequestParam(required = false) Integer size
	) {
		ViewAdminActionLogsResult result = viewAdminActionLogsUseCase.execute(new ViewAdminActionLogsQuery(
				adminId,
				action,
				targetType,
				targetId,
				status,
				from,
				to,
				page,
				size
		));

		ViewAdminActionLogsResponse data = new ViewAdminActionLogsResponse(
				result.page(),
				result.size(),
				result.totalElements(),
				result.totalPages(),
				result.logs().stream().map(this::toEntryResponse).toList()
		);

		return ResponseEntity.ok(ApiResponse.success(
				HttpStatus.OK.value(),
				viewAdminActionLogsUseCase.successMessage(),
				data
		));
	}

	@GetMapping("/{logId}")
	@RequireAdminPermission(AdminPermission.ADMIN_AUDIT_VIEW)
	public ResponseEntity<ApiResponse<AdminActionLogEntryResponse>> detail(@PathVariable UUID logId) {
		AdminActionLogItem item = viewAdminActionLogDetailUseCase.execute(new ViewAdminActionLogDetailQuery(logId));

		return ResponseEntity.ok(ApiResponse.success(
				HttpStatus.OK.value(),
				viewAdminActionLogDetailUseCase.successMessage(),
				toEntryResponse(item)
		));
	}

	private AdminActionLogEntryResponse toEntryResponse(AdminActionLogItem item) {
		return new AdminActionLogEntryResponse(
				item.logId(),
				item.adminId(),
				item.actionType(),
				item.targetType(),
				item.targetId(),
				item.status().name(),
				parseJson(item.requestPayload()),
				parseJson(item.responsePayload()),
				item.ipAddress(),
				item.userAgent(),
				item.createdAt()
		);
	}

	private JsonNode parseJson(String payload) {
		if (payload == null || payload.isBlank()) {
			return null;
		}
		try {
			return objectMapper.readTree(payload);
		} catch (JsonProcessingException ex) {
			return objectMapper.getNodeFactory().textNode(payload);
		}
	}
}
