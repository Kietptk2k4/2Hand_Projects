package com.twohands.auth_service.integration.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.auth_service.domain.outbox.OutboxEvent;
import com.twohands.auth_service.domain.outbox.OutboxEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RegisterRollbackIntegrationTest {

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
    void shouldRollbackWhenOutboxWriteFails() throws Exception {
        String email = "integration_rollback@example.com";

        Map<String, Object> request = new HashMap<>();
        request.put("email", email);
        request.put("password", "Password123");
        request.put("confirm_password", "Password123");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());

        Integer users = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users WHERE email_normalized = ?", Integer.class, email);
        Integer profiles = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM user_profiles up JOIN users u ON up.user_id = u.id WHERE u.email_normalized = ?",
                Integer.class,
                email
        );

        assertEquals(0, users);
        assertEquals(0, profiles);
    }

    @TestConfiguration
    static class FailingOutboxConfig {
        @Bean
        @Primary
        OutboxEventRepository outboxEventRepository() {
            return new OutboxEventRepository() {
                @Override
                public OutboxEvent save(OutboxEvent event) {
                    throw new RuntimeException("Forced outbox failure for rollback test");
                }
            };
        }
    }
}
