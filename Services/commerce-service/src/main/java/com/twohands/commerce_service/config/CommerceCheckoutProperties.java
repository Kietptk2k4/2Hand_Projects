package com.twohands.commerce_service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "commerce.checkout")
public class CommerceCheckoutProperties {

    /**
     * When true, checkout accepts COD only (Phase 1 — PayOS temporarily disabled).
     */
    private boolean codOnlyEnabled = true;

    public boolean isCodOnlyEnabled() {
        return codOnlyEnabled;
    }

    public void setCodOnlyEnabled(boolean codOnlyEnabled) {
        this.codOnlyEnabled = codOnlyEnabled;
    }
}
