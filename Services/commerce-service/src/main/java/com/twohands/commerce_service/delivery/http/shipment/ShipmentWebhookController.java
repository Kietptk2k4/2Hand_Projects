package com.twohands.commerce_service.delivery.http.shipment;

import com.fasterxml.jackson.databind.JsonNode;
import com.twohands.commerce_service.application.shipment.processghnwebhook.ProcessGhnWebhookResult;
import com.twohands.commerce_service.application.shipment.processghnwebhook.ProcessGhnWebhookUseCase;
import com.twohands.commerce_service.common.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/commerce/api/v1/shipments/webhooks")
public class ShipmentWebhookController {

    private final ProcessGhnWebhookUseCase processGhnWebhookUseCase;

    public ShipmentWebhookController(ProcessGhnWebhookUseCase processGhnWebhookUseCase) {
        this.processGhnWebhookUseCase = processGhnWebhookUseCase;
    }

    @PostMapping("/ghn")
    public ResponseEntity<ApiResponse<GhnWebhookResponse>> processGhnWebhook(
            @RequestBody JsonNode webhookBody,
            @RequestHeader(value = "Token", required = false) String tokenHeader,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader
    ) {
        ProcessGhnWebhookResult result = processGhnWebhookUseCase.execute(
                webhookBody,
                tokenHeader,
                authorizationHeader
        );

        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                "Da nhan GHN webhook.",
                toResponse(result)
        ));
    }

    private GhnWebhookResponse toResponse(ProcessGhnWebhookResult result) {
        return new GhnWebhookResponse(
                result.ghnOrderCode(),
                result.rawStatus(),
                result.signatureValid(),
                result.processed(),
                result.shipmentFound(),
                result.statusChanged(),
                result.previousStatus(),
                result.newStatus(),
                result.shipmentId()
        );
    }
}
