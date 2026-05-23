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
class CheckAdminPermissionIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void checkPermission_returns401WithoutToken() throws Exception {
		mockMvc.perform(get("/admin/api/v1/me/permissions/check")
						.param("permission", AdminPermission.USER_SUSPEND))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void checkPermission_returnsGrantedTrue() throws Exception {
		UUID adminId = UUID.randomUUID();
		String token = AdminJwtTestTokens.accessToken(
				adminId,
				List.of("MODERATOR"),
				List.of(AdminPermission.USER_SUSPEND)
		);

		mockMvc.perform(get("/admin/api/v1/me/permissions/check")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
						.param("permission", AdminPermission.USER_SUSPEND))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data.admin_id").value(adminId.toString()))
				.andExpect(jsonPath("$.data.permission_code").value(AdminPermission.USER_SUSPEND))
				.andExpect(jsonPath("$.data.granted").value(true));
	}

	@Test
	void checkPermission_returnsGrantedFalse() throws Exception {
		UUID adminId = UUID.randomUUID();
		String token = AdminJwtTestTokens.accessToken(adminId, List.of("SUPPORT"), List.of());

		mockMvc.perform(get("/admin/api/v1/me/permissions/check")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
						.param("permission", AdminPermission.PRODUCT_REMOVE))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.granted").value(false));
	}

	@Test
	void checkPermission_returns400ForUnknownPermission() throws Exception {
		UUID adminId = UUID.randomUUID();
		String token = AdminJwtTestTokens.accessToken(adminId, List.of("MODERATOR"), List.of());

		mockMvc.perform(get("/admin/api/v1/me/permissions/check")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
						.param("permission", "NOT_A_REAL_PERMISSION"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.success").value(false));
	}
}
