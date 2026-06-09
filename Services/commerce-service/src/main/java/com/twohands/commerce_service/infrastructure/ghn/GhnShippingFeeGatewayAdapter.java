package com.twohands.commerce_service.infrastructure.ghn;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.commerce_service.config.CommerceIntegrationProperties;
import com.twohands.commerce_service.domain.shipment.GhnShippingFeeGateway;
import com.twohands.commerce_service.domain.shipment.GhnShippingFeeQuery;
import com.twohands.commerce_service.domain.shipment.GhnShippingFeeResult;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class GhnShippingFeeGatewayAdapter implements GhnShippingFeeGateway {

    private static final Logger log = LoggerFactory.getLogger(GhnShippingFeeGatewayAdapter.class);

    private final CommerceIntegrationProperties.Ghn ghnProperties;
    private final RestClient ghnRestClient;
    private final ObjectMapper objectMapper;

    public GhnShippingFeeGatewayAdapter(
            CommerceIntegrationProperties integrationProperties,
            RestClient ghnRestClient,
            ObjectMapper objectMapper
    ) {
        this.ghnProperties = integrationProperties.getGhn();
        this.ghnRestClient = ghnRestClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public GhnShippingFeeResult calculateFee(GhnShippingFeeQuery query) {
        if (!ghnProperties.isLiveClientConfigured()) {
            throw new AppException(ErrorCode.GHN_PROVIDER_UNAVAILABLE, "GHN integration is not configured");
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("from_district_id", query.fromDistrictId());
        body.put("from_ward_code", query.fromWardCode());
        body.put("to_district_id", query.toDistrictId());
        body.put("to_ward_code", query.toWardCode());
        body.put("weight", query.weightGram());
        body.put("service_id", query.serviceId());
        if (query.serviceTypeId() > 0) {
            body.put("service_type_id", query.serviceTypeId());
        }
        body.put("length", query.lengthCm());
        body.put("width", query.widthCm());
        body.put("height", query.heightCm());
        body.put("insurance_value", 0);
        body.put("cod_value", 0);

        try {
            String rawResponse = ghnRestClient.post()
                    .uri("/shiip/public-api/v2/shipping-order/fee")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("ShopId", ghnProperties.getShopId())
                    .body(body)
                    .retrieve()
                    .body(String.class);

            return parseFeeResponse(rawResponse);
        } catch (RestClientException ex) {
            log.warn("GHN calculate-fee request failed: {}", ex.getMessage());
            throw new AppException(ErrorCode.GHN_PROVIDER_UNAVAILABLE, "GHN provider unavailable", ex);
        }
    }

    public GhnShippingFeeResult parseFeeResponse(String rawResponse) {
        if (rawResponse == null || rawResponse.isBlank()) {
            throw new AppException(ErrorCode.GHN_PROVIDER_UNAVAILABLE, "GHN returned empty response");
        }

        try {
            JsonNode root = objectMapper.readTree(rawResponse);
            int code = root.path("code").asInt(0);
            if (code != 200) {
                String message = root.path("message").asText("GHN calculate-fee failed");
                throw new AppException(ErrorCode.GHN_PROVIDER_UNAVAILABLE, message);
            }

            JsonNode data = root.path("data");
            long total = data.path("total").asLong(0);
            if (total <= 0) {
                total = data.path("service_fee").asLong(0);
            }
            if (total <= 0) {
                throw new AppException(ErrorCode.GHN_PROVIDER_UNAVAILABLE, "GHN response missing fee total");
            }

            BigDecimal totalFee = BigDecimal.valueOf(total);
            long serviceFee = data.path("service_fee").asLong(total);
            return new GhnShippingFeeResult(
                    totalFee,
                    BigDecimal.valueOf(serviceFee),
                    rawResponse,
                    false
            );
        } catch (JsonProcessingException ex) {
            throw new AppException(ErrorCode.GHN_PROVIDER_UNAVAILABLE, "Cannot parse GHN calculate-fee response", ex);
        }
    }
}
