package com.twohands.admin_service.delivery.http.announcement;

import com.twohands.admin_service.application.announcement.createsystemannouncement.CreateSystemAnnouncementCommand;
import com.twohands.admin_service.application.announcement.createsystemannouncement.CreateSystemAnnouncementResult;
import com.twohands.admin_service.application.announcement.createsystemannouncement.CreateSystemAnnouncementUseCase;
import com.twohands.admin_service.common.dto.ApiResponse;
import com.twohands.admin_service.constant.AdminPermission;
import com.twohands.admin_service.security.annotation.RequireAdminPermission;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/api/v1/system-announcements")
public class SystemAnnouncementController {

	private final CreateSystemAnnouncementUseCase createSystemAnnouncementUseCase;

	public SystemAnnouncementController(CreateSystemAnnouncementUseCase createSystemAnnouncementUseCase) {
		this.createSystemAnnouncementUseCase = createSystemAnnouncementUseCase;
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
}
