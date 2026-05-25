package com.twohands.notification_service.domain.commerce;

public final class ReviewReminderEligibilityPolicy {

    private ReviewReminderEligibilityPolicy() {
    }

    /**
     * Commerce remains source-of-truth; when payload marks item as reviewed, notification is skipped.
     */
    public static boolean shouldSkip(ReviewReminderNotificationContext context) {
        return context != null && context.alreadyReviewed();
    }
}
