package com.twohands.admin_service.application.announcement.listsystemannouncements;

import com.twohands.admin_service.constant.AdminPermission;
import com.twohands.admin_service.domain.announcement.SystemAnnouncementSearchCriteria;
import com.twohands.admin_service.domain.announcement.SystemAnnouncementSeverity;
import com.twohands.admin_service.domain.announcement.SystemAnnouncementStatus;
import com.twohands.admin_service.domain.auth.AdminAuthorizationService;
import com.twohands.admin_service.domain.common.PageRequest;
import com.twohands.admin_service.domain.common.PagedResult;
import com.twohands.admin_service.domain.common.PaginationPolicy;
import com.twohands.admin_service.exception.AppException;
import com.twohands.admin_service.exception.ErrorCode;
import com.twohands.admin_service.infrastructure.persistence.announcement.SystemAnnouncementSearchRepository;
import com.twohands.admin_service.infrastructure.persistence.jpa.entity.SystemAnnouncementEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ListSystemAnnouncementsUseCase {

	private static final String SUCCESS_MESSAGE = "System announcements retrieved successfully";

	private final AdminAuthorizationService adminAuthorizationService;
	private final SystemAnnouncementSearchRepository systemAnnouncementSearchRepository;

	public ListSystemAnnouncementsUseCase(
			AdminAuthorizationService adminAuthorizationService,
			SystemAnnouncementSearchRepository systemAnnouncementSearchRepository
	) {
		this.adminAuthorizationService = adminAuthorizationService;
		this.systemAnnouncementSearchRepository = systemAnnouncementSearchRepository;
	}

	@Transactional(readOnly = true)
	public ListSystemAnnouncementsResult execute(ListSystemAnnouncementsQuery query) {
		adminAuthorizationService.requireCurrentAdminId();
		adminAuthorizationService.requireAnyPermission(
				AdminPermission.SYSTEM_ANNOUNCEMENT_CREATE,
				AdminPermission.SYSTEM_ANNOUNCEMENT_UPDATE,
				AdminPermission.SYSTEM_ANNOUNCEMENT_PUBLISH,
				AdminPermission.SYSTEM_ANNOUNCEMENT_CANCEL
		);

		SystemAnnouncementSearchCriteria criteria = new SystemAnnouncementSearchCriteria(
				normalizeOptionalText(query.query()),
				parseStatus(query.status()),
				parseSeverity(query.severity())
		);

		PageRequest pageRequest = PaginationPolicy.normalize(query.page(), query.size());
		PagedResult<SystemAnnouncementEntity> page = systemAnnouncementSearchRepository.search(criteria, pageRequest);

		List<SystemAnnouncementListItem> items = page.items().stream()
				.map(this::toItem)
				.toList();

		return new ListSystemAnnouncementsResult(
				page.page(),
				page.size(),
				page.totalElements(),
				page.totalPages(),
				items
		);
	}

	public String successMessage() {
		return SUCCESS_MESSAGE;
	}

	private SystemAnnouncementListItem toItem(SystemAnnouncementEntity entity) {
		return new SystemAnnouncementListItem(
				entity.getId(),
				entity.getTitle(),
				entity.getContent(),
				SystemAnnouncementSeverity.valueOf(entity.getSeverity().name()),
				SystemAnnouncementStatus.valueOf(entity.getStatus().name()),
				entity.isPinned(),
				entity.isDismissible(),
				entity.getCreatedBy(),
				entity.getCreatedAt(),
				entity.getSentAt()
		);
	}

	private String normalizeOptionalText(String value) {
		if (value == null) {
			return null;
		}
		String trimmed = value.trim();
		return trimmed.isEmpty() ? null : trimmed;
	}

	private SystemAnnouncementStatus parseStatus(String status) {
		if (status == null || status.isBlank()) {
			return null;
		}
		try {
			return SystemAnnouncementStatus.valueOf(status.trim().toUpperCase());
		} catch (IllegalArgumentException ex) {
			throw new AppException(ErrorCode.VALIDATION_ERROR, "Invalid status: " + status);
		}
	}

	private SystemAnnouncementSeverity parseSeverity(String severity) {
		if (severity == null || severity.isBlank()) {
			return null;
		}
		try {
			return SystemAnnouncementSeverity.valueOf(severity.trim().toUpperCase());
		} catch (IllegalArgumentException ex) {
			throw new AppException(ErrorCode.VALIDATION_ERROR, "Invalid severity: " + severity);
		}
	}
}
