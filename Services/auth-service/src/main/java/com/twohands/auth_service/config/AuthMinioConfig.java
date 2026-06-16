package com.twohands.auth_service.config;

import lombok.extern.slf4j.Slf4j;
import io.minio.MinioClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("!test")
@ConditionalOnProperty(prefix = "auth.object-storage", name = "enabled", havingValue = "true")
@Slf4j
public class AuthMinioConfig {

    public static final String INTERNAL_MINIO_CLIENT = "authInternalMinioClient";
    public static final String PRESIGN_MINIO_CLIENT = "authPresignMinioClient";

    @Bean(name = INTERNAL_MINIO_CLIENT)
    public MinioClient authInternalMinioClient(AuthObjectStorageProperties properties) {
        log.info(
                "MinIO internal client configured. endpoint={}, bucket={}",
                properties.getEndpoint(),
                properties.getAvatarBucket()
        );
        return buildClient(properties.getEndpoint(), properties);
    }

    @Bean(name = PRESIGN_MINIO_CLIENT)
    public MinioClient authPresignMinioClient(AuthObjectStorageProperties properties) {
        log.info(
                "MinIO presign client configured. presignedEndpoint={}, publicUrl={}",
                properties.resolvePresignedEndpoint(),
                properties.getPublicUrl()
        );
        return buildClient(properties.resolvePresignedEndpoint(), properties);
    }

    private static MinioClient buildClient(String endpoint, AuthObjectStorageProperties properties) {
        return MinioClient.builder()
                .endpoint(endpoint)
                .credentials(properties.getAccessKey(), properties.getSecretKey())
                .build();
    }
}
