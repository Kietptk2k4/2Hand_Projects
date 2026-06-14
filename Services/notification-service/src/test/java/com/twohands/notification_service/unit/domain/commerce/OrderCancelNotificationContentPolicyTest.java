package com.twohands.notification_service.unit.domain.commerce;

import com.twohands.notification_service.domain.commerce.OrderCancelNotificationContentPolicy;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrderCancelNotificationContentPolicyTest {

    @Test
    void supportsReasonInContent_forCancelEventTypes() {
        assertTrue(OrderCancelNotificationContentPolicy.supportsReasonInContent("ORDER_CANCELLED"));
        assertTrue(OrderCancelNotificationContentPolicy.supportsReasonInContent("ORDER_CANCEL_PENDING_REFUND"));
        assertFalse(OrderCancelNotificationContentPolicy.supportsReasonInContent("ORDER_CREATED"));
    }

    @Test
    void appendReason_addsSuffixWhenMissing() {
        assertEquals(
                "Người bán đã hủy đơn hàng. Lý do: Hết hàng",
                OrderCancelNotificationContentPolicy.appendReason(
                        "Người bán đã hủy đơn hàng.",
                        "Hết hàng"
                )
        );
    }

    @Test
    void appendReason_skipsWhenReasonAlreadyPresent() {
        String base = "Người bán đã hủy đơn hàng. Lý do: Hết hàng";
        assertEquals(base, OrderCancelNotificationContentPolicy.appendReason(base, "Hết hàng"));
    }

    @Test
    void appendReason_returnsBaseWhenReasonBlank() {
        assertEquals("Template", OrderCancelNotificationContentPolicy.appendReason("Template", "  "));
    }
}
