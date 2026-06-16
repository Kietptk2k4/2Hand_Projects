package com.twohands.commerce_service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "commerce.object-storage")
public class CommerceObjectStorageProperties {

    private boolean enabled;
    private String endpoint = "http://localhost:9000";
    private String presignedEndpoint = "";
    private String publicUrl = "http://localhost:9000";
    private String accessKey = "admin";
    private String secretKey = "password123";
    private String shopBucket = "2hands-commerce-shop";
    private String productBucket = "2hands-commerce-product";
    private String reviewBucket = "2hands-commerce-review";
    private long shopMediaMaxFileSizeBytes = 5_242_880L;
    private long productMediaMaxFileSizeBytes = 5_242_880L;
    private long productMediaMaxVideoFileSizeBytes = 52_428_800L;
    private int productMediaMaxCount = 10;
    private int productMediaMaxVideoCount = 1;
    private int presignedUrlTtlSeconds = 900;
    private List<String> allowedShopMediaContentTypes = List.of(
            "image/jpeg",
            "image/png",
            "image/webp"
    );
    private List<String> allowedProductMediaContentTypes = List.of(
            "image/jpeg",
            "image/png",
            "image/webp",
            "video/mp4",
            "video/webm"
    );

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

    public String getPresignedEndpoint() {
        return presignedEndpoint;
    }

    public void setPresignedEndpoint(String presignedEndpoint) {
        this.presignedEndpoint = presignedEndpoint;
    }

    public String resolvePresignedEndpoint() {
        return MinioPresignEndpointResolver.resolve(endpoint, presignedEndpoint, publicUrl);
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

    public long getShopMediaMaxFileSizeBytes() {
        return shopMediaMaxFileSizeBytes;
    }

    public void setShopMediaMaxFileSizeBytes(long shopMediaMaxFileSizeBytes) {
        this.shopMediaMaxFileSizeBytes = shopMediaMaxFileSizeBytes;
    }

    public int getPresignedUrlTtlSeconds() {
        return presignedUrlTtlSeconds;
    }

    public void setPresignedUrlTtlSeconds(int presignedUrlTtlSeconds) {
        this.presignedUrlTtlSeconds = presignedUrlTtlSeconds;
    }

    public List<String> getAllowedShopMediaContentTypes() {
        return allowedShopMediaContentTypes;
    }

    public void setAllowedShopMediaContentTypes(List<String> allowedShopMediaContentTypes) {
        this.allowedShopMediaContentTypes = allowedShopMediaContentTypes;
    }

    public long getProductMediaMaxFileSizeBytes() {
        return productMediaMaxFileSizeBytes;
    }

    public void setProductMediaMaxFileSizeBytes(long productMediaMaxFileSizeBytes) {
        this.productMediaMaxFileSizeBytes = productMediaMaxFileSizeBytes;
    }

    public long getProductMediaMaxVideoFileSizeBytes() {
        return productMediaMaxVideoFileSizeBytes;
    }

    public void setProductMediaMaxVideoFileSizeBytes(long productMediaMaxVideoFileSizeBytes) {
        this.productMediaMaxVideoFileSizeBytes = productMediaMaxVideoFileSizeBytes;
    }

    public int getProductMediaMaxCount() {
        return productMediaMaxCount;
    }

    public void setProductMediaMaxCount(int productMediaMaxCount) {
        this.productMediaMaxCount = productMediaMaxCount;
    }

    public int getProductMediaMaxVideoCount() {
        return productMediaMaxVideoCount;
    }

    public void setProductMediaMaxVideoCount(int productMediaMaxVideoCount) {
        this.productMediaMaxVideoCount = productMediaMaxVideoCount;
    }

    public List<String> getAllowedProductMediaContentTypes() {
        return allowedProductMediaContentTypes;
    }

    public void setAllowedProductMediaContentTypes(List<String> allowedProductMediaContentTypes) {
        this.allowedProductMediaContentTypes = allowedProductMediaContentTypes;
    }
}
