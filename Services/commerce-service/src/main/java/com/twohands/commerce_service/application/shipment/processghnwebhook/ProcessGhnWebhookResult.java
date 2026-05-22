package com.twohands.commerce_service.application.shipment.processghnwebhook;

import com.twohands.commerce_service.domain.shipment.ShipmentStatus;

import java.util.UUID;

public record ProcessGhnWebhookResult(
        String ghnOrderCode,
        String rawStatus,
        boolean signatureValid,
        boolean processed,
        boolean shipmentFound,
        boolean statusChanged,
        ShipmentStatus previousStatus,
        ShipmentStatus newStatus,
        UUID shipmentId
) {
    public static ProcessGhnWebhookResult invalidSignature(String ghnOrderCode, String rawStatus) {
        return new ProcessGhnWebhookResult(
                ghnOrderCode, rawStatus, false, false, false, false, null, null, null
        );
    }

    public static ProcessGhnWebhookResult shipmentNotFound(
            String ghnOrderCode,
            String rawStatus,
            boolean signatureValid
    ) {
        return new ProcessGhnWebhookResult(
                ghnOrderCode, rawStatus, signatureValid, false, false, false, null, null, null
        );
    }

    public static ProcessGhnWebhookResult unmappedStatus(
            String ghnOrderCode,
            String rawStatus,
            boolean signatureValid
    ) {
        return new ProcessGhnWebhookResult(
                ghnOrderCode, rawStatus, signatureValid, true, true, false, null, null, null
        );
    }

    public static ProcessGhnWebhookResult unchanged(
            String ghnOrderCode,
            String rawStatus,
            boolean signatureValid,
            UUID shipmentId,
            ShipmentStatus status
    ) {
        return new ProcessGhnWebhookResult(
                ghnOrderCode, rawStatus, signatureValid, true, true, false, status, status, shipmentId
        );
    }

    public static ProcessGhnWebhookResult ignoredTransition(
            String ghnOrderCode,
            String rawStatus,
            boolean signatureValid,
            UUID shipmentId,
            ShipmentStatus currentStatus,
            ShipmentStatus mappedStatus
    ) {
        return new ProcessGhnWebhookResult(
                ghnOrderCode,
                rawStatus,
                signatureValid,
                true,
                true,
                false,
                currentStatus,
                mappedStatus,
                shipmentId
        );
    }

    public static ProcessGhnWebhookResult updated(
            String ghnOrderCode,
            String rawStatus,
            boolean signatureValid,
            UUID shipmentId,
            ShipmentStatus previousStatus,
            ShipmentStatus newStatus
    ) {
        return new ProcessGhnWebhookResult(
                ghnOrderCode,
                rawStatus,
                signatureValid,
                true,
                true,
                true,
                previousStatus,
                newStatus,
                shipmentId
        );
    }
}
