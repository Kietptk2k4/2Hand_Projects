package com.twohands.social_service.config;

import io.minio.MinioClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("!test")
@ConditionalOnProperty(prefix = "social.object-storage", name = "enabled", havingValue = "true")
public class SocialMinioConfig {

    public static final String INTERNAL_MINIO_CLIENT = "socialInternalMinioClient";
    public static final String PRESIGN_MINIO_CLIENT = "socialPresignMinioClient";

    @Bean(name = INTERNAL_MINIO_CLIENT)
    public MinioClient socialInternalMinioClient(SocialObjectStorageProperties properties) {
        return buildClient(properties.getEndpoint(), properties);
    }

    @Bean(name = PRESIGN_MINIO_CLIENT)
    public MinioClient socialPresignMinioClient(SocialObjectStorageProperties properties) {
        return buildClient(properties.resolvePresignedEndpoint(), properties);
    }

    private static MinioClient buildClient(String endpoint, SocialObjectStorageProperties properties) {
        return MinioClient.builder()
                .endpoint(endpoint)
                .credentials(properties.getAccessKey(), properties.getSecretKey())
                .build();
    }
}
