package com.twohands.notification_service.domain.admin;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public record ReviewHiddenNotificationContext(
        UUID reviewAuthorId,
        UUID sellerUserId,
        String reviewId,
        String hiddenReason,
        String referenceType,
        String referenceId
) {

    public List<UUID> recipientUserIds() {
        Set<UUID> recipients = new LinkedHashSet<>();
        if (reviewAuthorId != null) {
            recipients.add(reviewAuthorId);
        }
        if (sellerUserId != null) {
            recipients.add(sellerUserId);
        }
        return List.copyOf(new ArrayList<>(recipients));
    }
}
