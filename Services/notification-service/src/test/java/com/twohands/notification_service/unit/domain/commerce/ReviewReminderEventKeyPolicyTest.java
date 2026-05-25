package com.twohands.notification_service.unit.domain.commerce;

import com.twohands.notification_service.domain.commerce.ReviewReminderEventKeyPolicy;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ReviewReminderEventKeyPolicyTest {

    @Test
    void build_formatsDeterministicKey() {
        assertEquals(
                "notification.review_reminder.item-42.7",
                ReviewReminderEventKeyPolicy.build("item-42", 7)
        );
    }

    @Test
    void build_throwsWhenOrderItemMissing() {
        assertThrows(IllegalArgumentException.class, () -> ReviewReminderEventKeyPolicy.build(null, 1));
    }
}
