package com.twohands.admin_service.integration.auth;

import com.twohands.admin_service.constant.AdminRole;
import com.twohands.admin_service.support.AdminJwtTestTokens;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CheckAdminRoleIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void listRoles_returns401WithoutToken() throws Exception {
		mockMvc.perform(get("/admin/api/v1/me/roles"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void listRoles_returnsRolesFromJwt() throws Exception {
		UUID adminId = UUID.randomUUID();
		String token = AdminJwtTestTokens.accessToken(
				adminId,
				List.of(AdminRole.MODERATOR, AdminRole.SUPPORT),
				List.of()
		);

		mockMvc.perform(get("/admin/api/v1/me/roles")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data.admin_id").value(adminId.toString()))
				.andExpect(jsonPath("$.data.roles.length()").value(2))
				.andExpect(jsonPath("$.data.roles[0]").value(AdminRole.MODERATOR));
	}

	@Test
	void checkRole_returnsGrantedTrue() throws Exception {
		UUID adminId = UUID.randomUUID();
		String token = AdminJwtTestTokens.accessToken(adminId, List.of(AdminRole.SUPER_ADMIN), List.of());

		mockMvc.perform(get("/admin/api/v1/me/roles/check")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
						.param("role", AdminRole.SUPER_ADMIN))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.granted").value(true))
				.andExpect(jsonPath("$.data.role_code").value(AdminRole.SUPER_ADMIN));
	}

	@Test
	void checkRole_returnsGrantedFalse() throws Exception {
		UUID adminId = UUID.randomUUID();
		String token = AdminJwtTestTokens.accessToken(adminId, List.of(AdminRole.SUPPORT), List.of());

		mockMvc.perform(get("/admin/api/v1/me/roles/check")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
						.param("role", AdminRole.MODERATOR))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.granted").value(false));
	}

	@Test
	void checkRole_returns400ForUnknownRole() throws Exception {
		UUID adminId = UUID.randomUUID();
		String token = AdminJwtTestTokens.accessToken(adminId, List.of(AdminRole.MODERATOR), List.of());

		mockMvc.perform(get("/admin/api/v1/me/roles/check")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
						.param("role", "INVALID_ROLE"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.success").value(false));
	}
}
