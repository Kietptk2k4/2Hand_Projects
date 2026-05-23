package com.twohands.admin_service.delivery.http.announcement;

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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/admin/api/v1/system-announcements")
public class SystemAnnouncementController {

	private final CreateSystemAnnouncementUseCase createSystemAnnouncementUseCase;
	private final PublishSystemAnnouncementUseCase publishSystemAnnouncementUseCase;
	private final PinSystemAnnouncementUseCase pinSystemAnnouncementUseCase;
	private final CancelSystemAnnouncementUseCase cancelSystemAnnouncementUseCase;
	private final DismissSystemAnnouncementUseCase dismissSystemAnnouncementUseCase;

	public SystemAnnouncementController(
			CreateSystemAnnouncementUseCase createSystemAnnouncementUseCase,
			PublishSystemAnnouncementUseCase publishSystemAnnouncementUseCase,
			PinSystemAnnouncementUseCase pinSystemAnnouncementUseCase,
			CancelSystemAnnouncementUseCase cancelSystemAnnouncementUseCase,
			DismissSystemAnnouncementUseCase dismissSystemAnnouncementUseCase
	) {
		this.createSystemAnnouncementUseCase = createSystemAnnouncementUseCase;
		this.publishSystemAnnouncementUseCase = publishSystemAnnouncementUseCase;
		this.pinSystemAnnouncementUseCase = pinSystemAnnouncementUseCase;
		this.cancelSystemAnnouncementUseCase = cancelSystemAnnouncementUseCase;
		this.dismissSystemAnnouncementUseCase = dismissSystemAnnouncementUseCase;
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

	@PostMapping("/{announcementId}/publish")
	@RequireAdminPermission(AdminPermission.SYSTEM_ANNOUNCEMENT_PUBLISH)
	public ResponseEntity<ApiResponse<PublishSystemAnnouncementResponse>> publish(
			@PathVariable UUID announcementId
	) {
		PublishSystemAnnouncementResult result = publishSystemAnnouncementUseCase.execute(
				new PublishSystemAnnouncementCommand(announcementId)
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
