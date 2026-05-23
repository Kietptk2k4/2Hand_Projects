package com.twohands.admin_service.unit.audit;

import com.twohands.admin_service.application.audit.DefaultCriticalPayloadBuilder;
import com.twohands.admin_service.application.audit.JacksonAuditPayloadSanitizer;
import com.twohands.admin_service.application.audit.logadminaction.LogAdminActionResult;
import com.twohands.admin_service.application.audit.logadminaction.LogAdminActionUseCase;
import com.twohands.admin_service.application.audit.logcriticalpayload.LogCriticalAdminActionPayloadCommand;
import com.twohands.admin_service.application.audit.logcriticalpayload.LogCriticalAdminActionPayloadUseCase;
import com.twohands.admin_service.domain.audit.AdminActionStatus;
import com.twohands.admin_service.domain.audit.AdminActionTargetType;
import com.twohands.admin_service.domain.audit.AdminRequestContextProvider;
import com.twohands.admin_service.exception.AppException;
import com.twohands.admin_service.exception.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LogCriticalAdminActionPayloadUseCaseTest {

	@Mock
	private LogAdminActionUseCase logAdminActionUseCase;

	@Mock
	private AdminRequestContextProvider requestContextProvider;

	private LogCriticalAdminActionPayloadUseCase useCase;

	@BeforeEach
	void setUp() {
		useCase = new LogCriticalAdminActionPayloadUseCase(
				logAdminActionUseCase,
				new DefaultCriticalPayloadBuilder(),
				new JacksonAuditPayloadSanitizer(new ObjectMapper()),
				requestContextProvider
		);
	}

	@Test
	void execute_persistsBeforeAfterPayload() {
		UUID adminId = UUID.randomUUID();
		UUID logId = UUID.randomUUID();
		when(requestContextProvider.clientIpAddress()).thenReturn("127.0.0.1");
		when(requestContextProvider.userAgent()).thenReturn("JUnit");
		when(logAdminActionUseCase.execute(any())).thenReturn(new LogAdminActionResult(
				logId, adminId, "USER_SUSPEND", AdminActionTargetType.USER, "user-1", Instant.now()
		));

		var result = useCase.execute(new LogCriticalAdminActionPayloadCommand(
				adminId,
				"USER_SUSPEND",
				AdminActionTargetType.USER,
				UUID.randomUUID().toString(),
				AdminActionStatus.SUCCESS,
				"Suspended",
				"Policy violation",
				Map.of("status", "ACTIVE"),
				Map.of("status", "SUSPENDED"),
				Map.of("reason_code", "ABUSE"),
				Map.of("enforcement_id", UUID.randomUUID().toString())
		));

		assertEquals(logId, result.logId());
		assertTrue(result.requestPayloadJson().contains("\"before\""));
		assertTrue(result.requestPayloadJson().contains("\"after\""));
		assertTrue(result.requestPayloadJson().contains("Policy violation"));
		verify(logAdminActionUseCase).execute(any());
	}

	@Test
	void execute_rejectsNonCriticalAction() {
		AppException ex = assertThrows(AppException.class, () -> useCase.execute(
				new LogCriticalAdminActionPayloadCommand(
						UUID.randomUUID(),
						"ORDER_SUPPORT_VIEW",
						AdminActionTargetType.ORDER,
						"order-1",
						AdminActionStatus.SUCCESS,
						"View",
						"summary",
						Map.of(),
						Map.of(),
						Map.of(),
						Map.of()
				)
		));
		assertEquals(ErrorCode.VALIDATION_ERROR, ex.getErrorCode());
	}
}
