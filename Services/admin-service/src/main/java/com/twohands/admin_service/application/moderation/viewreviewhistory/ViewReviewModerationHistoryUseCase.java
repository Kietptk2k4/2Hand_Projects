package com.twohands.admin_service.application.moderation.viewreviewhistory;

import com.twohands.admin_service.constant.AdminPermission;
import com.twohands.admin_service.domain.auth.AdminAuthorizationService;
import com.twohands.admin_service.domain.common.PageRequest;
import com.twohands.admin_service.domain.common.PagedResult;
import com.twohands.admin_service.domain.common.PaginationPolicy;
import com.twohands.admin_service.domain.moderation.ContentModerationLog;
import com.twohands.admin_service.domain.moderation.ContentModerationLogRepository;
import com.twohands.admin_service.domain.moderation.ContentModerationTargetType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ViewReviewModerationHistoryUseCase {

	private static final String SUCCESS_MESSAGE = "Review moderation history retrieved successfully";

	private final AdminAuthorizationService adminAuthorizationService;
	private final ContentModerationLogRepository contentModerationLogRepository;

	public ViewReviewModerationHistoryUseCase(
			AdminAuthorizationService adminAuthorizationService,
			ContentModerationLogRepository contentModerationLogRepository
	) {
		this.adminAuthorizationService = adminAuthorizationService;
		this.contentModerationLogRepository = contentModerationLogRepository;
	}

	@Transactional(readOnly = true)
	public ViewReviewModerationHistoryResult execute(ViewReviewModerationHistoryQuery query) {
		adminAuthorizationService.requireCurrentAdminId();
		adminAuthorizationService.requirePermission(AdminPermission.REVIEW_MODERATION_READ);

		PageRequest pageRequest = PaginationPolicy.normalize(query.page(), query.size());
		PagedResult<ContentModerationLog> historyPage = contentModerationLogRepository.findByTargetTypeAndTargetIdOrderByCreatedAtDesc(
				ContentModerationTargetType.REVIEW,
				query.reviewId().toString(),
				pageRequest
		);

		List<ReviewModerationHistoryItem> history = historyPage.items().stream()
				.map(this::toItem)
				.toList();

		return new ViewReviewModerationHistoryResult(
				query.reviewId(),
				historyPage.page(),
				historyPage.size(),
				historyPage.totalElements(),
				historyPage.totalPages(),
				history
		);
	}

	public String successMessage() {
		return SUCCESS_MESSAGE;
	}

	private ReviewModerationHistoryItem toItem(ContentModerationLog log) {
		return new ReviewModerationHistoryItem(
				log.id(),
				log.action(),
				log.reason(),
				log.note(),
				log.adminId(),
				log.createdAt()
		);
	}
}
