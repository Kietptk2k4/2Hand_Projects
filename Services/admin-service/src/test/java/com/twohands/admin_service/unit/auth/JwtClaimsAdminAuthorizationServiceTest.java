package com.twohands.admin_service.unit.auth;

import com.twohands.admin_service.application.auth.JwtClaimsAdminAuthorizationService;
import com.twohands.admin_service.constant.AdminPermission;
import com.twohands.admin_service.domain.auth.AuthPermissionGateway;
import com.twohands.admin_service.domain.auth.AuthRoleGateway;
import com.twohands.admin_service.exception.AppException;
import com.twohands.admin_service.exception.ErrorCode;
import com.twohands.admin_service.security.AuthenticatedUser;
import com.twohands.admin_service.security.BearerTokenDetails;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtClaimsAdminAuthorizationServiceTest {

	private final JwtClaimsAdminAuthorizationService service = new JwtClaimsAdminAuthorizationService(
			disabledPermissionGateway(),
			disabledRoleGateway()
	);

	private static AuthPermissionGateway disabledPermissionGateway() {
		return new AuthPermissionGateway() {
			@Override
			public boolean isEnabled() {
				return false;
			}

			@Override
			public boolean isAvailable() {
				return false;
			}

			@Override
			public boolean hasPermission(UUID adminId, String permissionCode, String bearerToken) {
				return false;
			}
		};
	}

	private static com.twohands.admin_service.domain.auth.AuthRoleGateway disabledRoleGateway() {
		return new com.twohands.admin_service.domain.auth.AuthRoleGateway() {
			@Override
			public boolean isEnabled() {
				return false;
			}

			@Override
			public boolean isAvailable() {
				return false;
			}

			@Override
			public java.util.List<String> resolveRoles(UUID adminId, String bearerToken) {
				return java.util.List.of();
			}
		};
	}

	@AfterEach
	void clearContext() {
		SecurityContextHolder.clearContext();
	}

	@Test
	void requirePermission_passesWhenClaimPresent() {
		UUID adminId = UUID.randomUUID();
		setAdmin(adminId, List.of("MODERATOR"), List.of(AdminPermission.USER_SUSPEND));

		service.requirePermission(AdminPermission.USER_SUSPEND);
	}

	@Test
	void requirePermission_failsWhenClaimMissing() {
		UUID adminId = UUID.randomUUID();
		setAdmin(adminId, List.of("MODERATOR"), List.of());

		AppException ex = assertThrows(AppException.class, () -> service.requirePermission(AdminPermission.USER_SUSPEND));
		assertEquals(ErrorCode.FORBIDDEN, ex.getErrorCode());
	}

	@Test
	void requireCurrentAdmin_failsWhenUnauthenticated() {
		AppException ex = assertThrows(AppException.class, service::requireCurrentAdmin);
		assertEquals(ErrorCode.UNAUTHORIZED, ex.getErrorCode());
	}

	@Test
	void hasPermission_returnsTrueWhenClaimPresent() {
		UUID adminId = UUID.randomUUID();
		setAdmin(adminId, List.of(), List.of(AdminPermission.USER_SUSPEND));

		assertTrue(service.hasPermission(adminId, AdminPermission.USER_SUSPEND));
	}

	@Test
	void hasRole_returnsTrueWhenRoleInClaims() {
		UUID adminId = UUID.randomUUID();
		setAdmin(adminId, List.of("MODERATOR"), List.of());

		assertTrue(service.hasRole(adminId, "MODERATOR"));
		assertEquals(List.of("MODERATOR"), service.getRoles(adminId));
	}

	private void setAdmin(UUID adminId, List<String> roles, List<String> permissions) {
		var authentication = new UsernamePasswordAuthenticationToken(
				new AuthenticatedUser(adminId, roles, permissions),
				null,
				List.of()
		);
		authentication.setDetails(new BearerTokenDetails("test-token"));
		SecurityContextHolder.getContext().setAuthentication(authentication);
	}
}
