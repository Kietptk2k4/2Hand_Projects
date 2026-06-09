package com.twohands.commerce_service.infrastructure.ghn;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.commerce_service.config.CommerceIntegrationProperties;
import com.twohands.commerce_service.domain.shipment.GhnCancelOrderGateway;
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
import java.util.List;
import java.util.Map;

@Component
public class GhnCancelOrderGatewayAdapter implements GhnCancelOrderGateway {

    private static final Logger log = LoggerFactory.getLogger(GhnCancelOrderGatewayAdapter.class);

    private final CommerceIntegrationProperties.Ghn ghnProperties;
    private final RestClient ghnRestClient;
    private final ObjectMapper objectMapper;

    public GhnCancelOrderGatewayAdapter(
            CommerceIntegrationProperties integrationProperties,
            RestClient ghnRestClient,
            ObjectMapper objectMapper
    ) {
        this.ghnProperties = integrationProperties.getGhn();
        this.ghnRestClient = ghnRestClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public void cancelOrder(String ghnOrderCode) {
        if (!ghnProperties.isLiveClientConfigured()) {
            throw new AppException(ErrorCode.GHN_PROVIDER_UNAVAILABLE, "GHN integration is not configured");
        }
        if (!StringUtils.hasText(ghnOrderCode)) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "ghn_order_code is required");
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("order_codes", List.of(ghnOrderCode.trim()));

        try {
            String rawResponse = ghnRestClient.post()
                    .uri("/shiip/public-api/v2/switch-status/cancel")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("ShopId", ghnProperties.getShopId())
                    .body(body)
                    .retrieve()
                    .body(String.class);
            parseCancelResponse(rawResponse, ghnOrderCode.trim());
        } catch (RestClientException ex) {
            log.warn("GHN cancel-order failed for {}: {}", ghnOrderCode, ex.getMessage());
            throw new AppException(ErrorCode.GHN_PROVIDER_UNAVAILABLE, "GHN provider unavailable", ex);
        }
    }

    public void parseCancelResponse(String rawResponse, String ghnOrderCode) {
        if (rawResponse == null || rawResponse.isBlank()) {
            throw new AppException(ErrorCode.GHN_PROVIDER_UNAVAILABLE, "GHN returned empty response");
        }
        try {
            JsonNode root = objectMapper.readTree(rawResponse);
            if (root.path("code").asInt(0) != 200) {
                throw new AppException(
                        ErrorCode.GHN_PROVIDER_UNAVAILABLE,
                        root.path("message").asText("GHN cancel-order failed")
                );
            }
            JsonNode data = root.path("data");
            if (!data.isArray() || data.isEmpty()) {
                throw new AppException(ErrorCode.GHN_PROVIDER_UNAVAILABLE, "GHN cancel-order returned no result");
            }
            for (JsonNode item : data) {
                if (ghnOrderCode.equals(item.path("order_code").asText())) {
                    if (!item.path("result").asBoolean(false)) {
                        throw new AppException(
                                ErrorCode.GHN_PROVIDER_UNAVAILABLE,
                                item.path("message").asText("GHN rejected cancel request")
                        );
                    }
                    return;
                }
            }
            throw new AppException(ErrorCode.GHN_PROVIDER_UNAVAILABLE, "GHN cancel-order missing order result");
        } catch (JsonProcessingException ex) {
            throw new AppException(ErrorCode.GHN_PROVIDER_UNAVAILABLE, "Cannot parse GHN cancel response", ex);
        }
    }
}
