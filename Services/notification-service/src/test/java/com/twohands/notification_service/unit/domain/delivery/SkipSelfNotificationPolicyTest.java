package com.twohands.notification_service.unit.domain.delivery;

import com.twohands.notification_service.domain.delivery.SkipSelfNotificationOutcome;
import com.twohands.notification_service.domain.delivery.SkipSelfNotificationPolicy;
import com.twohands.notification_service.domain.notificationevent.NotificationSourceService;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

class SkipSelfNotificationPolicyTest {

    @Test
    void appliesTo_onlySocialInteractionEvents() {
        assertTrue(SkipSelfNotificationPolicy.appliesTo("POST_LIKED"));
        assertTrue(SkipSelfNotificationPolicy.appliesTo("USER_FOLLOWED"));
        assertFalse(SkipSelfNotificationPolicy.appliesTo("PAYMENT_SUCCESS"));
        assertFalse(SkipSelfNotificationPolicy.appliesTo("USER_SUSPENDED"));
    }

    @Test
    void evaluate_skipsWhenActorEqualsRecipientForSocialEvent() {
        UUID userId = UUID.randomUUID();

        SkipSelfNotificationOutcome outcome = SkipSelfNotificationPolicy.evaluate(
                "POST_LIKED",
                NotificationSourceService.SOCIAL,
                userId,
                userId
        );

        assertEquals(SkipSelfNotificationOutcome.SKIP, outcome);
    }

    @Test
    void evaluate_proceedsWhenActorDiffersFromRecipient() {
        SkipSelfNotificationOutcome outcome = SkipSelfNotificationPolicy.evaluate(
                "COMMENT_CREATED",
                NotificationSourceService.SOCIAL,
                UUID.randomUUID(),
                UUID.randomUUID()
        );

        assertEquals(SkipSelfNotificationOutcome.PROCEED, outcome);
    }

    @Test
    void evaluate_doesNotApplyToCommerceEvents() {
        UUID userId = UUID.randomUUID();

        SkipSelfNotificationOutcome outcome = SkipSelfNotificationPolicy.evaluate(
                "PAYMENT_SUCCESS",
                NotificationSourceService.COMMERCE,
                userId,
                userId
        );

        assertEquals(SkipSelfNotificationOutcome.PROCEED, outcome);
    }

    @Test
    void evaluate_returnsMissingActorForSelfSkipSocialEvent() {
        SkipSelfNotificationOutcome outcome = SkipSelfNotificationPolicy.evaluate(
                "USER_FOLLOWED",
                NotificationSourceService.SOCIAL,
                null,
                UUID.randomUUID()
        );

        assertEquals(SkipSelfNotificationOutcome.MISSING_ACTOR, outcome);
    }

    @Test
    void evaluate_doesNotRequireActorForNonSelfSkipEvent() {
        SkipSelfNotificationOutcome outcome = SkipSelfNotificationPolicy.evaluate(
                "ORDER_CREATED",
                NotificationSourceService.COMMERCE,
                null,
                UUID.randomUUID()
        );

        assertEquals(SkipSelfNotificationOutcome.PROCEED, outcome);
    }
}
