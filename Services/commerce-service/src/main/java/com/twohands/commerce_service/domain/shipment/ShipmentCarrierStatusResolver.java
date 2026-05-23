package com.twohands.commerce_service.domain.shipment;

import org.springframework.util.StringUtils;

import java.util.List;

public final class ShipmentCarrierStatusResolver {

    private ShipmentCarrierStatusResolver() {
    }

    public static String resolve(
            List<GhnWebhookSummary> carrierWebhookEvents,
            List<ShipmentStatusHistoryEntry> statusHistory
    ) {
        if (carrierWebhookEvents != null && !carrierWebhookEvents.isEmpty()) {
            String fromWebhook = carrierWebhookEvents.getFirst().carrierStatus();
            if (StringUtils.hasText(fromWebhook)) {
                return fromWebhook;
            }
        }
        if (statusHistory == null || statusHistory.isEmpty()) {
            return null;
        }
        for (int i = statusHistory.size() - 1; i >= 0; i--) {
            String rawStatus = statusHistory.get(i).rawStatus();
            if (StringUtils.hasText(rawStatus)) {
                return rawStatus;
            }
        }
        return null;
    }
}
