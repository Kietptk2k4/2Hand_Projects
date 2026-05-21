package com.twohands.commerce_service.infrastructure.ghn;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.commerce_service.config.CommerceIntegrationProperties;
import com.twohands.commerce_service.domain.shipment.GhnCreateOrderCommand;
import com.twohands.commerce_service.domain.shipment.GhnCreateOrderResult;
import com.twohands.commerce_service.domain.shipment.GhnShipmentGateway;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class GhnShipmentGatewayAdapter implements GhnShipmentGateway {

    private static final Logger log = LoggerFactory.getLogger(GhnShipmentGatewayAdapter.class);

    private final CommerceIntegrationProperties.Ghn ghnProperties;
    private final RestClient ghnRestClient;
    private final ObjectMapper objectMapper;

    public GhnShipmentGatewayAdapter(
            CommerceIntegrationProperties integrationProperties,
            RestClient ghnRestClient,
            ObjectMapper objectMapper
    ) {
        this.ghnProperties = integrationProperties.getGhn();
        this.ghnRestClient = ghnRestClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public GhnCreateOrderResult createOrder(GhnCreateOrderCommand command) {
        if (ghnProperties.isLiveClientConfigured()) {
            try {
                return createViaGhnApi(command);
            } catch (AppException ex) {
                if (!ghnProperties.isMockFallbackEnabled()) {
                    throw ex;
                }
                log.warn("GHN API failed for shipment {}, using mock fallback: {}", command.shipmentId(), ex.getMessage());
            } catch (RestClientException ex) {
                if (!ghnProperties.isMockFallbackEnabled()) {
                    throw new AppException(ErrorCode.GHN_PROVIDER_UNAVAILABLE, "GHN provider unavailable", ex);
                }
                log.warn("GHN API unreachable for shipment {}, using mock fallback", command.shipmentId(), ex);
            }
        }

        if (ghnProperties.isMockFallbackEnabled()) {
            return createMockOrder(command);
        }

        throw new AppException(ErrorCode.GHN_PROVIDER_UNAVAILABLE, "GHN integration is not configured");
    }

    private GhnCreateOrderResult createViaGhnApi(GhnCreateOrderCommand command) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("payment_type_id", command.codAmount() > 0 ? 2 : 1);
        body.put("required_note", "2Hands shipment");
        body.put("to_name", command.receiverName());
        body.put("to_phone", command.receiverPhone());
        body.put("to_address", command.toAddressDetail());
        body.put("to_ward_code", command.toWardCode());
        body.put("to_district_id", parseDistrictId(command.toDistrictCode()));
        body.put("from_name", "2Hands Seller");
        body.put("from_phone", "0900000000");
        body.put("from_address", command.fromAddressDetail());
        body.put("from_ward_code", command.fromWardCode());
        body.put("from_district_id", parseDistrictId(command.fromDistrictCode()));
        body.put("weight", command.totalWeightGram());
        body.put("cod_amount", command.codAmount());
        body.put("client_order_code", command.shipmentId().toString());

        String rawResponse = ghnRestClient.post()
                .uri("/shiip/public-api/v2/shipping-order/create")
                .contentType(MediaType.APPLICATION_JSON)
                .header("ShopId", ghnProperties.getShopId())
                .body(body)
                .retrieve()
                .body(String.class);

        if (rawResponse == null || rawResponse.isBlank()) {
            throw new AppException(ErrorCode.GHN_PROVIDER_UNAVAILABLE, "GHN returned empty response");
        }

        try {
            JsonNode root = objectMapper.readTree(rawResponse);
            JsonNode data = root.path("data");
            String orderCode = data.path("order_code").asText(null);
            if (orderCode == null || orderCode.isBlank()) {
                throw new AppException(ErrorCode.GHN_PROVIDER_UNAVAILABLE, "GHN response missing order_code");
            }
            return new GhnCreateOrderResult(
                    orderCode,
                    ghnProperties.getShopId(),
                    data.path("expected_delivery_time").asText(orderCode),
                    rawResponse,
                    false
            );
        } catch (JsonProcessingException ex) {
            throw new AppException(ErrorCode.GHN_PROVIDER_UNAVAILABLE, "Cannot parse GHN response", ex);
        }
    }

    private GhnCreateOrderResult createMockOrder(GhnCreateOrderCommand command) {
        String mockCode = "GHN-MOCK-" + command.shipmentId().toString().substring(0, 8).toUpperCase();
        String tracking = "TRK-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase();
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("mock", true);
        payload.put("shipment_id", command.shipmentId().toString());
        payload.put("order_code", mockCode);
        payload.put("tracking_number", tracking);

        try {
            return new GhnCreateOrderResult(
                    mockCode,
                    ghnProperties.getShopId() != null ? ghnProperties.getShopId() : "MOCK-SHOP",
                    tracking,
                    objectMapper.writeValueAsString(payload),
                    true
            );
        } catch (JsonProcessingException ex) {
            throw new AppException(ErrorCode.INTERNAL_ERROR, "Cannot serialize mock GHN payload", ex);
        }
    }

    private int parseDistrictId(String districtCode) {
        try {
            return Integer.parseInt(districtCode);
        } catch (NumberFormatException ex) {
            return 0;
        }
    }
}
