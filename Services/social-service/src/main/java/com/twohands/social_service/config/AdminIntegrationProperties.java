package com.twohands.social_service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "social.admin")
public class AdminIntegrationProperties {

    /**
     * Admin service base URL (e.g. http://localhost:3005). Empty disables Admin HTTP lookups.
     */
    private String baseUrl = "";

    private String serviceToken = "";

    private int connectTimeoutMs = 3000;

    private int readTimeoutMs = 5000;

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getServiceToken() {
        return serviceToken;
    }

    public void setServiceToken(String serviceToken) {
        this.serviceToken = serviceToken;
    }

    public int getConnectTimeoutMs() {
        return connectTimeoutMs;
    }

    public void setConnectTimeoutMs(int connectTimeoutMs) {
        this.connectTimeoutMs = connectTimeoutMs;
    }

    public int getReadTimeoutMs() {
        return readTimeoutMs;
    }

    public void setReadTimeoutMs(int readTimeoutMs) {
        this.readTimeoutMs = readTimeoutMs;
    }

    public boolean isConfigured() {
        return baseUrl != null && !baseUrl.isBlank();
    }
}
