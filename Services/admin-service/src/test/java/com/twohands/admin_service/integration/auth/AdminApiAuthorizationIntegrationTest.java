package com.twohands.admin_service.integration.auth;

import com.twohands.admin_service.constant.AdminPermission;
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
class AdminApiAuthorizationIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void protectedEndpoint_returns401WithoutToken() throws Exception {
		mockMvc.perform(get("/admin/api/v1/me"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.success").value(false));
	}

	@Test
	void me_returnsAdminClaimsWithValidToken() throws Exception {
		UUID adminId = UUID.randomUUID();
		String token = AdminJwtTestTokens.accessToken(
				adminId,
				List.of("MODERATOR"),
				List.of(AdminPermission.USER_SUSPEND)
		);

		mockMvc.perform(get("/admin/api/v1/me")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data.admin_id").value(adminId.toString()))
				.andExpect(jsonPath("$.data.permissions[0]").value(AdminPermission.USER_SUSPEND));
	}

	@Test
	void permissionProbe_returns403WithoutPermission() throws Exception {
		UUID adminId = UUID.randomUUID();
		String token = AdminJwtTestTokens.accessToken(adminId, List.of("SUPPORT"), List.of());

		mockMvc.perform(get("/admin/api/v1/authorization-probe/user-suspend")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.success").value(false));
	}

	@Test
	void permissionProbe_returns200WithPermission() throws Exception {
		UUID adminId = UUID.randomUUID();
		String token = AdminJwtTestTokens.accessToken(
				adminId,
				List.of("MODERATOR"),
				List.of(AdminPermission.USER_SUSPEND)
		);

		mockMvc.perform(get("/admin/api/v1/authorization-probe/user-suspend")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data.permission").value(AdminPermission.USER_SUSPEND));
	}
}
