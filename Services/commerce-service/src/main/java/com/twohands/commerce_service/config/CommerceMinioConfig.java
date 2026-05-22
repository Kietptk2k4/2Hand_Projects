package com.twohands.commerce_service.config;

import io.minio.MinioClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(prefix = "commerce.object-storage", name = "enabled", havingValue = "true")
public class CommerceMinioConfig {

    @Bean
    public MinioClient commerceMinioClient(CommerceObjectStorageProperties properties) {
        return MinioClient.builder()
                .endpoint(properties.getEndpoint())
                .credentials(properties.getAccessKey(), properties.getSecretKey())
                .build();
    }
}
