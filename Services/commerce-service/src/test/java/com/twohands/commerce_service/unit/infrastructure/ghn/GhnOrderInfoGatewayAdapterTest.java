package com.twohands.commerce_service.unit.infrastructure.ghn;

import com.twohands.commerce_service.infrastructure.ghn.GhnOrderInfoGatewayAdapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.commerce_service.config.CommerceIntegrationProperties;
import com.twohands.commerce_service.domain.shipment.GhnOrderInfoResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;

class GhnOrderInfoGatewayAdapterTest {

    private GhnOrderInfoGatewayAdapter adapter;

    @BeforeEach
    void setUp() {
        CommerceIntegrationProperties properties = new CommerceIntegrationProperties();
        properties.getGhn().setEnabled(true);
        properties.getGhn().setToken("test-token");
        properties.getGhn().setShopId("885");
        adapter = new GhnOrderInfoGatewayAdapter(
                properties,
                RestClient.builder().build(),
                new ObjectMapper()
        );
    }

    @Test
    void parseDetailResponse_readsStatusFromArrayPayload() {
        String json = """
                {
                  "code": 200,
                  "message": "Success",
                  "data": [
                    {
                      "order_code": "5ENLKKHD",
                      "status": "picked"
                    }
                  ]
                }
                """;

        GhnOrderInfoResult result = adapter.parseDetailResponse(json);

        assertThat(result.orderCode()).isEqualTo("5ENLKKHD");
        assertThat(result.rawStatus()).isEqualTo("picked");
    }

    @Test
    void parseDetailResponse_readsStatusFromObjectPayload() {
        String json = """
                {
                  "code": 200,
                  "message": "Success",
                  "data": {
                    "order_code": "Z82BS",
                    "status": "delivering"
                  }
                }
                """;

        GhnOrderInfoResult result = adapter.parseDetailResponse(json);

        assertThat(result.orderCode()).isEqualTo("Z82BS");
        assertThat(result.rawStatus()).isEqualTo("delivering");
    }
}
