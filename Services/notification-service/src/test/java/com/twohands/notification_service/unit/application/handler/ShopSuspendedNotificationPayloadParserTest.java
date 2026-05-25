package com.twohands.notification_service.unit.application.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.notification_service.application.handler.ShopSuspendedNotificationPayloadParser;
import com.twohands.notification_service.domain.admin.ShopSuspendedNotificationContext;
import com.twohands.notification_service.domain.notificationevent.NotificationEvent;
import com.twohands.notification_service.domain.notificationevent.NotificationEventStatus;
import com.twohands.notification_service.domain.notificationevent.NotificationSourceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ShopSuspendedNotificationPayloadParserTest {

    private static final UUID SHOP_OWNER_ID = UUID.randomUUID();
    private static final String SHOP_ID = "shop-1";

    private ShopSuspendedNotificationPayloadParser parser;

    @BeforeEach
    void setUp() {
        parser = new ShopSuspendedNotificationPayloadParser(new ObjectMapper());
    }

    @Test
    void parse_resolvesShopOwnerAndShopReference() {
        ShopSuspendedNotificationContext context = parser.parse(event(
                """
                        {
                          "shop_owner_id":"%s",
                          "shop_id":"%s",
                          "suspension_reason":"Policy violation",
                          "suspension_expires_at":"2026-12-31T00:00:00Z"
                        }
                        """.formatted(SHOP_OWNER_ID, SHOP_ID)
        ));

        assertEquals(SHOP_OWNER_ID, context.shopOwnerId());
        assertEquals(SHOP_ID, context.shopId());
        assertEquals("SHOP", context.referenceType());
        assertEquals(SHOP_ID, context.referenceId());
    }

    @Test
    void parse_throwsWhenShopOwnerMissing() {
        assertThrows(IllegalArgumentException.class, () -> parser.parse(eventWithoutShopOwner(
                """
                        {"shop_id":"%s"}
                        """.formatted(SHOP_ID)
        )));
    }

    @Test
    void parse_throwsWhenShopIdMissing() {
        assertThrows(IllegalArgumentException.class, () -> parser.parse(eventWithoutShopAggregate(
                """
                        {"shop_owner_id":"%s"}
                        """.formatted(SHOP_OWNER_ID)
        )));
    }

    private NotificationEvent event(String payload) {
        return new NotificationEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                null,
                "SHOP_SUSPENDED",
                NotificationSourceService.ADMIN,
                "SHOP",
                SHOP_ID,
                null,
                SHOP_OWNER_ID,
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

    private NotificationEvent eventWithoutShopOwner(String payload) {
        return new NotificationEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                null,
                "SHOP_SUSPENDED",
                NotificationSourceService.ADMIN,
                "SHOP",
                SHOP_ID,
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

    private NotificationEvent eventWithoutShopAggregate(String payload) {
        return new NotificationEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                null,
                "SHOP_SUSPENDED",
                NotificationSourceService.ADMIN,
                null,
                null,
                null,
                SHOP_OWNER_ID,
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
