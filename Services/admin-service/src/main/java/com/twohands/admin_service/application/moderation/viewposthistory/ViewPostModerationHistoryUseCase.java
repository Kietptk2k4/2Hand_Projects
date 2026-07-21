package com.twohands.admin_service.application.moderation.viewposthistory;

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
public class ViewPostModerationHistoryUseCase {

	private static final String SUCCESS_MESSAGE = "Post moderation history retrieved successfully";

	private final AdminAuthorizationService adminAuthorizationService;
	private final ContentModerationLogRepository contentModerationLogRepository;

	public ViewPostModerationHistoryUseCase(
			AdminAuthorizationService adminAuthorizationService,
			ContentModerationLogRepository contentModerationLogRepository
	) {
		this.adminAuthorizationService = adminAuthorizationService;
		this.contentModerationLogRepository = contentModerationLogRepository;
	}

	@Transactional(readOnly = true)
	public ViewPostModerationHistoryResult execute(ViewPostModerationHistoryQuery query) {
		adminAuthorizationService.requireCurrentAdminId();
		adminAuthorizationService.requirePermission(AdminPermission.POST_MODERATION_READ);

		PageRequest pageRequest = PaginationPolicy.normalize(query.page(), query.size());
		PagedResult<ContentModerationLog> historyPage = contentModerationLogRepository.findByTargetTypeAndTargetIdOrderByCreatedAtDesc(
				ContentModerationTargetType.POST,
				query.postId(),
				pageRequest
		);

		List<PostModerationHistoryItem> history = historyPage.items().stream()
				.map(this::toItem)
				.toList();

		return new ViewPostModerationHistoryResult(
				query.postId(),
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

	private PostModerationHistoryItem toItem(ContentModerationLog log) {
		return new PostModerationHistoryItem(
				log.id(),
				log.action(),
				log.reason(),
				log.note(),
				log.adminId(),
				log.createdAt()
		);
	}
}
