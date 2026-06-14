package com.twohands.notification_service.application.handler;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrderCancelNotificationRecipientResolverTest {

    private static final UUID BUYER_ID = UUID.randomUUID();
    private static final UUID SELLER_ID = UUID.randomUUID();

    @Test
    void forCancelled_buyerCancel_notifiesSellersOnly() {
        var recipients = OrderCancelNotificationRecipientResolver.forCancelled(
                BUYER_ID,
                List.of(SELLER_ID),
                "BUYER",
                BUYER_ID
        );

        assertEquals(1, recipients.size());
        assertEquals(SELLER_ID, recipients.getFirst().userId());
        assertTrue(recipients.getFirst().sellerAudience());
    }

    @Test
    void forCancelled_adminConfirm_notifiesSellersOnly() {
        var recipients = OrderCancelNotificationRecipientResolver.forCancelled(
                BUYER_ID,
                List.of(SELLER_ID),
                "ADMIN",
                null
        );

        assertEquals(1, recipients.size());
        assertEquals(SELLER_ID, recipients.getFirst().userId());
        assertTrue(recipients.getFirst().sellerAudience());
    }

    @Test
    void forCancelled_sellerCancel_notifiesBuyer() {
        var recipients = OrderCancelNotificationRecipientResolver.forCancelled(
                BUYER_ID,
                List.of(SELLER_ID),
                "SELLER",
                SELLER_ID
        );

        assertEquals(1, recipients.size());
        assertEquals(BUYER_ID, recipients.getFirst().userId());
    }

    @Test
    void forPendingRefund_buyerRequest_notifiesBuyerAndSellers() {
        var recipients = OrderCancelNotificationRecipientResolver.forPendingRefund(
                BUYER_ID,
                List.of(SELLER_ID),
                "BUYER",
                BUYER_ID
        );

        assertEquals(2, recipients.size());
        assertEquals(BUYER_ID, recipients.get(0).userId());
        assertEquals(SELLER_ID, recipients.get(1).userId());
    }

    @Test
    void forPendingRefund_sellerRequest_notifiesBuyerOnly() {
        var recipients = OrderCancelNotificationRecipientResolver.forPendingRefund(
                BUYER_ID,
                List.of(SELLER_ID),
                "SELLER",
                SELLER_ID
        );

        assertEquals(1, recipients.size());
        assertEquals(BUYER_ID, recipients.getFirst().userId());
    }
}
