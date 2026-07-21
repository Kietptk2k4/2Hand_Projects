package com.twohands.admin_service.unit.moderation;

import com.twohands.admin_service.application.moderation.viewposthistory.ViewPostModerationHistoryQuery;
import com.twohands.admin_service.application.moderation.viewposthistory.ViewPostModerationHistoryUseCase;
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

class ViewPostModerationHistoryUseCaseTest {

	private final AdminAuthorizationService adminAuthorizationService = mock(AdminAuthorizationService.class);
	private final ContentModerationLogRepository contentModerationLogRepository = mock(ContentModerationLogRepository.class);

	private ViewPostModerationHistoryUseCase useCase;

	@BeforeEach
	void setUp() {
		useCase = new ViewPostModerationHistoryUseCase(
				adminAuthorizationService,
				contentModerationLogRepository
		);
	}

	@Test
	void shouldReturnPagedHistoryNewestFirst() {
		UUID adminId = UUID.randomUUID();
		String postId = "674a10000000000000000002";
		Instant now = Instant.now();

		ContentModerationLog restoreLog = new ContentModerationLog(
				UUID.randomUUID(),
				ContentModerationTargetType.POST,
				postId,
				ContentModerationAction.RESTORE,
				"Appeal approved",
				adminId,
				now,
				"Case note"
		);
		ContentModerationLog hideLog = new ContentModerationLog(
				UUID.randomUUID(),
				ContentModerationTargetType.POST,
				postId,
				ContentModerationAction.HIDE,
				"Policy violation",
				adminId,
				now.minusSeconds(60),
				null
		);

		when(adminAuthorizationService.requireCurrentAdminId()).thenReturn(adminId);
		when(contentModerationLogRepository.findByTargetTypeAndTargetIdOrderByCreatedAtDesc(
				eq(ContentModerationTargetType.POST),
				eq(postId),
				eq(new PageRequest(1, 20))
		)).thenReturn(new PagedResult<>(List.of(restoreLog, hideLog), 1, 20, 2, 1));

		var result = useCase.execute(new ViewPostModerationHistoryQuery(postId, 1, 20));

		assertThat(result.postId()).isEqualTo(postId);
		assertThat(result.totalElements()).isEqualTo(2);
		assertThat(result.history()).hasSize(2);
		assertThat(result.history().get(0).action()).isEqualTo(ContentModerationAction.RESTORE);
		assertThat(result.history().get(1).action()).isEqualTo(ContentModerationAction.HIDE);
		verify(adminAuthorizationService).requirePermission(AdminPermission.POST_MODERATION_READ);
	}

	@Test
	void shouldReturnEmptyHistoryWhenNoLogs() {
		String postId = "674a10000000000000000001";

		when(adminAuthorizationService.requireCurrentAdminId()).thenReturn(UUID.randomUUID());
		when(contentModerationLogRepository.findByTargetTypeAndTargetIdOrderByCreatedAtDesc(
				eq(ContentModerationTargetType.POST),
				eq(postId),
				eq(new PageRequest(1, 20))
		)).thenReturn(new PagedResult<>(List.of(), 1, 20, 0, 0));

		var result = useCase.execute(new ViewPostModerationHistoryQuery(postId, 1, 20));

		assertThat(result.history()).isEmpty();
		assertThat(result.totalElements()).isZero();
	}
}
