package com.twohands.notification_service.unit.application.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.notification_service.application.handler.ReviewReminderNotificationPayloadParser;
import com.twohands.notification_service.domain.commerce.ReviewReminderNotificationContext;
import com.twohands.notification_service.domain.notificationevent.NotificationEvent;
import com.twohands.notification_service.domain.notificationevent.NotificationEventStatus;
import com.twohands.notification_service.domain.notificationevent.NotificationSourceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReviewReminderNotificationPayloadParserTest {

    private ReviewReminderNotificationPayloadParser parser;

    @BeforeEach
    void setUp() {
        parser = new ReviewReminderNotificationPayloadParser(new ObjectMapper());
    }

    @Test
    void parse_resolvesBuyerProductReferenceAndReminderDay() {
        UUID buyerId = UUID.randomUUID();

        ReviewReminderNotificationContext context = parser.parse(sampleEvent(
                "ORDER_ITEM",
                "item-99",
                """
                        {
                          "buyer_id":"%s",
                          "order_item_id":"item-99",
                          "order_id":"order-1",
                          "order_code":"ORD-1",
                          "product_id":"prod-9",
                          "product_name":"Blue Tee",
                          "reminder_day":7
                        }
                        """.formatted(buyerId)
        ));

        assertEquals(buyerId, context.buyerId());
        assertEquals("item-99", context.orderItemId());
        assertEquals(7, context.reminderDay());
        assertEquals("PRODUCT", context.referenceType());
        assertEquals("prod-9", context.referenceId());
        assertFalse(context.alreadyReviewed());
    }

    @Test
    void parse_skipsWhenAlreadyReviewed() {
        UUID buyerId = UUID.randomUUID();

        ReviewReminderNotificationContext context = parser.parse(sampleEvent(
                "ORDER_ITEM",
                "item-1",
                """
                        {
                          "buyer_id":"%s",
                          "order_item_id":"item-1",
                          "order_id":"order-1",
                          "reminder_day":3,
                          "already_reviewed":true
                        }
                        """.formatted(buyerId)
        ));

        assertTrue(context.alreadyReviewed());
    }

    @Test
    void parse_throwsWhenOrderItemMissing() {
        assertThrows(
                IllegalArgumentException.class,
                () -> parser.parse(sampleEvent("OTHER", "x", """
                        {
                          "buyer_id":"%s",
                          "order_id":"order-1",
                          "reminder_day":1
                        }
                        """.formatted(UUID.randomUUID())))
        );
    }

    private NotificationEvent sampleEvent(String aggregateType, String aggregateId, String payload) {
        return new NotificationEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                null,
                "REVIEW_REMINDER",
                NotificationSourceService.COMMERCE,
                aggregateType,
                aggregateId,
                null,
                null,
                payload,
                NotificationEventStatus.PENDING,
                0,
                5,
                null,
                null,
                null,
                Instant.now(),
                null
        );
    }
}
