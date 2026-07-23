package com.twohands.admin_service.application.moderation.viewcommenthistory;

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
public class ViewCommentModerationHistoryUseCase {

	private static final String SUCCESS_MESSAGE = "Comment moderation history retrieved successfully";

	private final AdminAuthorizationService adminAuthorizationService;
	private final ContentModerationLogRepository contentModerationLogRepository;

	public ViewCommentModerationHistoryUseCase(
			AdminAuthorizationService adminAuthorizationService,
			ContentModerationLogRepository contentModerationLogRepository
	) {
		this.adminAuthorizationService = adminAuthorizationService;
		this.contentModerationLogRepository = contentModerationLogRepository;
	}

	@Transactional(readOnly = true)
	public ViewCommentModerationHistoryResult execute(ViewCommentModerationHistoryQuery query) {
		adminAuthorizationService.requireCurrentAdminId();
		adminAuthorizationService.requirePermission(AdminPermission.COMMENT_MODERATION_READ);

		PageRequest pageRequest = PaginationPolicy.normalize(query.page(), query.size());
		PagedResult<ContentModerationLog> historyPage = contentModerationLogRepository.findByTargetTypeAndTargetIdOrderByCreatedAtDesc(
				ContentModerationTargetType.COMMENT,
				query.commentId(),
				pageRequest
		);

		List<CommentModerationHistoryItem> history = historyPage.items().stream()
				.map(this::toItem)
				.toList();

		return new ViewCommentModerationHistoryResult(
				query.commentId(),
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

	private CommentModerationHistoryItem toItem(ContentModerationLog log) {
		return new CommentModerationHistoryItem(
				log.id(),
				log.action(),
				log.reason(),
				log.note(),
				log.adminId(),
				log.createdAt()
		);
	}
}
