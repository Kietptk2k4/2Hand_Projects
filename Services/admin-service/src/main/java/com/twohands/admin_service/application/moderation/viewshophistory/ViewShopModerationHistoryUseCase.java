package com.twohands.admin_service.application.moderation.viewshophistory;

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
public class ViewShopModerationHistoryUseCase {

	private static final String SUCCESS_MESSAGE = "Shop moderation history retrieved successfully";

	private final AdminAuthorizationService adminAuthorizationService;
	private final ContentModerationLogRepository contentModerationLogRepository;

	public ViewShopModerationHistoryUseCase(
			AdminAuthorizationService adminAuthorizationService,
			ContentModerationLogRepository contentModerationLogRepository
	) {
		this.adminAuthorizationService = adminAuthorizationService;
		this.contentModerationLogRepository = contentModerationLogRepository;
	}

	@Transactional(readOnly = true)
	public ViewShopModerationHistoryResult execute(ViewShopModerationHistoryQuery query) {
		adminAuthorizationService.requireCurrentAdminId();
		adminAuthorizationService.requirePermission(AdminPermission.SHOP_MODERATION_READ);

		PageRequest pageRequest = PaginationPolicy.normalize(query.page(), query.size());
		PagedResult<ContentModerationLog> historyPage = contentModerationLogRepository.findByTargetTypeAndTargetIdOrderByCreatedAtDesc(
				ContentModerationTargetType.SHOP,
				query.shopId().toString(),
				pageRequest
		);

		List<ShopModerationHistoryItem> history = historyPage.items().stream()
				.map(this::toItem)
				.toList();

		return new ViewShopModerationHistoryResult(
				query.shopId(),
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

	private ShopModerationHistoryItem toItem(ContentModerationLog log) {
		return new ShopModerationHistoryItem(
				log.id(),
				log.action(),
				log.reason(),
				log.note(),
				log.adminId(),
				log.createdAt()
		);
	}
}
