package com.twohands.notification_service.application.handler;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

final class OrderCancelNotificationRecipientResolver {

    record Recipient(UUID userId, boolean sellerAudience) {
    }

    private OrderCancelNotificationRecipientResolver() {
    }

    static List<Recipient> forCancelled(
            UUID buyerId,
            List<UUID> sellerIds,
            String cancelledBy,
            UUID actorUserId
    ) {
        String by = normalizeRole(cancelledBy);
        Set<Recipient> recipients = new LinkedHashSet<>();

        if ("BUYER".equals(by)) {
            addSellersExceptActor(recipients, sellerIds, actorUserId);
            return List.copyOf(recipients);
        }

        if ("ADMIN".equals(by)) {
            addSellersExceptActor(recipients, sellerIds, actorUserId);
            return List.copyOf(recipients);
        }

        if (buyerId != null && !buyerId.equals(actorUserId)) {
            recipients.add(new Recipient(buyerId, false));
        }

        return List.copyOf(recipients);
    }

    static List<Recipient> forPendingRefund(
            UUID buyerId,
            List<UUID> sellerIds,
            String requestedBy,
            UUID actorUserId
    ) {
        String by = normalizeRole(requestedBy);
        Set<Recipient> recipients = new LinkedHashSet<>();

        if ("SELLER".equals(by)) {
            if (buyerId != null && !buyerId.equals(actorUserId)) {
                recipients.add(new Recipient(buyerId, false));
            }
            return List.copyOf(recipients);
        }

        if ("BUYER".equals(by)) {
            if (buyerId != null) {
                recipients.add(new Recipient(buyerId, false));
            }
            addSellersExceptActor(recipients, sellerIds, actorUserId);
        }

        return List.copyOf(recipients);
    }

    private static void addSellersExceptActor(Set<Recipient> recipients, List<UUID> sellerIds, UUID actorUserId) {
        if (sellerIds == null || sellerIds.isEmpty()) {
            return;
        }
        for (UUID sellerId : sellerIds) {
            if (sellerId == null || sellerId.equals(actorUserId)) {
                continue;
            }
            recipients.add(new Recipient(sellerId, true));
        }
    }

    private static String normalizeRole(String role) {
        if (role == null || role.isBlank()) {
            return "";
        }
        return role.trim().toUpperCase(Locale.ROOT);
    }
}
