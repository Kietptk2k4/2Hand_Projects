package com.twohands.commerce_service.application.shipment.processghnwebhook;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.commerce_service.application.shipment.common.ShipmentStatusChangedOutboxService;
import com.twohands.commerce_service.domain.outbox.OutboxEventRepository;
import com.twohands.commerce_service.domain.shipment.GhnShipmentStatusMapper;
import com.twohands.commerce_service.domain.shipment.GhnShipmentStatusPolicy;
import com.twohands.commerce_service.domain.shipment.GhnWebhookLogRepository;
import com.twohands.commerce_service.domain.shipment.ProcessGhnWebhookRepository;
import com.twohands.commerce_service.domain.shipment.SellerShipmentRecord;
import com.twohands.commerce_service.domain.shipment.ShipmentStatus;
import com.twohands.commerce_service.infrastructure.ghn.GhnWebhookSignatureVerifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class ProcessGhnWebhookUseCase {

    private static final Logger log = LoggerFactory.getLogger(ProcessGhnWebhookUseCase.class);

    private final GhnWebhookSignatureVerifier signatureVerifier;
    private final GhnWebhookLogRepository ghnWebhookLogRepository;
    private final ProcessGhnWebhookRepository processGhnWebhookRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final ShipmentStatusChangedOutboxService shipmentStatusChangedOutboxService;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    public ProcessGhnWebhookUseCase(
            GhnWebhookSignatureVerifier signatureVerifier,
            GhnWebhookLogRepository ghnWebhookLogRepository,
            ProcessGhnWebhookRepository processGhnWebhookRepository,
            OutboxEventRepository outboxEventRepository,
            ShipmentStatusChangedOutboxService shipmentStatusChangedOutboxService,
            ObjectMapper objectMapper,
            Clock clock
    ) {
        this.signatureVerifier = signatureVerifier;
        this.ghnWebhookLogRepository = ghnWebhookLogRepository;
        this.processGhnWebhookRepository = processGhnWebhookRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.shipmentStatusChangedOutboxService = shipmentStatusChangedOutboxService;
        this.objectMapper = objectMapper;
        this.clock = clock;
    }

    @Transactional
    public ProcessGhnWebhookResult execute(JsonNode webhookBody, String tokenHeader, String authorizationHeader) {
        String payloadJson = serialize(webhookBody);
        String ghnOrderCode = extractGhnOrderCode(webhookBody);
        String rawStatus = extractRawStatus(webhookBody);

        boolean signatureValid = signatureVerifier.verify(tokenHeader, authorizationHeader);
        UUID logId = ghnWebhookLogRepository.insertLog(ghnOrderCode, rawStatus, payloadJson);

        if (!signatureValid) {
            log.warn("Rejected GHN webhook with invalid token for ghnOrderCode={}", ghnOrderCode);
            return ProcessGhnWebhookResult.invalidSignature(ghnOrderCode, rawStatus);
        }

        Optional<SellerShipmentRecord> shipmentOptional =
                processGhnWebhookRepository.findByGhnOrderCodeForUpdate(ghnOrderCode);
        if (shipmentOptional.isEmpty()) {
            log.warn("GHN webhook shipment not found for ghnOrderCode={}", ghnOrderCode);
            return ProcessGhnWebhookResult.shipmentNotFound(ghnOrderCode, rawStatus, true);
        }

        SellerShipmentRecord shipment = shipmentOptional.get();
        Optional<ShipmentStatus> mappedStatus = GhnShipmentStatusMapper.map(rawStatus);
        if (mappedStatus.isEmpty()) {
            log.warn("GHN webhook unmapped status '{}' for ghnOrderCode={}", rawStatus, ghnOrderCode);
            ghnWebhookLogRepository.markProcessed(logId);
            return ProcessGhnWebhookResult.unmappedStatus(ghnOrderCode, rawStatus, true);
        }

        ShipmentStatus newStatus = mappedStatus.get();
        if (shipment.status() == newStatus) {
            ghnWebhookLogRepository.markProcessed(logId);
            return ProcessGhnWebhookResult.unchanged(
                    ghnOrderCode, rawStatus, true, shipment.shipmentId(), shipment.status()
            );
        }

        if (!GhnShipmentStatusPolicy.canTransition(shipment.status(), newStatus)) {
            log.warn(
                    "GHN webhook ignored out-of-order transition {} -> {} for shipment {}",
                    shipment.status(),
                    newStatus,
                    shipment.shipmentId()
            );
            ghnWebhookLogRepository.markProcessed(logId);
            return ProcessGhnWebhookResult.ignoredTransition(
                    ghnOrderCode,
                    rawStatus,
                    true,
                    shipment.shipmentId(),
                    shipment.status(),
                    newStatus
            );
        }

        Instant occurredAt = clock.instant();
        boolean updated = processGhnWebhookRepository.updateStatus(
                shipment.shipmentId(),
                shipment.status(),
                newStatus,
                occurredAt
        );
        if (!updated) {
            log.warn("GHN webhook concurrent status change for shipment {}", shipment.shipmentId());
            ghnWebhookLogRepository.markProcessed(logId);
            return ProcessGhnWebhookResult.unchanged(
                    ghnOrderCode, rawStatus, true, shipment.shipmentId(), shipment.status()
            );
        }

        processGhnWebhookRepository.insertStatusHistory(
                shipment.shipmentId(),
                shipment.status(),
                newStatus,
                rawStatus,
                occurredAt
        );

        GhnShipmentStatusPolicy.orderItemStatusForShipmentStatus(newStatus)
                .ifPresent(itemStatus -> processGhnWebhookRepository.updateOrderItemsForShipment(
                        shipment.shipmentId(),
                        itemStatus.name(),
                        occurredAt
                ));

        outboxEventRepository.save(shipmentStatusChangedOutboxService.build(
                shipment.shipmentId(),
                shipment.orderId(),
                shipment.sellerId(),
                shipment.status(),
                newStatus,
                occurredAt
        ));

        ghnWebhookLogRepository.markProcessed(logId);
        return ProcessGhnWebhookResult.updated(
                ghnOrderCode,
                rawStatus,
                true,
                shipment.shipmentId(),
                shipment.status(),
                newStatus
        );
    }

    private String extractGhnOrderCode(JsonNode body) {
        String[] rootFields = {"OrderCode", "order_code", "ghn_order_code", "code"};
        for (String field : rootFields) {
            if (body.hasNonNull(field) && !body.get(field).asText().isBlank()) {
                return body.get(field).asText();
            }
        }
        JsonNode data = body.path("Data");
        if (!data.isMissingNode() && !data.isNull()) {
            for (String field : rootFields) {
                if (data.hasNonNull(field) && !data.get(field).asText().isBlank()) {
                    return data.get(field).asText();
                }
            }
        }
        JsonNode dataLower = body.path("data");
        if (!dataLower.isMissingNode() && !dataLower.isNull()) {
            for (String field : rootFields) {
                if (dataLower.hasNonNull(field) && !dataLower.get(field).asText().isBlank()) {
                    return dataLower.get(field).asText();
                }
            }
        }
        return "UNKNOWN";
    }

    private String extractRawStatus(JsonNode body) {
        String[] fields = {"Status", "status", "state"};
        for (String field : fields) {
            if (body.hasNonNull(field) && !body.get(field).asText().isBlank()) {
                return body.get(field).asText();
            }
        }
        JsonNode data = body.path("Data");
        if (!data.isMissingNode() && !data.isNull()) {
            for (String field : fields) {
                if (data.hasNonNull(field) && !data.get(field).asText().isBlank()) {
                    return data.get(field).asText();
                }
            }
        }
        JsonNode dataLower = body.path("data");
        if (!dataLower.isMissingNode() && !dataLower.isNull()) {
            for (String field : fields) {
                if (dataLower.hasNonNull(field) && !dataLower.get(field).asText().isBlank()) {
                    return dataLower.get(field).asText();
                }
            }
        }
        return "unknown";
    }

    private String serialize(JsonNode webhookBody) {
        try {
            return objectMapper.writeValueAsString(webhookBody);
        } catch (JsonProcessingException ex) {
            return webhookBody.toString();
        }
    }
}
