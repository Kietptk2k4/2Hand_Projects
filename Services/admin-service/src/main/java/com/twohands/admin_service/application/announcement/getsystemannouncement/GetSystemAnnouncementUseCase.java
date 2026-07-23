package com.twohands.admin_service.application.announcement.getsystemannouncement;

import com.twohands.admin_service.constant.AdminPermission;
import com.twohands.admin_service.domain.announcement.SystemAnnouncement;
import com.twohands.admin_service.domain.announcement.SystemAnnouncementRepository;
import com.twohands.admin_service.domain.auth.AdminAuthorizationService;
import com.twohands.admin_service.exception.AppException;
import com.twohands.admin_service.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GetSystemAnnouncementUseCase {

	private static final String SUCCESS_MESSAGE = "System announcement retrieved successfully";

	private final AdminAuthorizationService adminAuthorizationService;
	private final SystemAnnouncementRepository systemAnnouncementRepository;

	public GetSystemAnnouncementUseCase(
			AdminAuthorizationService adminAuthorizationService,
			SystemAnnouncementRepository systemAnnouncementRepository
	) {
		this.adminAuthorizationService = adminAuthorizationService;
		this.systemAnnouncementRepository = systemAnnouncementRepository;
	}

	@Transactional(readOnly = true)
	public GetSystemAnnouncementResult execute(GetSystemAnnouncementQuery query) {
		adminAuthorizationService.requireCurrentAdminId();
		adminAuthorizationService.requireAnyPermission(
				AdminPermission.SYSTEM_ANNOUNCEMENT_CREATE,
				AdminPermission.SYSTEM_ANNOUNCEMENT_UPDATE,
				AdminPermission.SYSTEM_ANNOUNCEMENT_PUBLISH,
				AdminPermission.SYSTEM_ANNOUNCEMENT_CANCEL
		);

		SystemAnnouncement announcement = systemAnnouncementRepository.findById(query.announcementId())
				.orElseThrow(() -> new AppException(
						ErrorCode.RESOURCE_NOT_FOUND,
						ErrorCode.RESOURCE_NOT_FOUND.defaultMessage()
				));

		return toResult(announcement);
	}

	public String successMessage() {
		return SUCCESS_MESSAGE;
	}

	private GetSystemAnnouncementResult toResult(SystemAnnouncement announcement) {
		return new GetSystemAnnouncementResult(
				announcement.id(),
				announcement.title(),
				announcement.content(),
				announcement.severity(),
				announcement.status(),
				announcement.pinned(),
				announcement.dismissible(),
				announcement.createdBy(),
				announcement.createdAt(),
				announcement.sentAt()
		);
	}
}
