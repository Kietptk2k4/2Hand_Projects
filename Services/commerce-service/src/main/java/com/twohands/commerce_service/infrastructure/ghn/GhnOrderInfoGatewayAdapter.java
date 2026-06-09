package com.twohands.commerce_service.infrastructure.ghn;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.commerce_service.config.CommerceIntegrationProperties;
import com.twohands.commerce_service.domain.shipment.GhnOrderInfoGateway;
import com.twohands.commerce_service.domain.shipment.GhnOrderInfoResult;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class GhnOrderInfoGatewayAdapter implements GhnOrderInfoGateway {

    private static final Logger log = LoggerFactory.getLogger(GhnOrderInfoGatewayAdapter.class);

    private final CommerceIntegrationProperties.Ghn ghnProperties;
    private final RestClient ghnRestClient;
    private final ObjectMapper objectMapper;

    public GhnOrderInfoGatewayAdapter(
            CommerceIntegrationProperties integrationProperties,
            RestClient ghnRestClient,
            ObjectMapper objectMapper
    ) {
        this.ghnProperties = integrationProperties.getGhn();
        this.ghnRestClient = ghnRestClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public GhnOrderInfoResult fetchByOrderCode(String orderCode) {
        if (!StringUtils.hasText(orderCode)) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "GHN order_code is required");
        }
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("order_code", orderCode.trim());
        return fetchDetail(body);
    }

    @Override
    public GhnOrderInfoResult fetchByClientOrderCode(String clientOrderCode) {
        if (!StringUtils.hasText(clientOrderCode)) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "client_order_code is required");
        }
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("client_order_code", clientOrderCode.trim());
        return fetchDetail(body);
    }

    private GhnOrderInfoResult fetchDetail(Map<String, Object> body) {
        if (!ghnProperties.isLiveClientConfigured()) {
            throw new AppException(ErrorCode.GHN_PROVIDER_UNAVAILABLE, "GHN integration is not configured");
        }

        try {
            String rawResponse = ghnRestClient.post()
                    .uri("/shiip/public-api/v2/shipping-order/detail")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("ShopId", ghnProperties.getShopId())
                    .body(body)
                    .retrieve()
                    .body(String.class);
            return parseDetailResponse(rawResponse);
        } catch (RestClientException ex) {
            log.warn("GHN order-info request failed: {}", ex.getMessage());
            throw new AppException(ErrorCode.GHN_PROVIDER_UNAVAILABLE, "GHN provider unavailable", ex);
        }
    }

    public GhnOrderInfoResult parseDetailResponse(String rawResponse) {
        if (rawResponse == null || rawResponse.isBlank()) {
            throw new AppException(ErrorCode.GHN_PROVIDER_UNAVAILABLE, "GHN returned empty response");
        }

        try {
            JsonNode root = objectMapper.readTree(rawResponse);
            int code = root.path("code").asInt(0);
            if (code != 200) {
                String message = root.path("message").asText("GHN order-info failed");
                throw new AppException(ErrorCode.GHN_PROVIDER_UNAVAILABLE, message);
            }

            JsonNode orderNode = extractOrderNode(root.path("data"));
            String orderCode = orderNode.path("order_code").asText(null);
            String status = orderNode.path("status").asText(null);
            if (!StringUtils.hasText(orderCode)) {
                throw new AppException(ErrorCode.GHN_PROVIDER_UNAVAILABLE, "GHN response missing order_code");
            }
            if (!StringUtils.hasText(status)) {
                throw new AppException(ErrorCode.GHN_PROVIDER_UNAVAILABLE, "GHN response missing status");
            }

            return new GhnOrderInfoResult(orderCode, status.trim(), rawResponse, false);
        } catch (JsonProcessingException ex) {
            throw new AppException(ErrorCode.GHN_PROVIDER_UNAVAILABLE, "Cannot parse GHN order-info response", ex);
        }
    }

    private JsonNode extractOrderNode(JsonNode data) {
        if (data.isArray() && !data.isEmpty()) {
            return data.get(0);
        }
        if (data.isObject()) {
            return data;
        }
        throw new AppException(ErrorCode.GHN_PROVIDER_UNAVAILABLE, "GHN response missing order data");
    }
}
