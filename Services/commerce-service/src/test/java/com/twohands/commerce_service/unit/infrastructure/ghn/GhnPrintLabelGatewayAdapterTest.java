package com.twohands.commerce_service.unit.infrastructure.ghn;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.commerce_service.config.CommerceIntegrationProperties;
import com.twohands.commerce_service.infrastructure.ghn.GhnPrintLabelGatewayAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;

class GhnPrintLabelGatewayAdapterTest {

    private GhnPrintLabelGatewayAdapter adapter;

    @BeforeEach
    void setUp() {
        CommerceIntegrationProperties properties = new CommerceIntegrationProperties();
        properties.getGhn().setEnabled(true);
        properties.getGhn().setToken("test-token");
        properties.getGhn().setShopId("885");
        adapter = new GhnPrintLabelGatewayAdapter(
                properties,
                RestClient.builder().build(),
                new ObjectMapper()
        );
    }

    @Test
    void parsePrintToken_readsToken() {
        String json = """
                {
                  "code": 200,
                  "message": "Success",
                  "data": {
                    "token": "e27db030-a1bf-11ea-b421-6a186c15e40e"
                  }
                }
                """;

        String token = adapter.parsePrintToken(json);

        assertThat(token).isEqualTo("e27db030-a1bf-11ea-b421-6a186c15e40e");
    }
}
