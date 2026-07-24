package com.twohands.commerce_service.infrastructure.ghn;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.commerce_service.config.CommerceIntegrationProperties;
import com.twohands.commerce_service.domain.shipment.GhnLeadtimeGateway;
import com.twohands.commerce_service.domain.shipment.GhnLeadtimeQuery;
import com.twohands.commerce_service.domain.shipment.GhnLeadtimeResult;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class GhnLeadtimeGatewayAdapter implements GhnLeadtimeGateway {

    private static final Logger log = LoggerFactory.getLogger(GhnLeadtimeGatewayAdapter.class);

    private final CommerceIntegrationProperties.Ghn ghnProperties;
    private final RestClient ghnRestClient;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    public GhnLeadtimeGatewayAdapter(
            CommerceIntegrationProperties integrationProperties,
            RestClient ghnRestClient,
            ObjectMapper objectMapper,
            Clock clock
    ) {
        this.ghnProperties = integrationProperties.getGhn();
        this.ghnRestClient = ghnRestClient;
        this.objectMapper = objectMapper;
        this.clock = clock;
    }

    @Override
    public GhnLeadtimeResult calculateLeadtime(GhnLeadtimeQuery query) {
        if (!ghnProperties.isLiveClientConfigured()) {
            throw new AppException(ErrorCode.GHN_PROVIDER_UNAVAILABLE, "GHN integration is not configured");
        }
        if (!StringUtils.hasText(query.toWardCode())) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "to_ward_code is required for leadtime");
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("from_district_id", query.fromDistrictId());
        if (StringUtils.hasText(query.fromWardCode())) {
            body.put("from_ward_code", query.fromWardCode().trim());
        }
        body.put("to_district_id", query.toDistrictId());
        body.put("to_ward_code", query.toWardCode().trim());
        body.put("service_id", query.serviceId());

        try {
            String rawResponse = ghnRestClient.post()
                    .uri("/shiip/public-api/v2/shipping-order/leadtime")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("ShopId", ghnProperties.getShopId())
                    .body(body)
                    .retrieve()
                    .body(String.class);

            return parseLeadtimeResponse(rawResponse);
        } catch (RestClientException ex) {
            log.warn("GHN leadtime request failed: {}", ex.getMessage());
            throw new AppException(ErrorCode.GHN_PROVIDER_UNAVAILABLE, "GHN provider unavailable", ex);
        }
    }

    public GhnLeadtimeResult parseLeadtimeResponse(String rawResponse) {
        if (rawResponse == null || rawResponse.isBlank()) {
            throw new AppException(ErrorCode.GHN_PROVIDER_UNAVAILABLE, "GHN returned empty response");
        }

        try {
            JsonNode root = objectMapper.readTree(rawResponse);
            int code = root.path("code").asInt(0);
            if (code != 200) {
                String message = root.path("message").asText("GHN leadtime failed");
                throw new AppException(ErrorCode.GHN_PROVIDER_UNAVAILABLE, message);
            }

            JsonNode data = root.path("data");
            long leadtimeEpoch = data.path("leadtime").asLong(0);
            if (leadtimeEpoch <= 0) {
                throw new AppException(ErrorCode.GHN_PROVIDER_UNAVAILABLE, "GHN response missing leadtime");
            }

            LocalDate estimatedDeliveryDate = Instant.ofEpochSecond(leadtimeEpoch)
                    .atZone(clock.getZone())
                    .toLocalDate();

            return new GhnLeadtimeResult(estimatedDeliveryDate, rawResponse);
        } catch (JsonProcessingException ex) {
            throw new AppException(ErrorCode.GHN_PROVIDER_UNAVAILABLE, "Cannot parse GHN leadtime response", ex);
        }
    }
}
