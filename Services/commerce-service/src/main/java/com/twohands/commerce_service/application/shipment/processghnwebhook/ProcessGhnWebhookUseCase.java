package com.twohands.commerce_service.application.shipment.processghnwebhook;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.commerce_service.application.shipment.common.GhnShipmentStatusUpdateResult;
import com.twohands.commerce_service.application.shipment.common.GhnShipmentStatusUpdateService;
import com.twohands.commerce_service.domain.shipment.GhnWebhookLogRepository;
import com.twohands.commerce_service.domain.shipment.ProcessGhnWebhookRepository;
import com.twohands.commerce_service.domain.shipment.SellerShipmentRecord;
import com.twohands.commerce_service.infrastructure.ghn.GhnWebhookSignatureVerifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Optional;
import java.util.UUID;

@Service
public class ProcessGhnWebhookUseCase {

    private static final Logger log = LoggerFactory.getLogger(ProcessGhnWebhookUseCase.class);

    private final GhnWebhookSignatureVerifier signatureVerifier;
    private final GhnWebhookLogRepository ghnWebhookLogRepository;
    private final ProcessGhnWebhookRepository processGhnWebhookRepository;
    private final GhnShipmentStatusUpdateService ghnShipmentStatusUpdateService;
    private final ObjectMapper objectMapper;

    public ProcessGhnWebhookUseCase(
            GhnWebhookSignatureVerifier signatureVerifier,
            GhnWebhookLogRepository ghnWebhookLogRepository,
            ProcessGhnWebhookRepository processGhnWebhookRepository,
            GhnShipmentStatusUpdateService ghnShipmentStatusUpdateService,
            ObjectMapper objectMapper
    ) {
        this.signatureVerifier = signatureVerifier;
        this.ghnWebhookLogRepository = ghnWebhookLogRepository;
        this.processGhnWebhookRepository = processGhnWebhookRepository;
        this.ghnShipmentStatusUpdateService = ghnShipmentStatusUpdateService;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public ProcessGhnWebhookResult execute(JsonNode webhookBody, String tokenHeader, String authorizationHeader) {
        String payloadJson = serialize(webhookBody);
        String ghnOrderCode = extractGhnOrderCode(webhookBody);
        String rawStatus = extractRawStatus(webhookBody);
        String webhookType = extractWebhookType(webhookBody);

        boolean signatureValid = signatureVerifier.verify(tokenHeader, authorizationHeader);
        UUID logId = ghnWebhookLogRepository.insertLog(ghnOrderCode, rawStatus, payloadJson);

        if (!signatureValid) {
            log.warn("Rejected GHN webhook with invalid token for ghnOrderCode={}", ghnOrderCode);
            return ProcessGhnWebhookResult.invalidSignature(ghnOrderCode, rawStatus);
        }

        Optional<SellerShipmentRecord> shipmentOptional = resolveShipment(webhookBody, ghnOrderCode);
        if (shipmentOptional.isEmpty()) {
            log.warn("GHN webhook shipment not found for ghnOrderCode={}", ghnOrderCode);
            return ProcessGhnWebhookResult.shipmentNotFound(ghnOrderCode, rawStatus, true);
        }

        if (!shouldApplyStatusTransition(webhookType)) {
            log.info("GHN webhook type '{}' skipped status transition for {}", webhookType, ghnOrderCode);
            ghnWebhookLogRepository.markProcessed(logId);
            return ProcessGhnWebhookResult.unchanged(
                    ghnOrderCode,
                    rawStatus,
                    true,
                    shipmentOptional.get().shipmentId(),
                    shipmentOptional.get().status()
            );
        }

        SellerShipmentRecord shipment = shipmentOptional.get();
        GhnShipmentStatusUpdateResult updateResult = ghnShipmentStatusUpdateService.apply(
                shipment,
                rawStatus,
                ghnOrderCode
        );

        if (updateResult.unmappedStatus()) {
            ghnWebhookLogRepository.markProcessed(logId);
            return ProcessGhnWebhookResult.unmappedStatus(ghnOrderCode, rawStatus, true);
        }
        if (updateResult.ignoredTransition()) {
            ghnWebhookLogRepository.markProcessed(logId);
            return ProcessGhnWebhookResult.ignoredTransition(
                    ghnOrderCode,
                    rawStatus,
                    true,
                    shipment.shipmentId(),
                    shipment.status(),
                    updateResult.newStatus()
            );
        }
        if (updateResult.unchanged()) {
            ghnWebhookLogRepository.markProcessed(logId);
            return ProcessGhnWebhookResult.unchanged(
                    ghnOrderCode,
                    rawStatus,
                    true,
                    shipment.shipmentId(),
                    shipment.status()
            );
        }

        ghnWebhookLogRepository.markProcessed(logId);
        return ProcessGhnWebhookResult.updated(
                ghnOrderCode,
                rawStatus,
                true,
                shipment.shipmentId(),
                updateResult.previousStatus(),
                updateResult.newStatus()
        );
    }

    private Optional<SellerShipmentRecord> resolveShipment(JsonNode body, String ghnOrderCode) {
        if (!"UNKNOWN".equals(ghnOrderCode)) {
            Optional<SellerShipmentRecord> byOrderCode =
                    processGhnWebhookRepository.findByGhnOrderCodeForUpdate(ghnOrderCode);
            if (byOrderCode.isPresent()) {
                return byOrderCode;
            }
        }

        String clientOrderCode = extractClientOrderCode(body);
        if (isUuid(clientOrderCode)) {
            return processGhnWebhookRepository.findByShipmentIdForUpdate(UUID.fromString(clientOrderCode));
        }
        return Optional.empty();
    }

    private boolean shouldApplyStatusTransition(String webhookType) {
        if (!StringUtils.hasText(webhookType)) {
            return true;
        }
        String normalized = webhookType.trim().toLowerCase().replace('-', '_');
        if (normalized.contains("weight") || normalized.contains("cod") || normalized.contains("fee")) {
            return false;
        }
        return normalized.contains("status")
                || normalized.contains("create")
                || normalized.contains("switch");
    }

    private boolean isUuid(String value) {
        if (!StringUtils.hasText(value)) {
            return false;
        }
        try {
            UUID.fromString(value.trim());
            return true;
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    private String extractGhnOrderCode(JsonNode body) {
        String[] rootFields = {"OrderCode", "order_code", "ghn_order_code", "code"};
        for (String field : rootFields) {
            if (body.hasNonNull(field) && !body.get(field).asText().isBlank()) {
                return body.get(field).asText();
            }
        }
        for (String container : new String[] {"Data", "data"}) {
            JsonNode node = body.path(container);
            if (!node.isMissingNode() && !node.isNull()) {
                for (String field : rootFields) {
                    if (node.hasNonNull(field) && !node.get(field).asText().isBlank()) {
                        return node.get(field).asText();
                    }
                }
            }
        }
        return "UNKNOWN";
    }

    private String extractClientOrderCode(JsonNode body) {
        String[] fields = {"ClientOrderCode", "client_order_code"};
        for (String field : fields) {
            if (body.hasNonNull(field) && !body.get(field).asText().isBlank()) {
                return body.get(field).asText();
            }
        }
        for (String container : new String[] {"Data", "data"}) {
            JsonNode node = body.path(container);
            if (!node.isMissingNode() && !node.isNull()) {
                for (String field : fields) {
                    if (node.hasNonNull(field) && !node.get(field).asText().isBlank()) {
                        return node.get(field).asText();
                    }
                }
            }
        }
        return null;
    }

    private String extractRawStatus(JsonNode body) {
        String[] fields = {"Status", "status", "state"};
        for (String field : fields) {
            if (body.hasNonNull(field) && !body.get(field).asText().isBlank()) {
                return body.get(field).asText();
            }
        }
        for (String container : new String[] {"Data", "data"}) {
            JsonNode node = body.path(container);
            if (!node.isMissingNode() && !node.isNull()) {
                for (String field : fields) {
                    if (node.hasNonNull(field) && !node.get(field).asText().isBlank()) {
                        return node.get(field).asText();
                    }
                }
            }
        }
        return "unknown";
    }

    private String extractWebhookType(JsonNode body) {
        String[] fields = {"Type", "type"};
        for (String field : fields) {
            if (body.hasNonNull(field) && !body.get(field).asText().isBlank()) {
                return body.get(field).asText();
            }
        }
        for (String container : new String[] {"Data", "data"}) {
            JsonNode node = body.path(container);
            if (!node.isMissingNode() && !node.isNull()) {
                for (String field : fields) {
                    if (node.hasNonNull(field) && !node.get(field).asText().isBlank()) {
                        return node.get(field).asText();
                    }
                }
            }
        }
        return null;
    }

    private String serialize(JsonNode webhookBody) {
        try {
            return objectMapper.writeValueAsString(webhookBody);
        } catch (JsonProcessingException ex) {
            return webhookBody.toString();
        }
    }
}
