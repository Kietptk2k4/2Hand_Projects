package com.twohands.commerce_service.infrastructure.ghn;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.commerce_service.config.CommerceIntegrationProperties;
import com.twohands.commerce_service.domain.shipment.GhnAvailableServicesQuery;
import com.twohands.commerce_service.domain.shipment.GhnServiceCatalogGateway;
import com.twohands.commerce_service.domain.shipment.GhnServiceOption;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class GhnServiceCatalogGatewayAdapter implements GhnServiceCatalogGateway {

    private static final Logger log = LoggerFactory.getLogger(GhnServiceCatalogGatewayAdapter.class);

    private final CommerceIntegrationProperties.Ghn ghnProperties;
    private final RestClient ghnRestClient;
    private final ObjectMapper objectMapper;

    public GhnServiceCatalogGatewayAdapter(
            CommerceIntegrationProperties integrationProperties,
            RestClient ghnRestClient,
            ObjectMapper objectMapper
    ) {
        this.ghnProperties = integrationProperties.getGhn();
        this.ghnRestClient = ghnRestClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public List<GhnServiceOption> listAvailableServices(GhnAvailableServicesQuery query) {
        if (!ghnProperties.isLiveClientConfigured()) {
            throw new AppException(ErrorCode.GHN_PROVIDER_UNAVAILABLE, "GHN integration is not configured");
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("shop_id", Integer.parseInt(ghnProperties.getShopId().trim()));
        body.put("from_district", query.fromDistrictId());
        body.put("to_district", query.toDistrictId());

        try {
            String rawResponse = ghnRestClient.post()
                    .uri("/shiip/public-api/v2/shipping-order/available-services")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("ShopId", ghnProperties.getShopId())
                    .body(body)
                    .retrieve()
                    .body(String.class);

            return parseServices(rawResponse);
        } catch (RestClientException ex) {
            log.warn("GHN available-services request failed: {}", ex.getMessage());
            throw new AppException(ErrorCode.GHN_PROVIDER_UNAVAILABLE, "GHN provider unavailable", ex);
        }
    }

    public List<GhnServiceOption> parseServices(String rawResponse) {
        if (rawResponse == null || rawResponse.isBlank()) {
            throw new AppException(ErrorCode.GHN_PROVIDER_UNAVAILABLE, "GHN returned empty response");
        }

        try {
            JsonNode root = objectMapper.readTree(rawResponse);
            int code = root.path("code").asInt(0);
            if (code != 200) {
                String message = root.path("message").asText("GHN available-services failed");
                throw new AppException(ErrorCode.GHN_PROVIDER_UNAVAILABLE, message);
            }

            JsonNode data = root.path("data");
            if (!data.isArray()) {
                throw new AppException(ErrorCode.GHN_PROVIDER_UNAVAILABLE, "GHN response missing services array");
            }

            List<GhnServiceOption> services = new ArrayList<>();
            for (JsonNode node : data) {
                int serviceId = node.path("service_id").asInt(0);
                int serviceTypeId = node.path("service_type_id").asInt(0);
                if (serviceId <= 0) {
                    continue;
                }
                services.add(new GhnServiceOption(
                        serviceId,
                        serviceTypeId,
                        node.path("short_name").asText("")
                ));
            }

            if (services.isEmpty()) {
                throw new AppException(ErrorCode.GHN_PROVIDER_UNAVAILABLE, "GHN returned no usable services");
            }

            return List.copyOf(services);
        } catch (JsonProcessingException ex) {
            throw new AppException(ErrorCode.GHN_PROVIDER_UNAVAILABLE, "Cannot parse GHN available-services response", ex);
        }
    }
}
