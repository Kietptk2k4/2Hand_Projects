package com.twohands.commerce_service.infrastructure.ghn;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.commerce_service.config.CommerceIntegrationProperties;
import com.twohands.commerce_service.domain.shipment.GhnCreateOrderCommand;
import com.twohands.commerce_service.domain.shipment.GhnCreateOrderItem;
import com.twohands.commerce_service.domain.shipment.GhnCreateOrderResult;
import com.twohands.commerce_service.domain.shipment.GhnShipmentGateway;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
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
        body.put("required_note", "KHONGCHOXEMHANG");
        body.put("to_name", command.receiverName());
        body.put("to_phone", command.receiverPhone());
        body.put("to_address", command.toAddressDetail());
        body.put("to_ward_code", command.toWardCode());
        body.put("to_district_id", GhnDistrictIdParser.parseRequired(command.toDistrictCode(), "to_district_id"));
        body.put("from_name", command.fromName());
        body.put("from_phone", command.fromPhone());
        body.put("from_address", command.fromAddressDetail());
        body.put("from_ward_code", command.fromWardCode());
        body.put("from_district_id", GhnDistrictIdParser.parseRequired(command.fromDistrictCode(), "from_district_id"));
        body.put("weight", Math.max(command.totalWeightGram(), 1));
        body.put("length", command.lengthCm());
        body.put("width", command.widthCm());
        body.put("height", command.heightCm());
        body.put("cod_amount", command.codAmount());
        body.put("insurance_value", 0);
        body.put("service_id", command.serviceId());
        if (command.serviceTypeId() > 0) {
            body.put("service_type_id", command.serviceTypeId());
        }
        if (StringUtils.hasText(command.content())) {
            body.put("content", command.content());
        }
        body.put("client_order_code", command.shipmentId().toString());
        body.put("items", buildItemsPayload(command));

        String rawResponse = ghnRestClient.post()
                .uri("/shiip/public-api/v2/shipping-order/create")
                .contentType(MediaType.APPLICATION_JSON)
                .header("ShopId", ghnProperties.getShopId())
                .body(body)
                .retrieve()
                .body(String.class);

        return parseCreateResponse(rawResponse);
    }

    public List<Map<String, Object>> buildItemsPayload(GhnCreateOrderCommand command) {
        List<Map<String, Object>> items = new ArrayList<>();
        int defaultLength = Math.max(command.lengthCm() / 2, 1);
        int defaultWidth = Math.max(command.widthCm() / 2, 1);
        int defaultHeight = Math.max(command.heightCm() / 2, 1);

        for (GhnCreateOrderItem item : command.items()) {
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("name", item.name());
            payload.put("code", item.code());
            payload.put("quantity", item.quantity());
            payload.put("price", item.priceVnd());
            payload.put("weight", Math.max(item.weightGram(), 1));
            payload.put("length", defaultLength);
            payload.put("width", defaultWidth);
            payload.put("height", defaultHeight);
            items.add(payload);
        }

        if (items.isEmpty()) {
            Map<String, Object> fallback = new LinkedHashMap<>();
            fallback.put("name", "2Hands parcel");
            fallback.put("code", command.shipmentId().toString());
            fallback.put("quantity", 1);
            fallback.put("price", 0);
            fallback.put("weight", Math.max(command.totalWeightGram(), 1));
            fallback.put("length", command.lengthCm());
            fallback.put("width", command.widthCm());
            fallback.put("height", command.heightCm());
            items.add(fallback);
        }
        return items;
    }

    public GhnCreateOrderResult parseCreateResponse(String rawResponse) {
        if (rawResponse == null || rawResponse.isBlank()) {
            throw new AppException(ErrorCode.GHN_PROVIDER_UNAVAILABLE, "GHN returned empty response");
        }

        try {
            JsonNode root = objectMapper.readTree(rawResponse);
            int code = root.path("code").asInt(0);
            if (code != 200) {
                String message = root.path("message").asText("GHN create-order failed");
                throw new AppException(ErrorCode.GHN_PROVIDER_UNAVAILABLE, message);
            }

            JsonNode data = root.path("data");
            String orderCode = data.path("order_code").asText(null);
            if (orderCode == null || orderCode.isBlank()) {
                throw new AppException(ErrorCode.GHN_PROVIDER_UNAVAILABLE, "GHN response missing order_code");
            }
            return new GhnCreateOrderResult(
                    orderCode,
                    ghnProperties.getShopId(),
                    orderCode,
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
}
