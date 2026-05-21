package com.twohands.commerce_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class PayosClientConfig {

    @Bean
    public RestClient payosRestClient(CommerceIntegrationProperties integrationProperties) {
        CommerceIntegrationProperties.Payos payos = integrationProperties.getPayos();
        RestClient.Builder builder = RestClient.builder()
                .baseUrl(trimTrailingSlash(payos.getBaseUrl()));

        if (payos.isLiveClientConfigured()) {
            builder.defaultHeaders(headers -> {
                headers.set("x-client-id", payos.getClientId());
                headers.set("x-api-key", payos.getApiKey());
            });
        }

        return builder.build();
    }

    private String trimTrailingSlash(String baseUrl) {
        if (baseUrl == null || baseUrl.isBlank()) {
            return "https://api-merchant.payos.vn";
        }
        return baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }
}
