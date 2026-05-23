package com.twohands.admin_service.unit.audit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.admin_service.application.audit.logadminaction.LogAdminActionCommand;
import com.twohands.admin_service.application.audit.logadminaction.LogAdminActionResult;
import com.twohands.admin_service.application.audit.logadminaction.LogAdminActionUseCase;
import com.twohands.admin_service.domain.audit.AdminActionLog;
import com.twohands.admin_service.domain.audit.AdminActionLogRepository;
import com.twohands.admin_service.domain.audit.AdminActionStatus;
import com.twohands.admin_service.domain.audit.AdminActionTargetType;
import com.twohands.admin_service.domain.audit.AuditPayloadSanitizer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LogAdminActionUseCaseTest {

	@Mock
	private AdminActionLogRepository adminActionLogRepository;

	@Mock
	private AuditPayloadSanitizer auditPayloadSanitizer;

	private LogAdminActionUseCase useCase;

	@BeforeEach
	void setUp() {
		useCase = new LogAdminActionUseCase(adminActionLogRepository, auditPayloadSanitizer, new ObjectMapper());
	}

	@Test
	void execute_persistsCriticalActionWithPayload() {
		UUID adminId = UUID.randomUUID();
		UUID logId = UUID.randomUUID();
		when(auditPayloadSanitizer.sanitizeToJson(Map.of("reason", "spam"))).thenReturn("{\"reason\":\"spam\"}");
		when(adminActionLogRepository.save(any())).thenAnswer(invocation -> {
			AdminActionLog log = invocation.getArgument(0);
			return new AdminActionLog(
					logId,
					log.adminId(),
					log.actionType(),
					log.targetType(),
					log.targetId(),
					log.status(),
					log.requestPayloadJson(),
					log.responsePayloadJson(),
					log.ipAddress(),
					log.userAgent(),
					log.createdAt()
			);
		});

		LogAdminActionResult result = useCase.execute(new LogAdminActionCommand(
				adminId,
				"USER_SUSPEND",
				AdminActionTargetType.USER,
				UUID.randomUUID().toString(),
				AdminActionStatus.SUCCESS,
				"User suspended",
				Map.of("reason", "spam"),
				Map.of(),
				"127.0.0.1",
				"JUnit",
				false
		));

		assertEquals(logId, result.logId());
		ArgumentCaptor<AdminActionLog> captor = ArgumentCaptor.forClass(AdminActionLog.class);
		verify(adminActionLogRepository).save(captor.capture());
		AdminActionLog saved = captor.getValue();
		assertNotNull(saved.requestPayloadJson());
		assertTrue(saved.responsePayloadJson().contains("\"status\":\"SUCCESS\""));
	}

	@Test
	void execute_skipsRequestPayloadForNonCriticalAction() {
		UUID adminId = UUID.randomUUID();
		when(adminActionLogRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

		useCase.execute(new LogAdminActionCommand(
				adminId,
				"ORDER_SUPPORT_VIEW",
				AdminActionTargetType.ORDER,
				"order-1",
				AdminActionStatus.SUCCESS,
				"Viewed order",
				Map.of("orderId", "order-1"),
				Map.of(),
				null,
				null,
				false
		));

		ArgumentCaptor<AdminActionLog> captor = ArgumentCaptor.forClass(AdminActionLog.class);
		verify(adminActionLogRepository).save(captor.capture());
		assertNull(captor.getValue().requestPayloadJson());
	}
}
