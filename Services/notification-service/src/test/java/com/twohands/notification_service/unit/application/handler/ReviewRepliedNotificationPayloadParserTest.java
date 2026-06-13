package com.twohands.notification_service.unit.application.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.notification_service.application.handler.ReviewRepliedNotificationPayloadParser;
import com.twohands.notification_service.domain.commerce.ReviewRepliedNotificationContext;
import com.twohands.notification_service.domain.notificationevent.NotificationEvent;
import com.twohands.notification_service.domain.notificationevent.NotificationEventStatus;
import com.twohands.notification_service.domain.notificationevent.NotificationSourceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ReviewRepliedNotificationPayloadParserTest {

    private static final UUID BUYER_ID = UUID.randomUUID();
    private static final UUID SELLER_ID = UUID.randomUUID();
    private static final UUID REVIEW_ID = UUID.randomUUID();
    private static final UUID PRODUCT_ID = UUID.randomUUID();

    private ReviewRepliedNotificationPayloadParser parser;

    @BeforeEach
    void setUp() {
        parser = new ReviewRepliedNotificationPayloadParser(new ObjectMapper());
    }

    @Test
    void parse_readsBuyerSellerReviewAndProductIds() {
        ReviewRepliedNotificationContext context = parser.parse(event("""
                {
                  "buyer_id":"%s",
                  "seller_id":"%s",
                  "review_id":"%s",
                  "product_id":"%s"
                }
                """.formatted(BUYER_ID, SELLER_ID, REVIEW_ID, PRODUCT_ID)));

        assertEquals(BUYER_ID, context.buyerId());
        assertEquals(SELLER_ID, context.sellerId());
        assertEquals(REVIEW_ID, context.reviewId());
        assertEquals(PRODUCT_ID, context.productId());
    }

    @Test
    void parse_requiresBuyerId() {
        assertThrows(
                IllegalArgumentException.class,
                () -> parser.parse(event("""
                        {"seller_id":"%s","review_id":"%s"}
                        """.formatted(SELLER_ID, REVIEW_ID)))
        );
    }

    private NotificationEvent event(String payload) {
        return new NotificationEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "notification.review.replied." + REVIEW_ID,
                "REVIEW_REPLIED",
                NotificationSourceService.COMMERCE,
                "REVIEW",
                REVIEW_ID.toString(),
                SELLER_ID,
                BUYER_ID,
                payload,
                NotificationEventStatus.PENDING,
                0,
                5,
                null,
                null,
                null,
                Instant.parse("2026-05-21T10:00:00Z"),
                null
        );
    }
}
