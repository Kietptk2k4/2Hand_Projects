package com.twohands.commerce_service.infrastructure.ghn;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.commerce_service.config.CommerceIntegrationProperties;
import com.twohands.commerce_service.domain.shipment.GhnPrintLabelGateway;
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
public class GhnPrintLabelGatewayAdapter implements GhnPrintLabelGateway {

    private static final Logger log = LoggerFactory.getLogger(GhnPrintLabelGatewayAdapter.class);

    private final CommerceIntegrationProperties.Ghn ghnProperties;
    private final RestClient ghnRestClient;
    private final ObjectMapper objectMapper;

    public GhnPrintLabelGatewayAdapter(
            CommerceIntegrationProperties integrationProperties,
            RestClient ghnRestClient,
            ObjectMapper objectMapper
    ) {
        this.ghnProperties = integrationProperties.getGhn();
        this.ghnRestClient = ghnRestClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public String generatePrintToken(String ghnOrderCode) {
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
                    .uri("/shiip/public-api/v2/a5/gen-token")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(String.class);
            return parsePrintToken(rawResponse);
        } catch (RestClientException ex) {
            log.warn("GHN print-token failed for {}: {}", ghnOrderCode, ex.getMessage());
            throw new AppException(ErrorCode.GHN_PROVIDER_UNAVAILABLE, "GHN provider unavailable", ex);
        }
    }

    public String parsePrintToken(String rawResponse) {
        if (rawResponse == null || rawResponse.isBlank()) {
            throw new AppException(ErrorCode.GHN_PROVIDER_UNAVAILABLE, "GHN returned empty response");
        }
        try {
            JsonNode root = objectMapper.readTree(rawResponse);
            if (root.path("code").asInt(0) != 200) {
                throw new AppException(
                        ErrorCode.GHN_PROVIDER_UNAVAILABLE,
                        root.path("message").asText("GHN print-token failed")
                );
            }
            String token = root.path("data").path("token").asText(null);
            if (!StringUtils.hasText(token)) {
                throw new AppException(ErrorCode.GHN_PROVIDER_UNAVAILABLE, "GHN response missing print token");
            }
            return token.trim();
        } catch (JsonProcessingException ex) {
            throw new AppException(ErrorCode.GHN_PROVIDER_UNAVAILABLE, "Cannot parse GHN print-token response", ex);
        }
    }
}
