package com.twohands.admin_service.unit.auth;

import com.twohands.admin_service.application.auth.checkadminpermission.CheckAdminPermissionCommand;
import com.twohands.admin_service.application.auth.checkadminpermission.CheckAdminPermissionResult;
import com.twohands.admin_service.application.auth.checkadminpermission.CheckAdminPermissionUseCase;
import com.twohands.admin_service.constant.AdminPermission;
import com.twohands.admin_service.domain.auth.AdminAuthorizationService;
import com.twohands.admin_service.exception.AppException;
import com.twohands.admin_service.exception.ErrorCode;
import com.twohands.admin_service.security.AuthenticatedUser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CheckAdminPermissionUseCaseTest {

	@Mock
	private AdminAuthorizationService adminAuthorizationService;

	@InjectMocks
	private CheckAdminPermissionUseCase useCase;

	@Test
	void execute_returnsGrantedWhenPermissionPresent() {
		UUID adminId = UUID.randomUUID();
		when(adminAuthorizationService.requireCurrentAdminId()).thenReturn(adminId);
		when(adminAuthorizationService.hasPermission(adminId, AdminPermission.USER_SUSPEND)).thenReturn(true);

		CheckAdminPermissionResult result = useCase.execute(
				new CheckAdminPermissionCommand(AdminPermission.USER_SUSPEND, null, null)
		);

		assertTrue(result.granted());
		assertEquals(adminId, result.adminId());
		assertEquals(AdminPermission.USER_SUSPEND, result.permissionCode());
		verify(adminAuthorizationService).hasPermission(adminId, AdminPermission.USER_SUSPEND);
	}

	@Test
	void execute_returnsNotGrantedWhenPermissionMissing() {
		UUID adminId = UUID.randomUUID();
		when(adminAuthorizationService.requireCurrentAdminId()).thenReturn(adminId);
		when(adminAuthorizationService.hasPermission(adminId, AdminPermission.PRODUCT_REMOVE)).thenReturn(false);

		CheckAdminPermissionResult result = useCase.execute(
				new CheckAdminPermissionCommand("product_remove", "PRODUCT", "p-1")
		);

		assertFalse(result.granted());
		assertEquals(AdminPermission.PRODUCT_REMOVE, result.permissionCode());
		assertEquals("PRODUCT", result.resourceType());
		assertEquals("p-1", result.resourceId());
	}

	@Test
	void execute_rejectsBlankPermission() {
		AppException ex = assertThrows(
				AppException.class,
				() -> useCase.execute(new CheckAdminPermissionCommand("  ", null, null))
		);
		assertEquals(ErrorCode.VALIDATION_ERROR, ex.getErrorCode());
	}

	@Test
	void execute_rejectsUnknownPermission() {
		AppException ex = assertThrows(
				AppException.class,
				() -> useCase.execute(new CheckAdminPermissionCommand("UNKNOWN_PERM", null, null))
		);
		assertEquals(ErrorCode.VALIDATION_ERROR, ex.getErrorCode());
	}
}
