package com.twohands.commerce_service.unit.infrastructure.ghn;

import com.twohands.commerce_service.infrastructure.ghn.GhnShipmentGatewayAdapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.commerce_service.config.CommerceIntegrationProperties;
import com.twohands.commerce_service.domain.shipment.GhnCreateOrderCommand;
import com.twohands.commerce_service.domain.shipment.GhnCreateOrderItem;
import com.twohands.commerce_service.domain.shipment.GhnCreateOrderResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class GhnShipmentGatewayAdapterTest {

    private GhnShipmentGatewayAdapter adapter;

    @BeforeEach
    void setUp() {
        CommerceIntegrationProperties properties = new CommerceIntegrationProperties();
        properties.getGhn().setEnabled(true);
        properties.getGhn().setToken("test-token");
        properties.getGhn().setShopId("885");
        adapter = new GhnShipmentGatewayAdapter(
                properties,
                RestClient.builder().build(),
                new ObjectMapper()
        );
    }

    @Test
    void parseCreateResponse_readsOrderCodeAsTracking() {
        String json = """
                {
                  "code": 200,
                  "message": "Success",
                  "data": {
                    "order_code": "5ENLKKHD",
                    "expected_delivery_time": "2026-06-10"
                  }
                }
                """;

        GhnCreateOrderResult result = adapter.parseCreateResponse(json);

        assertThat(result.ghnOrderCode()).isEqualTo("5ENLKKHD");
        assertThat(result.trackingNumber()).isEqualTo("5ENLKKHD");
        assertThat(result.mockProvider()).isFalse();
    }

    @Test
    void buildItemsPayload_includesOrderLines() {
        UUID shipmentId = UUID.randomUUID();
        GhnCreateOrderCommand command = new GhnCreateOrderCommand(
                shipmentId,
                UUID.randomUUID(),
                0,
                1200,
                53320,
                2,
                20,
                20,
                10,
                "Buyer",
                "0900000000",
                "1442",
                "20309",
                "Buyer address",
                "Seller",
                "0901111111",
                "1444",
                "20308",
                "Seller address",
                "Test Product",
                List.of(new GhnCreateOrderItem("Test Product", "SKU-1", 1, 100_000, 1200))
        );

        List<Map<String, Object>> items = adapter.buildItemsPayload(command);

        assertThat(items).hasSize(1);
        assertThat(items.getFirst().get("name")).isEqualTo("Test Product");
        assertThat(items.getFirst().get("code")).isEqualTo("SKU-1");
    }
}
