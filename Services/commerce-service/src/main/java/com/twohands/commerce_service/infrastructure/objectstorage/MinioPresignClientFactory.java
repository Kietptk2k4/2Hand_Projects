package com.twohands.commerce_service.infrastructure.objectstorage;

import com.twohands.commerce_service.config.CommerceObjectStorageProperties;
import io.minio.MinioClient;
import org.springframework.stereotype.Component;

@Component
public class MinioPresignClientFactory {

    private final CommerceObjectStorageProperties properties;

    public MinioPresignClientFactory(CommerceObjectStorageProperties properties) {
        this.properties = properties;
    }

    public MinioClient createForEndpoint(String endpoint) {
        return MinioClient.builder()
                .endpoint(endpoint)
                .credentials(properties.getAccessKey(), properties.getSecretKey())
                .build();
    }
}
