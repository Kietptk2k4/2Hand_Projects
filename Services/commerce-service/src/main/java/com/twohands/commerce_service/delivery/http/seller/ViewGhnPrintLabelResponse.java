package com.twohands.commerce_service.delivery.http.seller;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.twohands.commerce_service.application.shipment.viewghnprintlabel.ViewGhnPrintLabelUseCase.ViewGhnPrintLabelResult;

import java.util.UUID;

public record ViewGhnPrintLabelResponse(
        @JsonProperty("shipment_id") UUID shipmentId,
        @JsonProperty("ghn_order_code") String ghnOrderCode,
        @JsonProperty("format") String format,
        @JsonProperty("print_token") String printToken,
        @JsonProperty("print_url") String printUrl,
        @JsonProperty("expires_in_minutes") int expiresInMinutes
) {
    public static ViewGhnPrintLabelResponse from(ViewGhnPrintLabelResult result) {
        return new ViewGhnPrintLabelResponse(
                result.shipmentId(),
                result.ghnOrderCode(),
                result.format(),
                result.printToken(),
                result.printUrl(),
                result.expiresInMinutes()
        );
    }
}
