package com.twohands.commerce_service.unit.infrastructure.ghn;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.commerce_service.config.CommerceIntegrationProperties;
import com.twohands.commerce_service.domain.shipment.GhnLeadtimeResult;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import com.twohands.commerce_service.infrastructure.ghn.GhnLeadtimeGatewayAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GhnLeadtimeGatewayAdapterTest {

    private GhnLeadtimeGatewayAdapter adapter;

    @BeforeEach
    void setUp() {
        CommerceIntegrationProperties properties = new CommerceIntegrationProperties();
        properties.getGhn().setEnabled(true);
        properties.getGhn().setToken("test-token");
        properties.getGhn().setShopId("885");
        adapter = new GhnLeadtimeGatewayAdapter(
                properties,
                RestClient.builder().build(),
                new ObjectMapper(),
                Clock.fixed(Instant.parse("2026-06-07T10:00:00Z"), ZoneOffset.UTC)
        );
    }

    @Test
    void parseLeadtimeResponse_readsUnixLeadtimeAsLocalDate() {
        // 2026-06-15T00:00:00Z
        String json = """
                {
                  "code": 200,
                  "message": "Success",
                  "data": {
                    "leadtime": 1781481600,
                    "order_date": 1781310000
                  }
                }
                """;

        GhnLeadtimeResult result = adapter.parseLeadtimeResponse(json);

        assertThat(result.estimatedDeliveryDate()).isEqualTo(LocalDate.of(2026, 6, 15));
        assertThat(result.rawResponse()).contains("1781481600");
    }

    @Test
    void parseLeadtimeResponse_rejectsMissingLeadtime() {
        String json = """
                {
                  "code": 200,
                  "message": "Success",
                  "data": {}
                }
                """;

        assertThatThrownBy(() -> adapter.parseLeadtimeResponse(json))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.GHN_PROVIDER_UNAVAILABLE);
    }

    @Test
    void parseLeadtimeResponse_rejectsNonSuccessCode() {
        String json = """
                {
                  "code": 400,
                  "message": "Invalid ward",
                  "data": null
                }
                """;

        assertThatThrownBy(() -> adapter.parseLeadtimeResponse(json))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.GHN_PROVIDER_UNAVAILABLE);
    }
}
