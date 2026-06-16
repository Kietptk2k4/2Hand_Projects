package com.twohands.commerce_service.config;

import io.minio.MinioClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(prefix = "commerce.object-storage", name = "enabled", havingValue = "true")
public class CommerceMinioConfig {

    public static final String INTERNAL_MINIO_CLIENT = "commerceInternalMinioClient";
    public static final String PRESIGN_MINIO_CLIENT = "commercePresignMinioClient";

    @Bean(name = INTERNAL_MINIO_CLIENT)
    public MinioClient commerceInternalMinioClient(CommerceObjectStorageProperties properties) {
        return buildClient(properties.getEndpoint(), properties);
    }

    @Bean(name = PRESIGN_MINIO_CLIENT)
    public MinioClient commercePresignMinioClient(CommerceObjectStorageProperties properties) {
        return buildClient(properties.resolvePresignedEndpoint(), properties);
    }

    private static MinioClient buildClient(String endpoint, CommerceObjectStorageProperties properties) {
        return MinioClient.builder()
                .endpoint(endpoint)
                .credentials(properties.getAccessKey(), properties.getSecretKey())
                .build();
    }
}
