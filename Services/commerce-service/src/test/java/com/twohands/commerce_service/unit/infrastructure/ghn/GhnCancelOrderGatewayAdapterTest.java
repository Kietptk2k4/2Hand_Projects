package com.twohands.commerce_service.unit.infrastructure.ghn;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.commerce_service.config.CommerceIntegrationProperties;
import com.twohands.commerce_service.infrastructure.ghn.GhnCancelOrderGatewayAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThatCode;

class GhnCancelOrderGatewayAdapterTest {

    private GhnCancelOrderGatewayAdapter adapter;

    @BeforeEach
    void setUp() {
        CommerceIntegrationProperties properties = new CommerceIntegrationProperties();
        properties.getGhn().setEnabled(true);
        properties.getGhn().setToken("test-token");
        properties.getGhn().setShopId("885");
        adapter = new GhnCancelOrderGatewayAdapter(
                properties,
                RestClient.builder().build(),
                new ObjectMapper()
        );
    }

    @Test
    void parseCancelResponse_acceptsSuccessfulCancel() {
        String json = """
                {
                  "code": 200,
                  "message": "Success",
                  "data": [
                    {"order_code": "5E3NK3RS", "result": true, "message": "OK"}
                  ]
                }
                """;

        assertThatCode(() -> adapter.parseCancelResponse(json, "5E3NK3RS")).doesNotThrowAnyException();
    }
}
