package com.twohands.commerce_service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "commerce.object-storage")
public class CommerceObjectStorageProperties {

    private boolean enabled;
    private String endpoint = "http://localhost:9000";
    private String publicUrl = "http://localhost:9000";
    private String accessKey = "admin";
    private String secretKey = "password123";
    private String shopBucket = "2hands-commerce-shop";
    private String productBucket = "2hands-commerce-product";
    private String reviewBucket = "2hands-commerce-review";

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
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

    public String getProductBucket() {
        return productBucket;
    }

    public void setProductBucket(String productBucket) {
        this.productBucket = productBucket;
    }

    public String getReviewBucket() {
        return reviewBucket;
    }

    public void setReviewBucket(String reviewBucket) {
        this.reviewBucket = reviewBucket;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }
}
