package com.twohands.commerce_service.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
@EnableConfigurationProperties(AdminIntegrationProperties.class)
public class AdminIntegrationConfig {

    public static final String ADMIN_REST_CLIENT = "adminRestClient";

    @Bean
    @Qualifier(ADMIN_REST_CLIENT)
    public RestClient adminRestClient(AdminIntegrationProperties properties) {
        String baseUrl = properties.isConfigured() ? properties.getBaseUrl() : "http://127.0.0.1";
        return RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory(properties))
                .build();
    }

    private org.springframework.http.client.SimpleClientHttpRequestFactory requestFactory(
            AdminIntegrationProperties properties
    ) {
        org.springframework.http.client.SimpleClientHttpRequestFactory factory =
                new org.springframework.http.client.SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofMillis(properties.getConnectTimeoutMs()));
        factory.setReadTimeout(Duration.ofMillis(properties.getReadTimeoutMs()));
        return factory;
    }
}
