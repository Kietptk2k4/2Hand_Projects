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
    }

    public static class Payos {
        private boolean enabled;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }
}
