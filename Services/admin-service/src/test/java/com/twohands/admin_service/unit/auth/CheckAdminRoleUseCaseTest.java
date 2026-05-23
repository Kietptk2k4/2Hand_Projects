package com.twohands.admin_service.unit.auth;

import com.twohands.admin_service.application.auth.checkadminrole.CheckAdminRoleCommand;
import com.twohands.admin_service.application.auth.checkadminrole.CheckAdminRoleResult;
import com.twohands.admin_service.application.auth.checkadminrole.CheckAdminRoleUseCase;
import com.twohands.admin_service.constant.AdminRole;
import com.twohands.admin_service.domain.auth.AdminAuthorizationService;
import com.twohands.admin_service.exception.AppException;
import com.twohands.admin_service.exception.ErrorCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CheckAdminRoleUseCaseTest {

	@Mock
	private AdminAuthorizationService adminAuthorizationService;

	@InjectMocks
	private CheckAdminRoleUseCase useCase;

	@Test
	void execute_returnsGrantedWhenRolePresent() {
		UUID adminId = UUID.randomUUID();
		when(adminAuthorizationService.requireCurrentAdminId()).thenReturn(adminId);
		when(adminAuthorizationService.hasRole(adminId, AdminRole.MODERATOR)).thenReturn(true);

		CheckAdminRoleResult result = useCase.execute(new CheckAdminRoleCommand("moderator"));

		assertTrue(result.granted());
		assertEquals(AdminRole.MODERATOR, result.roleCode());
	}

	@Test
	void execute_returnsNotGrantedWhenRoleMissing() {
		UUID adminId = UUID.randomUUID();
		when(adminAuthorizationService.requireCurrentAdminId()).thenReturn(adminId);
		when(adminAuthorizationService.hasRole(adminId, AdminRole.SUPER_ADMIN)).thenReturn(false);

		CheckAdminRoleResult result = useCase.execute(new CheckAdminRoleCommand(AdminRole.SUPER_ADMIN));

		assertFalse(result.granted());
	}

	@Test
	void execute_rejectsUnknownRole() {
		AppException ex = assertThrows(
				AppException.class,
				() -> useCase.execute(new CheckAdminRoleCommand("GUEST"))
		);
		assertEquals(ErrorCode.VALIDATION_ERROR, ex.getErrorCode());
	}
}
