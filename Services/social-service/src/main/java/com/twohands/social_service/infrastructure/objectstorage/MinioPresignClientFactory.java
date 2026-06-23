package com.twohands.social_service.infrastructure.objectstorage;

import com.twohands.social_service.config.SocialObjectStorageProperties;
import io.minio.MinioClient;
import org.springframework.stereotype.Component;

@Component
public class MinioPresignClientFactory {

    private final SocialObjectStorageProperties properties;

    public MinioPresignClientFactory(SocialObjectStorageProperties properties) {
        this.properties = properties;
    }

    public MinioClient createForEndpoint(String endpoint) {
        return MinioClient.builder()
                .endpoint(endpoint)
                .credentials(properties.getAccessKey(), properties.getSecretKey())
                .build();
    }
}
