package com.twohands.commerce_service.unit.infrastructure.ghn;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.commerce_service.config.CommerceIntegrationProperties;
import com.twohands.commerce_service.domain.shipment.GhnServiceOption;
import com.twohands.commerce_service.infrastructure.ghn.GhnServiceCatalogGatewayAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GhnServiceCatalogGatewayAdapterTest {

    private GhnServiceCatalogGatewayAdapter adapter;

    @BeforeEach
    void setUp() {
        CommerceIntegrationProperties properties = new CommerceIntegrationProperties();
        properties.getGhn().setEnabled(true);
        properties.getGhn().setToken("test-token");
        properties.getGhn().setShopId("885");
        adapter = new GhnServiceCatalogGatewayAdapter(
                properties,
                RestClient.builder().build(),
                new ObjectMapper()
        );
    }

    @Test
    void parseServices_readsServiceList() {
        String json = """
                {
                  "code": 200,
                  "message": "Success",
                  "data": [
                    {"service_id": 53319, "short_name": "Nhanh", "service_type_id": 1},
                    {"service_id": 53320, "short_name": "Chuan", "service_type_id": 2}
                  ]
                }
                """;

        List<GhnServiceOption> services = adapter.parseServices(json);

        assertThat(services).hasSize(2);
        assertThat(services.get(0).serviceId()).isEqualTo(53319);
        assertThat(services.get(1).serviceTypeId()).isEqualTo(2);
    }
}
