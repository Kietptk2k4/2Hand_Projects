package com.twohands.commerce_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class GhnClientConfig {

    @Bean
    public RestClient ghnRestClient(CommerceIntegrationProperties integrationProperties) {
        CommerceIntegrationProperties.Ghn ghn = integrationProperties.getGhn();
        RestClient.Builder builder = RestClient.builder()
                .baseUrl(trimTrailingSlash(ghn.getBaseUrl()));

        if (ghn.isLiveClientConfigured()) {
            builder.defaultHeaders(headers -> headers.set("Token", ghn.getToken()));
        }

        return builder.build();
    }

    private String trimTrailingSlash(String baseUrl) {
        if (baseUrl == null || baseUrl.isBlank()) {
            return "https://dev-online-gateway.ghn.vn";
        }
        return baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }
}
