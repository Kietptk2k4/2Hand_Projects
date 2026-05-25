package com.twohands.notification_service.unit.application.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.notification_service.application.handler.ProductRemovedNotificationPayloadParser;
import com.twohands.notification_service.domain.admin.ProductRemovedNotificationContext;
import com.twohands.notification_service.domain.notificationevent.NotificationEvent;
import com.twohands.notification_service.domain.notificationevent.NotificationEventStatus;
import com.twohands.notification_service.domain.notificationevent.NotificationSourceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ProductRemovedNotificationPayloadParserTest {

    private static final UUID SELLER_ID = UUID.randomUUID();
    private static final String PRODUCT_ID = "product-1";

    private ProductRemovedNotificationPayloadParser parser;

    @BeforeEach
    void setUp() {
        parser = new ProductRemovedNotificationPayloadParser(new ObjectMapper());
    }

    @Test
    void parse_resolvesSellerAndProductReference() {
        ProductRemovedNotificationContext context = parser.parse(event(
                """
                        {
                          "seller_user_id":"%s",
                          "product_id":"%s",
                          "removal_reason":"Policy violation"
                        }
                        """.formatted(SELLER_ID, PRODUCT_ID)
        ));

        assertEquals(SELLER_ID, context.sellerUserId());
        assertEquals(PRODUCT_ID, context.productId());
        assertEquals("Policy violation", context.removalReason());
        assertEquals("PRODUCT", context.referenceType());
        assertEquals(PRODUCT_ID, context.referenceId());
    }

    @Test
    void parse_throwsWhenSellerMissing() {
        assertThrows(IllegalArgumentException.class, () -> parser.parse(eventWithoutSeller(
                """
                        {"product_id":"%s"}
                        """.formatted(PRODUCT_ID)
        )));
    }

    @Test
    void parse_throwsWhenProductIdMissing() {
        assertThrows(IllegalArgumentException.class, () -> parser.parse(eventWithoutProductAggregate(
                """
                        {"seller_user_id":"%s"}
                        """.formatted(SELLER_ID)
        )));
    }

    private NotificationEvent event(String payload) {
        return new NotificationEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                null,
                "PRODUCT_REMOVED",
                NotificationSourceService.ADMIN,
                "PRODUCT",
                PRODUCT_ID,
                null,
                SELLER_ID,
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

    private NotificationEvent eventWithoutSeller(String payload) {
        return new NotificationEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                null,
                "PRODUCT_REMOVED",
                NotificationSourceService.ADMIN,
                "PRODUCT",
                PRODUCT_ID,
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

    private NotificationEvent eventWithoutProductAggregate(String payload) {
        return new NotificationEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                null,
                "PRODUCT_REMOVED",
                NotificationSourceService.ADMIN,
                null,
                null,
                null,
                SELLER_ID,
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
