package com.twohands.notification_service.integration.ingest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class StoreNotificationEventIntegrationTest {

    private static final String INTERNAL_EVENTS_URL = "/api/v1/notification/internal/events";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void cleanTable() {
        jdbcTemplate.execute("DELETE FROM notification_events");
    }

    @Test
    void ingest_storesPendingEvent() throws Exception {
        UUID sourceEventId = UUID.randomUUID();

        mockMvc.perform(post(INTERNAL_EVENTS_URL)
                        .header("X-Internal-Api-Key", "test-internal-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "sourceEventId": "%s",
                                  "eventType": "POST_LIKED",
                                  "sourceService": "SOCIAL",
                                  "payload": "{\\"actorName\\":\\"Alice\\",\\"password\\":\\"secret\\"}"
                                }
                                """.formatted(sourceEventId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.duplicate").value(false));

        assertEquals(1, countEvents());
        assertEquals("PENDING", queryStatus(sourceEventId));
        assertEquals(0, queryRetryCount(sourceEventId));
        String payload = queryPayload(sourceEventId);
        assertTrue(payload.contains("***REDACTED***"));
        assertFalse(payload.contains("secret"));
    }

    @Test
    void ingest_duplicateSourceEventIsIdempotent() throws Exception {
        UUID sourceEventId = UUID.randomUUID();
        String body = """
                {
                  "sourceEventId": "%s",
                  "eventType": "POST_LIKED",
                  "sourceService": "SOCIAL",
                  "payload": "{}"
                }
                """.formatted(sourceEventId);

        mockMvc.perform(post(INTERNAL_EVENTS_URL)
                        .header("X-Internal-Api-Key", "test-internal-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());

        mockMvc.perform(post(INTERNAL_EVENTS_URL)
                        .header("X-Internal-Api-Key", "test-internal-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.duplicate").value(true));

        assertEquals(1, countEvents());
    }

    @Test
    void ingest_duplicateEventKeyIsIdempotent() throws Exception {
        String body = """
                {
                  "eventKey": "social.post.post-id.liked",
                  "eventType": "POST_LIKED",
                  "sourceService": "SOCIAL",
                  "payload": "{}"
                }
                """;

        mockMvc.perform(post(INTERNAL_EVENTS_URL)
                        .header("X-Internal-Api-Key", "test-internal-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());

        mockMvc.perform(post(INTERNAL_EVENTS_URL)
                        .header("X-Internal-Api-Key", "test-internal-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.duplicate").value(true));

        assertEquals(1, countEvents());
    }

    @Test
    void ingest_rejectsMissingIdempotencyKey() throws Exception {
        mockMvc.perform(post(INTERNAL_EVENTS_URL)
                        .header("X-Internal-Api-Key", "test-internal-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "eventType": "POST_LIKED",
                                  "sourceService": "SOCIAL",
                                  "payload": "{}"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));

        assertEquals(0, countEvents());
    }

    private int countEvents() {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM notification_events", Integer.class);
        return count == null ? 0 : count;
    }

    private String queryStatus(UUID sourceEventId) {
        return jdbcTemplate.queryForObject(
                "SELECT status FROM notification_events WHERE source_event_id = ?",
                String.class,
                sourceEventId
        );
    }

    private int queryRetryCount(UUID sourceEventId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT retry_count FROM notification_events WHERE source_event_id = ?",
                Integer.class,
                sourceEventId
        );
        return count == null ? -1 : count;
    }

    private String queryPayload(UUID sourceEventId) {
        return jdbcTemplate.queryForObject(
                "SELECT payload FROM notification_events WHERE source_event_id = ?",
                String.class,
                sourceEventId
        );
    }
}
