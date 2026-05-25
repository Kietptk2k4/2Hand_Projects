package com.twohands.notification_service.unit.application.consume;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.notification_service.application.consume.DomainEventMessageParser;
import com.twohands.notification_service.application.consume.DomainEventTopicResolver;
import com.twohands.notification_service.application.consume.InvalidDomainEventException;
import com.twohands.notification_service.domain.notificationevent.NotificationEventTypeAliasResolver;
import com.twohands.notification_service.domain.notificationevent.NotificationSourceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DomainEventMessageParserTest {

    private DomainEventMessageParser parser;

    @BeforeEach
    void setUp() {
        parser = new DomainEventMessageParser(
                new ObjectMapper(),
                new DomainEventTopicResolver(),
                new NotificationEventTypeAliasResolver()
        );
    }

    @Test
    void parse_validEnvelopeFromSocialTopic() {
        UUID eventId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();
        UUID recipientId = UUID.randomUUID();

        String json = """
                {
                  "event_id": "%s",
                  "event_type": "POST_LIKED",
                  "source_service": "SOCIAL",
                  "event_key": "social.post.post-id.liked",
                  "aggregate_type": "POST",
                  "aggregate_id": "post-id",
                  "actor_id": "%s",
                  "recipient_user_ids": ["%s"],
                  "occurred_at": "2026-05-20T16:00:00Z",
                  "payload": {"post_id":"post-id","actor_name":"Alice"}
                }
                """.formatted(eventId, actorId, recipientId);

        var command = parser.parse(json, "social.post.liked");

        assertEquals(eventId, command.eventId());
        assertEquals("POST_LIKED", command.eventType());
        assertEquals(NotificationSourceService.SOCIAL, command.sourceService());
        assertEquals(recipientId, command.recipientUserId());
        assertEquals(actorId, command.actorId());
    }

    @Test
    void parse_resolvesBuyerIdAsRecipientForCommercePayment() {
        UUID eventId = UUID.randomUUID();
        UUID buyerId = UUID.randomUUID();

        String json = """
                {
                  "event_id": "%s",
                  "payload": {
                    "payment_id": "pay-1",
                    "order_id": "order-1",
                    "buyer_id": "%s"
                  }
                }
                """.formatted(eventId, buyerId);

        var command = parser.parse(json, "commerce.payment.paid");

        assertEquals(buyerId, command.recipientUserId());
        assertEquals("PAYMENT_SUCCESS", command.eventType());
    }

    @Test
    void parse_resolvesShipmentCreatedAliasFromTopicFallback() {
        UUID eventId = UUID.randomUUID();
        UUID buyerId = UUID.randomUUID();

        String json = """
                {
                  "event_id": "%s",
                  "payload": {
                    "shipment_id": "ship-1",
                    "order_id": "order-1",
                    "buyer_id": "%s"
                  }
                }
                """.formatted(eventId, buyerId);

        var command = parser.parse(json, "commerce.shipment.created");

        assertEquals("SHIPMENT_CREATED", command.eventType());
        assertEquals(buyerId, command.recipientUserId());
        assertEquals(NotificationSourceService.COMMERCE, command.sourceService());
    }

    @Test
    void parse_resolvesShipmentShippedAliasFromTopicFallback() {
        UUID eventId = UUID.randomUUID();
        UUID buyerId = UUID.randomUUID();

        String json = """
                {
                  "event_id": "%s",
                  "payload": {
                    "shipment_id": "ship-1",
                    "order_id": "order-1",
                    "buyer_id": "%s"
                  }
                }
                """.formatted(eventId, buyerId);

        var command = parser.parse(json, "commerce.shipment.shipped");

        assertEquals("SHIPMENT_SHIPPED", command.eventType());
        assertEquals(buyerId, command.recipientUserId());
        assertEquals(NotificationSourceService.COMMERCE, command.sourceService());
    }

    @Test
    void parse_resolvesShipmentDeliveredAliasFromTopicFallback() {
        UUID eventId = UUID.randomUUID();
        UUID buyerId = UUID.randomUUID();

        String json = """
                {
                  "event_id": "%s",
                  "payload": {
                    "shipment_id": "ship-1",
                    "order_id": "order-1",
                    "buyer_id": "%s"
                  }
                }
                """.formatted(eventId, buyerId);

        var command = parser.parse(json, "commerce.shipment.delivered");

        assertEquals("SHIPMENT_DELIVERED", command.eventType());
        assertEquals(buyerId, command.recipientUserId());
        assertEquals(NotificationSourceService.COMMERCE, command.sourceService());
    }

    @Test
    void parse_resolvesReviewHiddenFromAdminTopicFallback() {
        UUID eventId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();

        String json = """
                {
                  "event_id": "%s",
                  "payload": {
                    "review_id": "review-1",
                    "review_author_id": "%s"
                  }
                }
                """.formatted(eventId, authorId);

        var command = parser.parse(json, "admin.review.hidden");

        assertEquals("REVIEW_HIDDEN", command.eventType());
        assertEquals(authorId, command.recipientUserId());
        assertEquals(NotificationSourceService.ADMIN, command.sourceService());
    }

    @Test
    void parse_resolvesProductRemovedFromAdminTopicFallback() {
        UUID eventId = UUID.randomUUID();
        UUID sellerId = UUID.randomUUID();

        String json = """
                {
                  "event_id": "%s",
                  "payload": {
                    "product_id": "product-1",
                    "seller_user_id": "%s"
                  }
                }
                """.formatted(eventId, sellerId);

        var command = parser.parse(json, "admin.product.removed");

        assertEquals("PRODUCT_REMOVED", command.eventType());
        assertEquals(sellerId, command.recipientUserId());
        assertEquals(NotificationSourceService.ADMIN, command.sourceService());
    }

    @Test
    void parse_resolvesUserRestrictedFromAdminTopicFallback() {
        UUID eventId = UUID.randomUUID();
        UUID targetUserId = UUID.randomUUID();

        String json = """
                {
                  "event_id": "%s",
                  "payload": {
                    "user_id": "%s",
                    "enforcement_id": "enforcement-1"
                  }
                }
                """.formatted(eventId, targetUserId);

        var command = parser.parse(json, "admin.user.restricted");

        assertEquals("USER_RESTRICTED", command.eventType());
        assertEquals(targetUserId, command.recipientUserId());
        assertEquals(NotificationSourceService.ADMIN, command.sourceService());
    }

    @Test
    void parse_resolvesUserSuspendedFromAdminTopicFallback() {
        UUID eventId = UUID.randomUUID();
        UUID targetUserId = UUID.randomUUID();

        String json = """
                {
                  "event_id": "%s",
                  "payload": {
                    "user_id": "%s",
                    "enforcement_id": "enforcement-1"
                  }
                }
                """.formatted(eventId, targetUserId);

        var command = parser.parse(json, "admin.user.suspended");

        assertEquals("USER_SUSPENDED", command.eventType());
        assertEquals(targetUserId, command.recipientUserId());
        assertEquals(NotificationSourceService.ADMIN, command.sourceService());
    }

    @Test
    void parse_resolvesOrderCompletedAliasFromTopicFallback() {
        UUID eventId = UUID.randomUUID();
        UUID buyerId = UUID.randomUUID();

        String json = """
                {
                  "event_id": "%s",
                  "payload": {
                    "order_id": "order-1",
                    "buyer_id": "%s"
                  }
                }
                """.formatted(eventId, buyerId);

        var command = parser.parse(json, "commerce.order.completed");

        assertEquals("ORDER_COMPLETED", command.eventType());
        assertEquals(buyerId, command.recipientUserId());
        assertEquals(NotificationSourceService.COMMERCE, command.sourceService());
    }

    @Test
    void parse_resolvesPaymentFailedAliasFromTopicFallback() {
        UUID eventId = UUID.randomUUID();
        UUID buyerId = UUID.randomUUID();

        String json = """
                {
                  "event_id": "%s",
                  "payload": {
                    "payment_id": "pay-1",
                    "order_id": "order-1",
                    "buyer_id": "%s"
                  }
                }
                """.formatted(eventId, buyerId);

        var command = parser.parse(json, "commerce.payment.failed");

        assertEquals("PAYMENT_FAILED", command.eventType());
        assertEquals(buyerId, command.recipientUserId());
        assertEquals(NotificationSourceService.COMMERCE, command.sourceService());
    }

    @Test
    void parse_resolvesCommerceAliasFromTopicFallback() {
        UUID eventId = UUID.randomUUID();

        String json = """
                {
                  "event_id": "%s",
                  "payload": {"order_id":"order-1","buyer_id":"%s"}
                }
                """.formatted(eventId, UUID.randomUUID());

        var command = parser.parse(json, "commerce.payment.paid");

        assertEquals("PAYMENT_SUCCESS", command.eventType());
        assertEquals(NotificationSourceService.COMMERCE, command.sourceService());
    }

    @Test
    void parse_rejectsMissingEventId() {
        assertThrows(InvalidDomainEventException.class, () -> parser.parse("{}", "social.post.liked"));
    }
}
