package com.twohands.admin_service.integration.audit;

import com.twohands.admin_service.application.audit.logadminaction.LogAdminActionCommand;
import com.twohands.admin_service.application.audit.logadminaction.LogAdminActionUseCase;
import com.twohands.admin_service.application.audit.viewlogs.ViewAdminActionLogDetailQuery;
import com.twohands.admin_service.application.audit.viewlogs.ViewAdminActionLogDetailUseCase;
import com.twohands.admin_service.application.audit.viewlogs.ViewAdminActionLogsQuery;
import com.twohands.admin_service.application.audit.viewlogs.ViewAdminActionLogsUseCase;
import com.twohands.admin_service.domain.audit.AdminActionStatus;
import com.twohands.admin_service.domain.audit.AdminActionTargetType;
import com.twohands.admin_service.domain.auth.AdminAuthorizationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ViewAdminActionLogsIntegrationTest {

	@Autowired
	private LogAdminActionUseCase logAdminActionUseCase;

	@Autowired
	private ViewAdminActionLogsUseCase viewAdminActionLogsUseCase;

	@Autowired
	private ViewAdminActionLogDetailUseCase viewAdminActionLogDetailUseCase;

	@MockBean
	private AdminAuthorizationService adminAuthorizationService;

	@Test
	void execute_searchesAndReturnsLogDetail() {
		UUID adminId = UUID.randomUUID();
		when(adminAuthorizationService.requireCurrentAdminId()).thenReturn(adminId);

		var logged = logAdminActionUseCase.execute(new LogAdminActionCommand(
				adminId,
				"USER_SUSPEND",
				AdminActionTargetType.USER,
				"target-user-id",
				AdminActionStatus.SUCCESS,
				"User suspended",
				Map.of("reason", "spam"),
				Map.of("enforcement_id", UUID.randomUUID().toString()),
				"10.0.0.1",
				"integration-test",
				false
		));

		var list = viewAdminActionLogsUseCase.execute(new ViewAdminActionLogsQuery(
				adminId,
				"USER_SUSPEND",
				AdminActionTargetType.USER,
				"target-user-id",
				"SUCCESS",
				null,
				null,
				1,
				10
		));

		assertEquals(1, list.totalElements());
		assertEquals(logged.logId(), list.logs().get(0).logId());

		var detail = viewAdminActionLogDetailUseCase.execute(new ViewAdminActionLogDetailQuery(logged.logId()));
		assertEquals(adminId, detail.adminId());
		assertEquals("USER_SUSPEND", detail.actionType());
		assertNotNull(detail.createdAt());
	}
}
