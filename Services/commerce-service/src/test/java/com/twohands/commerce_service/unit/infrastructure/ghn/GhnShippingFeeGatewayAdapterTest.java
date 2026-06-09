package com.twohands.commerce_service.unit.infrastructure.ghn;

import com.twohands.commerce_service.infrastructure.ghn.GhnShippingFeeGatewayAdapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.commerce_service.config.CommerceIntegrationProperties;
import com.twohands.commerce_service.domain.shipment.GhnShippingFeeResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class GhnShippingFeeGatewayAdapterTest {

    private GhnShippingFeeGatewayAdapter adapter;

    @BeforeEach
    void setUp() {
        CommerceIntegrationProperties properties = new CommerceIntegrationProperties();
        properties.getGhn().setEnabled(true);
        properties.getGhn().setToken("test-token");
        properties.getGhn().setShopId("885");
        adapter = new GhnShippingFeeGatewayAdapter(
                properties,
                RestClient.builder().build(),
                new ObjectMapper()
        );
    }

    @Test
    void parseFeeResponse_readsTotalFee() {
        String json = """
                {
                  "code": 200,
                  "message": "Success",
                  "data": {
                    "total": 36300,
                    "service_fee": 36300,
                    "insurance_fee": 0
                  }
                }
                """;

        GhnShippingFeeResult result = adapter.parseFeeResponse(json);

        assertThat(result.totalFee()).isEqualByComparingTo(BigDecimal.valueOf(36_300));
        assertThat(result.serviceFee()).isEqualByComparingTo(BigDecimal.valueOf(36_300));
        assertThat(result.mock()).isFalse();
    }

    @Test
    void parseFeeResponse_fallsBackToServiceFeeWhenTotalMissing() {
        String json = """
                {
                  "code": 200,
                  "message": "Success",
                  "data": {
                    "service_fee": 25000
                  }
                }
                """;

        GhnShippingFeeResult result = adapter.parseFeeResponse(json);

        assertThat(result.totalFee()).isEqualByComparingTo(BigDecimal.valueOf(25_000));
    }
}
