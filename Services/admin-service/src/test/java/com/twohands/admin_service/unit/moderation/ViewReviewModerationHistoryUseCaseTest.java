package com.twohands.admin_service.unit.moderation;

import com.twohands.admin_service.application.moderation.viewreviewhistory.ViewReviewModerationHistoryQuery;
import com.twohands.admin_service.application.moderation.viewreviewhistory.ViewReviewModerationHistoryUseCase;
import com.twohands.admin_service.constant.AdminPermission;
import com.twohands.admin_service.domain.auth.AdminAuthorizationService;
import com.twohands.admin_service.domain.common.PageRequest;
import com.twohands.admin_service.domain.common.PagedResult;
import com.twohands.admin_service.domain.moderation.ContentModerationAction;
import com.twohands.admin_service.domain.moderation.ContentModerationLog;
import com.twohands.admin_service.domain.moderation.ContentModerationLogRepository;
import com.twohands.admin_service.domain.moderation.ContentModerationTargetType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ViewReviewModerationHistoryUseCaseTest {

	private final AdminAuthorizationService adminAuthorizationService = mock(AdminAuthorizationService.class);
	private final ContentModerationLogRepository contentModerationLogRepository = mock(ContentModerationLogRepository.class);

	private ViewReviewModerationHistoryUseCase useCase;

	@BeforeEach
	void setUp() {
		useCase = new ViewReviewModerationHistoryUseCase(
				adminAuthorizationService,
				contentModerationLogRepository
		);
	}

	@Test
	void shouldReturnPagedHistoryNewestFirst() {
		UUID adminId = UUID.randomUUID();
		UUID reviewId = UUID.randomUUID();
		Instant now = Instant.now();

		ContentModerationLog restoreLog = new ContentModerationLog(
				UUID.randomUUID(),
				ContentModerationTargetType.REVIEW,
				reviewId.toString(),
				ContentModerationAction.RESTORE,
				"Appeal approved",
				adminId,
				now,
				"Case note"
		);
		ContentModerationLog hideLog = new ContentModerationLog(
				UUID.randomUUID(),
				ContentModerationTargetType.REVIEW,
				reviewId.toString(),
				ContentModerationAction.HIDE,
				"Policy violation",
				adminId,
				now.minusSeconds(60),
				null
		);

		when(adminAuthorizationService.requireCurrentAdminId()).thenReturn(adminId);
		when(contentModerationLogRepository.findByTargetTypeAndTargetIdOrderByCreatedAtDesc(
				eq(ContentModerationTargetType.REVIEW),
				eq(reviewId.toString()),
				eq(new PageRequest(1, 20))
		)).thenReturn(new PagedResult<>(List.of(restoreLog, hideLog), 1, 20, 2, 1));

		var result = useCase.execute(new ViewReviewModerationHistoryQuery(reviewId, 1, 20));

		assertThat(result.reviewId()).isEqualTo(reviewId);
		assertThat(result.totalElements()).isEqualTo(2);
		assertThat(result.history()).hasSize(2);
		assertThat(result.history().get(0).action()).isEqualTo(ContentModerationAction.RESTORE);
		assertThat(result.history().get(0).reason()).isEqualTo("Appeal approved");
		assertThat(result.history().get(1).action()).isEqualTo(ContentModerationAction.HIDE);
		verify(adminAuthorizationService).requirePermission(AdminPermission.REVIEW_MODERATION_READ);
	}

	@Test
	void shouldReturnEmptyHistoryWhenNoLogs() {
		UUID reviewId = UUID.randomUUID();

		when(adminAuthorizationService.requireCurrentAdminId()).thenReturn(UUID.randomUUID());
		when(contentModerationLogRepository.findByTargetTypeAndTargetIdOrderByCreatedAtDesc(
				eq(ContentModerationTargetType.REVIEW),
				eq(reviewId.toString()),
				eq(new PageRequest(1, 20))
		)).thenReturn(new PagedResult<>(List.of(), 1, 20, 0, 0));

		var result = useCase.execute(new ViewReviewModerationHistoryQuery(reviewId, 1, 20));

		assertThat(result.history()).isEmpty();
		assertThat(result.totalElements()).isZero();
	}
}
