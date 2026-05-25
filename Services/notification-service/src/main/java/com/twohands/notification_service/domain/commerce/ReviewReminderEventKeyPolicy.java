package com.twohands.notification_service.domain.commerce;

public final class ReviewReminderEventKeyPolicy {

    private static final String PREFIX = "notification.review_reminder.";

    private ReviewReminderEventKeyPolicy() {
    }

    public static String build(String orderItemId, int reminderDay) {
        if (orderItemId == null || orderItemId.isBlank()) {
            throw new IllegalArgumentException("order_item_id is required to build review reminder event key.");
        }
        if (reminderDay < 0) {
            throw new IllegalArgumentException("reminder_day must be zero or positive.");
        }
        return PREFIX + orderItemId.trim() + "." + reminderDay;
    }
}
