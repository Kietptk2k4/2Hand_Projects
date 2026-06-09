package com.twohands.commerce_service.unit.infrastructure.ghn;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.commerce_service.config.CommerceIntegrationProperties;
import com.twohands.commerce_service.infrastructure.ghn.GhnAddressMasterDataGatewayAdapter;
import com.twohands.commerce_service.domain.shipment.GhnDistrict;
import com.twohands.commerce_service.domain.shipment.GhnProvince;
import com.twohands.commerce_service.domain.shipment.GhnWard;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GhnAddressMasterDataGatewayAdapterTest {

    private GhnAddressMasterDataGatewayAdapter adapter;

    @BeforeEach
    void setUp() {
        CommerceIntegrationProperties properties = new CommerceIntegrationProperties();
        properties.getGhn().setEnabled(true);
        properties.getGhn().setToken("test-token");
        properties.getGhn().setShopId("885");
        adapter = new GhnAddressMasterDataGatewayAdapter(
                properties,
                RestClient.builder().build(),
                new ObjectMapper()
        );
    }

    @Test
    void parseProvinces_readsProvinceList() {
        String json = """
                {
                  "code": 200,
                  "message": "Success",
                  "data": [
                    {"ProvinceID": 201, "ProvinceName": "Ha Noi", "Code": "4"},
                    {"ProvinceID": 202, "ProvinceName": "HCM", "Code": "8"}
                  ]
                }
                """;

        List<GhnProvince> provinces = adapter.parseProvinces(json);

        assertThat(provinces).hasSize(2);
        assertThat(provinces.get(0).provinceId()).isEqualTo(201);
    }

    @Test
    void parseDistricts_readsDistrictList() {
        String json = """
                {
                  "code": 200,
                  "message": "Success",
                  "data": [
                    {"DistrictID": 1442, "ProvinceID": 202, "DistrictName": "Quan 1", "Code": "0201"}
                  ]
                }
                """;

        List<GhnDistrict> districts = adapter.parseDistricts(json);

        assertThat(districts).hasSize(1);
        assertThat(districts.getFirst().districtId()).isEqualTo(1442);
    }

    @Test
    void parseWards_readsWardList() {
        String json = """
                {
                  "code": 200,
                  "message": "Success",
                  "data": [
                    {"WardCode": "20308", "DistrictID": 1444, "WardName": "Phuong 14"}
                  ]
                }
                """;

        List<GhnWard> wards = adapter.parseWards(json);

        assertThat(wards).hasSize(1);
        assertThat(wards.getFirst().wardCode()).isEqualTo("20308");
    }
}
