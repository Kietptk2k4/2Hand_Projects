package com.twohands.admin_service.delivery.http.config;

import com.twohands.admin_service.application.config.listsystemconfigs.ListSystemConfigsQuery;
import com.twohands.admin_service.application.config.listsystemconfigs.ListSystemConfigsResult;
import com.twohands.admin_service.application.config.listsystemconfigs.ListSystemConfigsUseCase;
import com.twohands.admin_service.application.config.listsystemconfigs.SystemConfigListItem;
import com.twohands.admin_service.application.config.createsystemconfig.CreateSystemConfigCommand;
import com.twohands.admin_service.application.config.createsystemconfig.CreateSystemConfigResult;
import com.twohands.admin_service.application.config.createsystemconfig.CreateSystemConfigUseCase;
import com.twohands.admin_service.application.config.togglesystemconfig.ToggleSystemConfigCommand;
import com.twohands.admin_service.application.config.togglesystemconfig.ToggleSystemConfigResult;
import com.twohands.admin_service.application.config.togglesystemconfig.ToggleSystemConfigUseCase;
import com.twohands.admin_service.application.config.updatesystemconfig.UpdateSystemConfigCommand;
import com.twohands.admin_service.application.config.updatesystemconfig.UpdateSystemConfigResult;
import com.twohands.admin_service.application.config.updatesystemconfig.UpdateSystemConfigUseCase;
import com.twohands.admin_service.application.config.viewhistory.ViewSystemConfigHistoryQuery;
import com.twohands.admin_service.application.config.viewhistory.ViewSystemConfigHistoryResult;
import com.twohands.admin_service.application.config.viewhistory.ViewSystemConfigHistoryUseCase;
import com.twohands.admin_service.common.dto.ApiResponse;
import com.twohands.admin_service.constant.AdminPermission;
import com.twohands.admin_service.security.annotation.RequireAdminPermission;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/admin/api/v1/system-configs")
public class SystemConfigController {

	private final ListSystemConfigsUseCase listSystemConfigsUseCase;
	private final CreateSystemConfigUseCase createSystemConfigUseCase;
	private final UpdateSystemConfigUseCase updateSystemConfigUseCase;
	private final ToggleSystemConfigUseCase toggleSystemConfigUseCase;
	private final ViewSystemConfigHistoryUseCase viewSystemConfigHistoryUseCase;

	public SystemConfigController(
			ListSystemConfigsUseCase listSystemConfigsUseCase,
			CreateSystemConfigUseCase createSystemConfigUseCase,
			UpdateSystemConfigUseCase updateSystemConfigUseCase,
			ToggleSystemConfigUseCase toggleSystemConfigUseCase,
			ViewSystemConfigHistoryUseCase viewSystemConfigHistoryUseCase
	) {
		this.listSystemConfigsUseCase = listSystemConfigsUseCase;
		this.createSystemConfigUseCase = createSystemConfigUseCase;
		this.updateSystemConfigUseCase = updateSystemConfigUseCase;
		this.toggleSystemConfigUseCase = toggleSystemConfigUseCase;
		this.viewSystemConfigHistoryUseCase = viewSystemConfigHistoryUseCase;
	}

	@GetMapping
	@RequireAdminPermission(AdminPermission.SYSTEM_CONFIG_VIEW)
	public ResponseEntity<ApiResponse<ListSystemConfigsResponse>> list(
			@RequestParam(required = false) String q,
			@RequestParam(name = "value_type", required = false) String valueType,
			@RequestParam(name = "is_active", required = false) Boolean active,
			@RequestParam(required = false) Integer page,
			@RequestParam(required = false) Integer size
	) {
		ListSystemConfigsResult result = listSystemConfigsUseCase.execute(new ListSystemConfigsQuery(
				q,
				valueType,
				active,
				page,
				size
		));

		return ResponseEntity.ok(ApiResponse.success(
				HttpStatus.OK.value(),
				listSystemConfigsUseCase.successMessage(),
				toListResponse(result)
		));
	}

	@PostMapping
	@RequireAdminPermission(AdminPermission.SYSTEM_CONFIG_UPDATE)
	public ResponseEntity<ApiResponse<CreateSystemConfigResponse>> create(
			@Valid @RequestBody CreateSystemConfigRequest request
	) {
		CreateSystemConfigResult result = createSystemConfigUseCase.execute(new CreateSystemConfigCommand(
				request.configKey(),
				request.configValue(),
				request.valueType(),
				request.description(),
				request.active(),
				request.reason()
		));

		CreateSystemConfigResponse data = new CreateSystemConfigResponse(
				result.configId(),
				result.configKey(),
				result.configValue(),
				result.valueType().name(),
				result.description(),
				result.active(),
				result.createdBy(),
				result.createdAt(),
				result.historyId(),
				result.outboxEventId()
		);

		return ResponseEntity.status(HttpStatus.CREATED)
				.body(ApiResponse.success(HttpStatus.CREATED.value(), createSystemConfigUseCase.successMessage(), data));
	}

	@PatchMapping("/{configId}")
	@RequireAdminPermission(AdminPermission.SYSTEM_CONFIG_UPDATE)
	public ResponseEntity<ApiResponse<UpdateSystemConfigResponse>> update(
			@PathVariable UUID configId,
			@Valid @RequestBody UpdateSystemConfigRequest request
	) {
		UpdateSystemConfigResult result = updateSystemConfigUseCase.execute(new UpdateSystemConfigCommand(
				configId,
				request.configValue(),
				request.description(),
				request.reason()
		));

		UpdateSystemConfigResponse data = new UpdateSystemConfigResponse(
				result.configId(),
				result.configKey(),
				result.configValue(),
				result.valueType().name(),
				result.description(),
				result.active(),
				result.updatedBy(),
				result.updatedAt(),
				result.historyId(),
				result.outboxEventId()
		);

		return ResponseEntity.ok(ApiResponse.success(
				HttpStatus.OK.value(),
				updateSystemConfigUseCase.successMessage(),
				data
		));
	}

	@PatchMapping("/{configId}/toggle")
	@RequireAdminPermission(AdminPermission.SYSTEM_CONFIG_UPDATE)
	public ResponseEntity<ApiResponse<ToggleSystemConfigResponse>> toggle(
			@PathVariable UUID configId,
			@Valid @RequestBody ToggleSystemConfigRequest request
	) {
		ToggleSystemConfigResult result = toggleSystemConfigUseCase.execute(new ToggleSystemConfigCommand(
				configId,
				request.active(),
				request.reason()
		));

		ToggleSystemConfigResponse data = new ToggleSystemConfigResponse(
				result.configId(),
				result.configKey(),
				result.configValue(),
				result.valueType().name(),
				result.description(),
				result.active(),
				result.updatedBy(),
				result.updatedAt(),
				result.historyId(),
				result.outboxEventId(),
				result.stateChanged()
		);

		return ResponseEntity.ok(ApiResponse.success(
				HttpStatus.OK.value(),
				toggleSystemConfigUseCase.successMessage(result.stateChanged()),
				data
		));
	}

	@GetMapping("/{configId}/history")
	@RequireAdminPermission(AdminPermission.SYSTEM_CONFIG_VIEW)
	public ResponseEntity<ApiResponse<ViewSystemConfigHistoryResponse>> viewHistory(
			@PathVariable UUID configId,
			@RequestParam(required = false) Integer page,
			@RequestParam(required = false) Integer size
	) {
		ViewSystemConfigHistoryResult result = viewSystemConfigHistoryUseCase.execute(
				new ViewSystemConfigHistoryQuery(configId, page, size)
		);

		return ResponseEntity.ok(ApiResponse.success(
				HttpStatus.OK.value(),
				viewSystemConfigHistoryUseCase.successMessage(),
				toHistoryResponse(result)
		));
	}

	private ListSystemConfigsResponse toListResponse(ListSystemConfigsResult result) {
		return new ListSystemConfigsResponse(
				result.page(),
				result.size(),
				result.totalElements(),
				result.totalPages(),
				result.items().stream().map(this::toListEntry).toList()
		);
	}

	private SystemConfigListEntryResponse toListEntry(SystemConfigListItem item) {
		return new SystemConfigListEntryResponse(
				item.configId(),
				item.configKey(),
				item.configValue(),
				item.valueType().name(),
				item.description(),
				item.active(),
				item.createdBy(),
				item.createdAt(),
				item.updatedBy(),
				item.updatedAt()
		);
	}

	private ViewSystemConfigHistoryResponse toHistoryResponse(ViewSystemConfigHistoryResult result) {
		return new ViewSystemConfigHistoryResponse(
				result.configId(),
				result.configKey(),
				result.page(),
				result.size(),
				result.totalElements(),
				result.totalPages(),
				result.valuesMasked(),
				result.history().stream()
						.map(item -> new SystemConfigHistoryEntryResponse(
								item.historyId(),
								item.configKey(),
								item.oldValue(),
								item.newValue(),
								item.changedBy(),
								item.reason(),
								item.createdAt(),
								item.valuesMasked()
						))
						.toList()
		);
	}
}
