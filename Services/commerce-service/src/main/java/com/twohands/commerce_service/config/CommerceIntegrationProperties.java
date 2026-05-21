package com.twohands.commerce_service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "commerce.integrations")
public class CommerceIntegrationProperties {

    private final Ghn ghn = new Ghn();
    private final Payos payos = new Payos();

    public Ghn getGhn() {
        return ghn;
    }

    public Payos getPayos() {
        return payos;
    }

    public static class Ghn {
        private boolean enabled;
        private boolean mockFallbackEnabled = true;
        private String baseUrl = "https://dev-online-gateway.ghn.vn";
        private String token;
        private String shopId;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public boolean isMockFallbackEnabled() {
            return mockFallbackEnabled;
        }

        public void setMockFallbackEnabled(boolean mockFallbackEnabled) {
            this.mockFallbackEnabled = mockFallbackEnabled;
        }

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }

        public String getShopId() {
            return shopId;
        }

        public void setShopId(String shopId) {
            this.shopId = shopId;
        }

        public boolean isLiveClientConfigured() {
            return enabled
                    && token != null && !token.isBlank()
                    && shopId != null && !shopId.isBlank()
                    && baseUrl != null && !baseUrl.isBlank();
        }
    }

    public static class Payos {
        private boolean enabled;
        private String baseUrl = "https://api-merchant.payos.vn";
        private String clientId;
        private String apiKey;
        private String checksumKey;
        private String returnUrl;
        private String cancelUrl;
        private boolean mockFallbackEnabled = true;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }

        public String getClientId() {
            return clientId;
        }

        public void setClientId(String clientId) {
            this.clientId = clientId;
        }

        public String getApiKey() {
            return apiKey;
        }

        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }

        public String getChecksumKey() {
            return checksumKey;
        }

        public void setChecksumKey(String checksumKey) {
            this.checksumKey = checksumKey;
        }

        public String getReturnUrl() {
            return returnUrl;
        }

        public void setReturnUrl(String returnUrl) {
            this.returnUrl = returnUrl;
        }

        public String getCancelUrl() {
            return cancelUrl;
        }

        public void setCancelUrl(String cancelUrl) {
            this.cancelUrl = cancelUrl;
        }

        public boolean isMockFallbackEnabled() {
            return mockFallbackEnabled;
        }

        public void setMockFallbackEnabled(boolean mockFallbackEnabled) {
            this.mockFallbackEnabled = mockFallbackEnabled;
        }

        public boolean isLiveClientConfigured() {
            return enabled
                    && clientId != null && !clientId.isBlank()
                    && apiKey != null && !apiKey.isBlank()
                    && checksumKey != null && !checksumKey.isBlank()
                    && returnUrl != null && !returnUrl.isBlank()
                    && cancelUrl != null && !cancelUrl.isBlank();
        }
    }
}
