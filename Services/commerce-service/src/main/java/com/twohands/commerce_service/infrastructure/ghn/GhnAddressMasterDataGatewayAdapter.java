package com.twohands.commerce_service.infrastructure.ghn;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.commerce_service.config.CommerceIntegrationProperties;
import com.twohands.commerce_service.domain.shipment.GhnAddressMasterDataGateway;
import com.twohands.commerce_service.domain.shipment.GhnDistrict;
import com.twohands.commerce_service.domain.shipment.GhnProvince;
import com.twohands.commerce_service.domain.shipment.GhnWard;
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
public class GhnAddressMasterDataGatewayAdapter implements GhnAddressMasterDataGateway {

    private static final Logger log = LoggerFactory.getLogger(GhnAddressMasterDataGatewayAdapter.class);

    private final CommerceIntegrationProperties.Ghn ghnProperties;
    private final RestClient ghnRestClient;
    private final ObjectMapper objectMapper;

    public GhnAddressMasterDataGatewayAdapter(
            CommerceIntegrationProperties integrationProperties,
            RestClient ghnRestClient,
            ObjectMapper objectMapper
    ) {
        this.ghnProperties = integrationProperties.getGhn();
        this.ghnRestClient = ghnRestClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public List<GhnProvince> listProvinces() {
        ensureConfigured();
        try {
            String rawResponse = ghnRestClient.get()
                    .uri("/shiip/public-api/master-data/province")
                    .retrieve()
                    .body(String.class);
            return parseProvinces(rawResponse);
        } catch (RestClientException ex) {
            log.warn("GHN list provinces failed: {}", ex.getMessage());
            throw new AppException(ErrorCode.GHN_PROVIDER_UNAVAILABLE, "GHN provider unavailable", ex);
        }
    }

    @Override
    public List<GhnDistrict> listDistricts(int provinceId) {
        ensureConfigured();
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("province_id", provinceId);
        try {
            String rawResponse = ghnRestClient.post()
                    .uri("/shiip/public-api/master-data/district")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(String.class);
            return parseDistricts(rawResponse);
        } catch (RestClientException ex) {
            log.warn("GHN list districts failed: {}", ex.getMessage());
            throw new AppException(ErrorCode.GHN_PROVIDER_UNAVAILABLE, "GHN provider unavailable", ex);
        }
    }

    @Override
    public List<GhnWard> listWards(int districtId) {
        ensureConfigured();
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("district_id", districtId);
        try {
            String rawResponse = ghnRestClient.post()
                    .uri("/shiip/public-api/master-data/ward?district_id")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(String.class);
            return parseWards(rawResponse);
        } catch (RestClientException ex) {
            log.warn("GHN list wards failed: {}", ex.getMessage());
            throw new AppException(ErrorCode.GHN_PROVIDER_UNAVAILABLE, "GHN provider unavailable", ex);
        }
    }

    public List<GhnProvince> parseProvinces(String rawResponse) {
        JsonNode data = parseDataArray(rawResponse);
        List<GhnProvince> provinces = new ArrayList<>();
        for (JsonNode node : data) {
            provinces.add(new GhnProvince(
                    node.path("ProvinceID").asInt(node.path("province_id").asInt(0)),
                    node.path("ProvinceName").asText(node.path("province_name").asText("")),
                    node.path("Code").asText(node.path("code").asText(""))
            ));
        }
        return List.copyOf(provinces);
    }

    public List<GhnDistrict> parseDistricts(String rawResponse) {
        JsonNode data = parseDataArray(rawResponse);
        List<GhnDistrict> districts = new ArrayList<>();
        for (JsonNode node : data) {
            districts.add(new GhnDistrict(
                    node.path("DistrictID").asInt(node.path("district_id").asInt(0)),
                    node.path("ProvinceID").asInt(node.path("province_id").asInt(0)),
                    node.path("DistrictName").asText(node.path("district_name").asText("")),
                    node.path("Code").asText(node.path("code").asText(""))
            ));
        }
        return List.copyOf(districts);
    }

    public List<GhnWard> parseWards(String rawResponse) {
        JsonNode data = parseDataArray(rawResponse);
        List<GhnWard> wards = new ArrayList<>();
        for (JsonNode node : data) {
            String wardCode = node.path("WardCode").asText(node.path("ward_code").asText(""));
            wards.add(new GhnWard(
                    wardCode,
                    node.path("DistrictID").asInt(node.path("district_id").asInt(0)),
                    node.path("WardName").asText(node.path("ward_name").asText(""))
            ));
        }
        return List.copyOf(wards);
    }

    private JsonNode parseDataArray(String rawResponse) {
        if (rawResponse == null || rawResponse.isBlank()) {
            throw new AppException(ErrorCode.GHN_PROVIDER_UNAVAILABLE, "GHN returned empty response");
        }
        try {
            JsonNode root = objectMapper.readTree(rawResponse);
            if (root.path("code").asInt(0) != 200) {
                throw new AppException(
                        ErrorCode.GHN_PROVIDER_UNAVAILABLE,
                        root.path("message").asText("GHN master-data failed")
                );
            }
            JsonNode data = root.path("data");
            if (!data.isArray()) {
                throw new AppException(ErrorCode.GHN_PROVIDER_UNAVAILABLE, "GHN response missing data array");
            }
            return data;
        } catch (JsonProcessingException ex) {
            throw new AppException(ErrorCode.GHN_PROVIDER_UNAVAILABLE, "Cannot parse GHN master-data response", ex);
        }
    }

    private void ensureConfigured() {
        if (!ghnProperties.isLiveClientConfigured()) {
            throw new AppException(ErrorCode.GHN_PROVIDER_UNAVAILABLE, "GHN integration is not configured");
        }
    }
}
