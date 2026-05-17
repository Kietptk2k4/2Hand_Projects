package com.twohands.auth_service.integration.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RegisterIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void cleanTables() {
        jdbcTemplate.execute("DELETE FROM outbox_events");
        jdbcTemplate.execute("DELETE FROM verification_tokens");
        jdbcTemplate.execute("DELETE FROM user_settings");
        jdbcTemplate.execute("DELETE FROM user_profiles");
        jdbcTemplate.execute("DELETE FROM users");
    }

    @Test
    void registerSuccessShouldPersistAllRecords() throws Exception {
        String email = "integration_success@example.com";

        Map<String, Object> request = buildRequest(email, "Password123", "Password123");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value(email))
                .andExpect(jsonPath("$.data.status").value("PENDING_VERIFICATION"));

        Integer users = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users WHERE email_normalized = ?", Integer.class, email);
        Integer profiles = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM user_profiles up JOIN users u ON up.user_id = u.id WHERE u.email_normalized = ?",
                Integer.class,
                email
        );
        Integer settings = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM user_settings us JOIN users u ON us.user_id = u.id WHERE u.email_normalized = ?",
                Integer.class,
                email
        );
        Integer tokens = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM verification_tokens vt JOIN users u ON vt.user_id = u.id WHERE u.email_normalized = ?",
                Integer.class,
                email
        );
        Integer userCreatedOutbox = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM outbox_events WHERE event_type = 'USER_CREATED' AND status = 'PENDING'",
                Integer.class
        );
        Integer verificationOutbox = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM outbox_events WHERE event_type = 'EMAIL_VERIFICATION_REQUESTED' AND status = 'PENDING'",
                Integer.class
        );

        assertEquals(1, users);
        assertEquals(1, profiles);
        assertEquals(1, settings);
        assertEquals(1, tokens);
        assertEquals(1, userCreatedOutbox);
        assertEquals(1, verificationOutbox);
    }

    @Test
    void duplicateEmailShouldReturn409() throws Exception {
        String email = "integration_duplicate@example.com";

        Map<String, Object> request = buildRequest(email, "Password123", "Password123");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errors[0].field").value("email"));
    }

    private Map<String, Object> buildRequest(String email, String password, String confirmPassword) {
        Map<String, Object> request = new HashMap<>();
        request.put("email", email);
        request.put("password", password);
        request.put("confirm_password", confirmPassword);
        return request;
    }
}
