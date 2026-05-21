package com.twohands.commerce_service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "commerce.object-storage")
public class CommerceObjectStorageProperties {

    private boolean enabled;
    private String publicUrl = "http://localhost:9000";
    private String shopBucket = "2hands-commerce-shop";

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getPublicUrl() {
        return publicUrl;
    }

    public void setPublicUrl(String publicUrl) {
        this.publicUrl = publicUrl;
    }

    public String getShopBucket() {
        return shopBucket;
    }

    public void setShopBucket(String shopBucket) {
        this.shopBucket = shopBucket;
    }
}
