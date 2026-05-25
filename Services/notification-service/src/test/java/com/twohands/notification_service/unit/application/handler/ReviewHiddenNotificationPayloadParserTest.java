package com.twohands.notification_service.unit.application.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.notification_service.application.handler.ReviewHiddenNotificationPayloadParser;
import com.twohands.notification_service.domain.admin.ReviewHiddenNotificationContext;
import com.twohands.notification_service.domain.notificationevent.NotificationEvent;
import com.twohands.notification_service.domain.notificationevent.NotificationEventStatus;
import com.twohands.notification_service.domain.notificationevent.NotificationSourceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ReviewHiddenNotificationPayloadParserTest {

    private static final UUID AUTHOR_ID = UUID.randomUUID();
    private static final UUID SELLER_ID = UUID.randomUUID();
    private static final String REVIEW_ID = "review-1";

    private ReviewHiddenNotificationPayloadParser parser;

    @BeforeEach
    void setUp() {
        parser = new ReviewHiddenNotificationPayloadParser(new ObjectMapper());
    }

    @Test
    void parse_resolvesAuthorSellerAndReviewReference() {
        ReviewHiddenNotificationContext context = parser.parse(event(
                """
                        {
                          "review_author_id":"%s",
                          "seller_user_id":"%s",
                          "review_id":"%s",
                          "hidden_reason":"Policy violation"
                        }
                        """.formatted(AUTHOR_ID, SELLER_ID, REVIEW_ID)
        ));

        assertEquals(AUTHOR_ID, context.reviewAuthorId());
        assertEquals(SELLER_ID, context.sellerUserId());
        assertEquals(2, context.recipientUserIds().size());
        assertEquals("REVIEW", context.referenceType());
        assertEquals(REVIEW_ID, context.referenceId());
    }

    @Test
    void parse_allowsSellerOnlyRecipient() {
        ReviewHiddenNotificationContext context = parser.parse(eventWithoutRecipientUserId(
                """
                        {
                          "seller_user_id":"%s",
                          "review_id":"%s"
                        }
                        """.formatted(SELLER_ID, REVIEW_ID)
        ));

        assertEquals(null, context.reviewAuthorId());
        assertEquals(SELLER_ID, context.sellerUserId());
        assertEquals(1, context.recipientUserIds().size());
    }

    @Test
    void parse_throwsWhenNoRecipient() {
        assertThrows(IllegalArgumentException.class, () -> parser.parse(eventWithoutRecipientUserId(
                """
                        {"review_id":"%s"}
                        """.formatted(REVIEW_ID)
        )));
    }

    @Test
    void parse_throwsWhenReviewIdMissing() {
        assertThrows(IllegalArgumentException.class, () -> parser.parse(eventWithoutReviewAggregate(
                """
                        {"review_author_id":"%s"}
                        """.formatted(AUTHOR_ID)
        )));
    }

    private NotificationEvent event(String payload) {
        return new NotificationEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                null,
                "REVIEW_HIDDEN",
                NotificationSourceService.ADMIN,
                "REVIEW",
                REVIEW_ID,
                null,
                AUTHOR_ID,
                payload,
                NotificationEventStatus.PROCESSING,
                0,
                5,
                null,
                Instant.now(),
                "worker",
                Instant.now(),
                null
        );
    }

    private NotificationEvent eventWithoutRecipientUserId(String payload) {
        return new NotificationEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                null,
                "REVIEW_HIDDEN",
                NotificationSourceService.ADMIN,
                "REVIEW",
                REVIEW_ID,
                null,
                null,
                payload,
                NotificationEventStatus.PROCESSING,
                0,
                5,
                null,
                Instant.now(),
                "worker",
                Instant.now(),
                null
        );
    }

    private NotificationEvent eventWithoutReviewAggregate(String payload) {
        return new NotificationEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                null,
                "REVIEW_HIDDEN",
                NotificationSourceService.ADMIN,
                null,
                null,
                null,
                AUTHOR_ID,
                payload,
                NotificationEventStatus.PROCESSING,
                0,
                5,
                null,
                Instant.now(),
                "worker",
                Instant.now(),
                null
        );
    }
}
