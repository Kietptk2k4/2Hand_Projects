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
        private String webhookSecret;
        private Integer defaultServiceTypeId = 2;
        private Integer defaultServiceId;
        private int defaultPackageLengthCm = 20;
        private int defaultPackageWidthCm = 20;
        private int defaultPackageHeightCm = 10;
        private int trackSyncCooldownSeconds = 300;
        private int printTokenTtlMinutes = 30;
        private String printBaseUrl;

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

        public String getWebhookSecret() {
            return webhookSecret;
        }

        public void setWebhookSecret(String webhookSecret) {
            this.webhookSecret = webhookSecret;
        }

        public Integer getDefaultServiceTypeId() {
            return defaultServiceTypeId;
        }

        public void setDefaultServiceTypeId(Integer defaultServiceTypeId) {
            this.defaultServiceTypeId = defaultServiceTypeId;
        }

        public Integer getDefaultServiceId() {
            return defaultServiceId;
        }

        public void setDefaultServiceId(Integer defaultServiceId) {
            this.defaultServiceId = defaultServiceId;
        }

        public int getDefaultPackageLengthCm() {
            return defaultPackageLengthCm;
        }

        public void setDefaultPackageLengthCm(int defaultPackageLengthCm) {
            this.defaultPackageLengthCm = defaultPackageLengthCm;
        }

        public int getDefaultPackageWidthCm() {
            return defaultPackageWidthCm;
        }

        public void setDefaultPackageWidthCm(int defaultPackageWidthCm) {
            this.defaultPackageWidthCm = defaultPackageWidthCm;
        }

        public int getDefaultPackageHeightCm() {
            return defaultPackageHeightCm;
        }

        public void setDefaultPackageHeightCm(int defaultPackageHeightCm) {
            this.defaultPackageHeightCm = defaultPackageHeightCm;
        }

        public int getTrackSyncCooldownSeconds() {
            return trackSyncCooldownSeconds;
        }

        public void setTrackSyncCooldownSeconds(int trackSyncCooldownSeconds) {
            this.trackSyncCooldownSeconds = trackSyncCooldownSeconds;
        }

        public int getPrintTokenTtlMinutes() {
            return printTokenTtlMinutes;
        }

        public void setPrintTokenTtlMinutes(int printTokenTtlMinutes) {
            this.printTokenTtlMinutes = printTokenTtlMinutes;
        }

        public String getPrintBaseUrl() {
            if (printBaseUrl != null && !printBaseUrl.isBlank()) {
                return trimSlash(printBaseUrl);
            }
            return trimSlash(baseUrl) + "/a5/public-api";
        }

        public void setPrintBaseUrl(String printBaseUrl) {
            this.printBaseUrl = printBaseUrl;
        }

        public boolean isWebhookVerificationEnabled() {
            return webhookSecret != null && !webhookSecret.isBlank();
        }

        public boolean isLiveClientConfigured() {
            return enabled
                    && token != null && !token.isBlank()
                    && shopId != null && !shopId.isBlank()
                    && baseUrl != null && !baseUrl.isBlank();
        }

        private static String trimSlash(String value) {
            if (value == null || value.isBlank()) {
                return "";
            }
            return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
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
