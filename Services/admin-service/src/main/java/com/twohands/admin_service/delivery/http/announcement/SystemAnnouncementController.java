package com.twohands.admin_service.delivery.http.announcement;

import com.twohands.admin_service.application.announcement.getsystemannouncement.GetSystemAnnouncementQuery;
import com.twohands.admin_service.application.announcement.getsystemannouncement.GetSystemAnnouncementResult;
import com.twohands.admin_service.application.announcement.getsystemannouncement.GetSystemAnnouncementUseCase;
import com.twohands.admin_service.application.announcement.listsystemannouncements.ListSystemAnnouncementsQuery;
import com.twohands.admin_service.application.announcement.listsystemannouncements.ListSystemAnnouncementsResult;
import com.twohands.admin_service.application.announcement.listsystemannouncements.ListSystemAnnouncementsUseCase;
import com.twohands.admin_service.application.announcement.listsystemannouncements.SystemAnnouncementListItem;
import com.twohands.admin_service.application.announcement.updatesystemannouncement.UpdateSystemAnnouncementCommand;
import com.twohands.admin_service.application.announcement.updatesystemannouncement.UpdateSystemAnnouncementResult;
import com.twohands.admin_service.application.announcement.updatesystemannouncement.UpdateSystemAnnouncementUseCase;
import com.twohands.admin_service.application.announcement.dismisssystemannouncement.DismissSystemAnnouncementCommand;
import com.twohands.admin_service.application.announcement.dismisssystemannouncement.DismissSystemAnnouncementResult;
import com.twohands.admin_service.application.announcement.dismisssystemannouncement.DismissSystemAnnouncementUseCase;
import com.twohands.admin_service.application.announcement.cancelsystemannouncement.CancelSystemAnnouncementCommand;
import com.twohands.admin_service.application.announcement.cancelsystemannouncement.CancelSystemAnnouncementResult;
import com.twohands.admin_service.application.announcement.cancelsystemannouncement.CancelSystemAnnouncementUseCase;
import com.twohands.admin_service.application.announcement.createsystemannouncement.CreateSystemAnnouncementCommand;
import com.twohands.admin_service.application.announcement.createsystemannouncement.CreateSystemAnnouncementResult;
import com.twohands.admin_service.application.announcement.createsystemannouncement.CreateSystemAnnouncementUseCase;
import com.twohands.admin_service.application.announcement.pinsystemannouncement.PinSystemAnnouncementCommand;
import com.twohands.admin_service.application.announcement.pinsystemannouncement.PinSystemAnnouncementResult;
import com.twohands.admin_service.application.announcement.pinsystemannouncement.PinSystemAnnouncementUseCase;
import com.twohands.admin_service.application.announcement.publishsystemannouncement.PublishSystemAnnouncementCommand;
import com.twohands.admin_service.application.announcement.publishsystemannouncement.PublishSystemAnnouncementResult;
import com.twohands.admin_service.application.announcement.publishsystemannouncement.PublishSystemAnnouncementUseCase;
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

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/admin/api/v1/system-announcements")
public class SystemAnnouncementController {

	private final ListSystemAnnouncementsUseCase listSystemAnnouncementsUseCase;
	private final GetSystemAnnouncementUseCase getSystemAnnouncementUseCase;
	private final CreateSystemAnnouncementUseCase createSystemAnnouncementUseCase;
	private final UpdateSystemAnnouncementUseCase updateSystemAnnouncementUseCase;
	private final PublishSystemAnnouncementUseCase publishSystemAnnouncementUseCase;
	private final PinSystemAnnouncementUseCase pinSystemAnnouncementUseCase;
	private final CancelSystemAnnouncementUseCase cancelSystemAnnouncementUseCase;
	private final DismissSystemAnnouncementUseCase dismissSystemAnnouncementUseCase;

	public SystemAnnouncementController(
			ListSystemAnnouncementsUseCase listSystemAnnouncementsUseCase,
			GetSystemAnnouncementUseCase getSystemAnnouncementUseCase,
			CreateSystemAnnouncementUseCase createSystemAnnouncementUseCase,
			UpdateSystemAnnouncementUseCase updateSystemAnnouncementUseCase,
			PublishSystemAnnouncementUseCase publishSystemAnnouncementUseCase,
			PinSystemAnnouncementUseCase pinSystemAnnouncementUseCase,
			CancelSystemAnnouncementUseCase cancelSystemAnnouncementUseCase,
			DismissSystemAnnouncementUseCase dismissSystemAnnouncementUseCase
	) {
		this.listSystemAnnouncementsUseCase = listSystemAnnouncementsUseCase;
		this.getSystemAnnouncementUseCase = getSystemAnnouncementUseCase;
		this.createSystemAnnouncementUseCase = createSystemAnnouncementUseCase;
		this.updateSystemAnnouncementUseCase = updateSystemAnnouncementUseCase;
		this.publishSystemAnnouncementUseCase = publishSystemAnnouncementUseCase;
		this.pinSystemAnnouncementUseCase = pinSystemAnnouncementUseCase;
		this.cancelSystemAnnouncementUseCase = cancelSystemAnnouncementUseCase;
		this.dismissSystemAnnouncementUseCase = dismissSystemAnnouncementUseCase;
	}

	@GetMapping
	@RequireAdminPermission({
			AdminPermission.SYSTEM_ANNOUNCEMENT_CREATE,
			AdminPermission.SYSTEM_ANNOUNCEMENT_UPDATE,
			AdminPermission.SYSTEM_ANNOUNCEMENT_PUBLISH,
			AdminPermission.SYSTEM_ANNOUNCEMENT_CANCEL
	})
	public ResponseEntity<ApiResponse<ListSystemAnnouncementsResponse>> list(
			@RequestParam(required = false) String q,
			@RequestParam(required = false) String status,
			@RequestParam(required = false) String severity,
			@RequestParam(required = false) Integer page,
			@RequestParam(required = false) Integer size
	) {
		ListSystemAnnouncementsResult result = listSystemAnnouncementsUseCase.execute(
				new ListSystemAnnouncementsQuery(q, status, severity, page, size)
		);

		return ResponseEntity.ok(ApiResponse.success(
				HttpStatus.OK.value(),
				listSystemAnnouncementsUseCase.successMessage(),
				toListResponse(result)
		));
	}

	@GetMapping("/{announcementId}")
	@RequireAdminPermission({
			AdminPermission.SYSTEM_ANNOUNCEMENT_CREATE,
			AdminPermission.SYSTEM_ANNOUNCEMENT_UPDATE,
			AdminPermission.SYSTEM_ANNOUNCEMENT_PUBLISH,
			AdminPermission.SYSTEM_ANNOUNCEMENT_CANCEL
	})
	public ResponseEntity<ApiResponse<ViewSystemAnnouncementDetailResponse>> getDetail(
			@PathVariable UUID announcementId
	) {
		GetSystemAnnouncementResult result = getSystemAnnouncementUseCase.execute(
				new GetSystemAnnouncementQuery(announcementId)
		);

		return ResponseEntity.ok(ApiResponse.success(
				HttpStatus.OK.value(),
				getSystemAnnouncementUseCase.successMessage(),
				toDetailResponse(result)
		));
	}

	@PostMapping
	@RequireAdminPermission(AdminPermission.SYSTEM_ANNOUNCEMENT_CREATE)
	public ResponseEntity<ApiResponse<CreateSystemAnnouncementResponse>> create(
			@Valid @RequestBody CreateSystemAnnouncementRequest request
	) {
		CreateSystemAnnouncementResult result = createSystemAnnouncementUseCase.execute(
				new CreateSystemAnnouncementCommand(
						request.title(),
						request.content(),
						request.severity(),
						request.pinned(),
						request.dismissible()
				)
		);

		CreateSystemAnnouncementResponse data = new CreateSystemAnnouncementResponse(
				result.announcementId(),
				result.title(),
				result.content(),
				result.severity().name(),
				result.pinned(),
				result.dismissible(),
				result.status().name(),
				result.createdBy(),
				result.createdAt()
		);

		return ResponseEntity.status(HttpStatus.CREATED)
				.body(ApiResponse.success(
						HttpStatus.CREATED.value(),
						createSystemAnnouncementUseCase.successMessage(),
						data
				));
	}

	@PatchMapping("/{announcementId}")
	@RequireAdminPermission(AdminPermission.SYSTEM_ANNOUNCEMENT_UPDATE)
	public ResponseEntity<ApiResponse<ViewSystemAnnouncementDetailResponse>> update(
			@PathVariable UUID announcementId,
			@Valid @RequestBody UpdateSystemAnnouncementRequest request
	) {
		UpdateSystemAnnouncementResult result = updateSystemAnnouncementUseCase.execute(
				new UpdateSystemAnnouncementCommand(
						announcementId,
						request.title(),
						request.content(),
						request.severity(),
						request.pinned(),
						request.dismissible()
				)
		);

		return ResponseEntity.ok(ApiResponse.success(
				HttpStatus.OK.value(),
				updateSystemAnnouncementUseCase.successMessage(),
				toDetailResponse(result)
		));
	}

	@PostMapping("/{announcementId}/publish")
	@RequireAdminPermission(AdminPermission.SYSTEM_ANNOUNCEMENT_PUBLISH)
	public ResponseEntity<ApiResponse<PublishSystemAnnouncementResponse>> publish(
			@PathVariable UUID announcementId,
			@RequestBody(required = false) PublishSystemAnnouncementRequest request
	) {
		List<UUID> recipientUserIds = request == null || request.recipientUserIds() == null
				? List.of()
				: request.recipientUserIds();
		String targetAudience = request == null ? null : request.targetAudience();
		PublishSystemAnnouncementResult result = publishSystemAnnouncementUseCase.execute(
				new PublishSystemAnnouncementCommand(announcementId, recipientUserIds, targetAudience)
		);

		PublishSystemAnnouncementResponse data = new PublishSystemAnnouncementResponse(
				result.announcementId(),
				result.title(),
				result.severity().name(),
				result.status().name(),
				result.pinned(),
				result.dismissible(),
				result.sentAt(),
				result.outboxEventId()
		);

		return ResponseEntity.ok(ApiResponse.success(
				HttpStatus.OK.value(),
				publishSystemAnnouncementUseCase.successMessage(),
				data
		));
	}

	@PatchMapping("/{announcementId}/pin")
	@RequireAdminPermission(AdminPermission.SYSTEM_ANNOUNCEMENT_UPDATE)
	public ResponseEntity<ApiResponse<PinSystemAnnouncementResponse>> pin(
			@PathVariable UUID announcementId,
			@Valid @RequestBody PinSystemAnnouncementRequest request
	) {
		PinSystemAnnouncementResult result = pinSystemAnnouncementUseCase.execute(
				new PinSystemAnnouncementCommand(announcementId, request.pinned())
		);

		PinSystemAnnouncementResponse data = new PinSystemAnnouncementResponse(
				result.announcementId(),
				result.title(),
				result.status().name(),
				result.pinned(),
				result.stateChanged()
		);

		String message = result.stateChanged()
				? pinSystemAnnouncementUseCase.successMessage()
				: pinSystemAnnouncementUseCase.idempotentMessage();

		return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), message, data));
	}

	@PostMapping("/{announcementId}/cancel")
	@RequireAdminPermission(AdminPermission.SYSTEM_ANNOUNCEMENT_CANCEL)
	public ResponseEntity<ApiResponse<CancelSystemAnnouncementResponse>> cancel(
			@PathVariable UUID announcementId
	) {
		CancelSystemAnnouncementResult result = cancelSystemAnnouncementUseCase.execute(
				new CancelSystemAnnouncementCommand(announcementId)
		);

		CancelSystemAnnouncementResponse data = new CancelSystemAnnouncementResponse(
				result.announcementId(),
				result.title(),
				result.status().name(),
				result.stateChanged(),
				result.outboxEventId()
		);

		String message = result.stateChanged()
				? cancelSystemAnnouncementUseCase.successMessage()
				: cancelSystemAnnouncementUseCase.idempotentMessage();

		return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), message, data));
	}

	private ListSystemAnnouncementsResponse toListResponse(ListSystemAnnouncementsResult result) {
		return new ListSystemAnnouncementsResponse(
				result.page(),
				result.size(),
				result.totalElements(),
				result.totalPages(),
				result.items().stream().map(this::toListEntry).toList()
		);
	}

	private SystemAnnouncementListEntryResponse toListEntry(SystemAnnouncementListItem item) {
		return new SystemAnnouncementListEntryResponse(
				item.announcementId(),
				item.title(),
				item.content(),
				item.severity().name(),
				item.status().name(),
				item.pinned(),
				item.dismissible(),
				item.createdBy(),
				item.createdAt(),
				item.sentAt()
		);
	}

	private ViewSystemAnnouncementDetailResponse toDetailResponse(GetSystemAnnouncementResult result) {
		return new ViewSystemAnnouncementDetailResponse(
				result.announcementId(),
				result.title(),
				result.content(),
				result.severity().name(),
				result.status().name(),
				result.pinned(),
				result.dismissible(),
				result.createdBy(),
				result.createdAt(),
				result.sentAt()
		);
	}

	private ViewSystemAnnouncementDetailResponse toDetailResponse(UpdateSystemAnnouncementResult result) {
		return new ViewSystemAnnouncementDetailResponse(
				result.announcementId(),
				result.title(),
				result.content(),
				result.severity().name(),
				result.status().name(),
				result.pinned(),
				result.dismissible(),
				result.createdBy(),
				result.createdAt(),
				result.sentAt()
		);
	}

	@PostMapping("/{announcementId}/dismiss")
	public ResponseEntity<ApiResponse<DismissSystemAnnouncementResponse>> dismiss(
			@PathVariable UUID announcementId
	) {
		DismissSystemAnnouncementResult result = dismissSystemAnnouncementUseCase.execute(
				new DismissSystemAnnouncementCommand(announcementId)
		);

		DismissSystemAnnouncementResponse data = new DismissSystemAnnouncementResponse(
				result.announcementId(),
				result.title(),
				result.status().name(),
				result.dismissible(),
				result.clientSidePersistence()
		);

		return ResponseEntity.ok(ApiResponse.success(
				HttpStatus.OK.value(),
				dismissSystemAnnouncementUseCase.successMessage(),
				data
		));
	}
}
