package com.twohands.notification_service.integration.handler;

import com.twohands.notification_service.application.worker.ProcessNotificationEventCommand;
import com.twohands.notification_service.application.worker.ProcessNotificationEventOutcome;
import com.twohands.notification_service.application.worker.ProcessNotificationEventUseCase;
import com.twohands.notification_service.domain.notificationevent.NotificationEvent;
import com.twohands.notification_service.domain.notificationevent.NotificationEventRepository;
import com.twohands.notification_service.domain.notificationevent.NotificationEventStatus;
import com.twohands.notification_service.domain.notificationevent.NotificationSourceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
class CommentRepliedNotificationIntegrationTest {

    @Autowired
    private ProcessNotificationEventUseCase processNotificationEventUseCase;

    @Autowired
    private NotificationEventRepository notificationEventRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void cleanTables() {
        jdbcTemplate.execute("DELETE FROM user_notifications");
        jdbcTemplate.execute("DELETE FROM notification_events");
    }

    @Test
    void process_createsInAppNotificationForExternalReply() {
        UUID parentAuthorId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();
        UUID eventId = insertCommentRepliedEvent(actorId, parentAuthorId, "parent-comment-10", "reply-comment-20", false);

        var result = processNotificationEventUseCase.execute(new ProcessNotificationEventCommand(eventId));

        assertEquals(ProcessNotificationEventOutcome.COMPLETED, result.outcome());
        assertEquals(1, countNotifications(eventId, parentAuthorId));
        assertEquals("COMMENT", queryReferenceType(eventId, parentAuthorId));
        assertEquals("parent-comment-10", queryReferenceId(eventId, parentAuthorId));
    }

    @Test
    void process_completesWithoutNotificationOnSelfReply() {
        UUID userId = UUID.randomUUID();
        UUID eventId = insertCommentRepliedEvent(userId, userId, "parent-comment-self", "reply-comment-self", false);

        var result = processNotificationEventUseCase.execute(new ProcessNotificationEventCommand(eventId));

        assertEquals(ProcessNotificationEventOutcome.COMPLETED, result.outcome());
        assertEquals(0, countNotifications(eventId, userId));
    }

    @Test
    void process_marksEventFailedWhenParentCommentAuthorMissing() {
        UUID actorId = UUID.randomUUID();
        UUID eventId = insertCommentRepliedEvent(actorId, null, "parent-comment-1", "reply-comment-1", true);

        var result = processNotificationEventUseCase.execute(new ProcessNotificationEventCommand(eventId));

        assertEquals(ProcessNotificationEventOutcome.FAILED, result.outcome());
        assertEquals("FAILED", queryEventStatus(eventId));
        assertEquals(0, countNotifications(eventId, actorId));
    }

    @Test
    void process_isIdempotentForDuplicateDelivery() {
        UUID parentAuthorId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();
        UUID eventId = insertCommentRepliedEvent(actorId, parentAuthorId, "parent-dup", "reply-dup", false);

        processNotificationEventUseCase.execute(new ProcessNotificationEventCommand(eventId));
        notificationEventRepository.save(reopenForReprocess(eventId));
        processNotificationEventUseCase.execute(new ProcessNotificationEventCommand(eventId));

        assertEquals(1, countNotifications(eventId, parentAuthorId));
    }

    private UUID insertCommentRepliedEvent(
            UUID actorId,
            UUID parentAuthorId,
            String parentCommentId,
            String replyCommentId,
            boolean omitParentAuthor
    ) {
        UUID eventId = UUID.randomUUID();
        String payload;
        if (omitParentAuthor) {
            payload = """
                    {"actor_id":"%s","parent_comment_id":"%s","comment_id":"%s"}
                    """.formatted(actorId, parentCommentId, replyCommentId);
        } else {
            payload = """
                    {"actor_id":"%s","parent_comment_author_id":"%s","parent_comment_id":"%s","comment_id":"%s"}
                    """.formatted(actorId, parentAuthorId, parentCommentId, replyCommentId);
        }

        notificationEventRepository.save(new NotificationEvent(
                eventId,
                UUID.randomUUID(),
                null,
                "COMMENT_REPLIED",
                NotificationSourceService.SOCIAL,
                "COMMENT",
                replyCommentId,
                actorId,
                parentAuthorId,
                payload,
                NotificationEventStatus.PENDING,
                0,
                5,
                null,
                null,
                null,
                Instant.now(),
                null
        ));
        return eventId;
    }

    private NotificationEvent reopenForReprocess(UUID eventId) {
        NotificationEvent existing = notificationEventRepository.findById(eventId).orElseThrow();
        return new NotificationEvent(
                existing.id(),
                existing.sourceEventId(),
                existing.eventKey(),
                existing.eventType(),
                existing.sourceService(),
                existing.aggregateType(),
                existing.aggregateId(),
                existing.actorId(),
                existing.recipientUserId(),
                existing.payload(),
                NotificationEventStatus.PENDING,
                0,
                existing.maxRetryCount(),
                null,
                null,
                null,
                existing.createdAt(),
                null
        );
    }

    private String queryEventStatus(UUID eventId) {
        return jdbcTemplate.queryForObject(
                "SELECT status FROM notification_events WHERE id = ?",
                String.class,
                eventId
        );
    }

    private int countNotifications(UUID eventId, UUID userId) {
        Integer count = jdbcTemplate.queryForObject(
                """
                        SELECT COUNT(*) FROM user_notifications
                        WHERE notification_event_id = ? AND user_id = ?
                        """,
                Integer.class,
                eventId,
                userId
        );
        return count == null ? 0 : count;
    }

    private String queryReferenceType(UUID eventId, UUID userId) {
        return jdbcTemplate.queryForObject(
                """
                        SELECT reference_type FROM user_notifications
                        WHERE notification_event_id = ? AND user_id = ?
                        """,
                String.class,
                eventId,
                userId
        );
    }

    private String queryReferenceId(UUID eventId, UUID userId) {
        return jdbcTemplate.queryForObject(
                """
                        SELECT reference_id FROM user_notifications
                        WHERE notification_event_id = ? AND user_id = ?
                        """,
                String.class,
                eventId,
                userId
        );
    }
}
