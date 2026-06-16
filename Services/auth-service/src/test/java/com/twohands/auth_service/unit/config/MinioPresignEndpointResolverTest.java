package com.twohands.auth_service.unit.config;

import com.twohands.auth_service.config.MinioPresignEndpointResolver;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MinioPresignEndpointResolverTest {

    @Test
    void explicitPresignedEndpointTakesPrecedence() {
        assertThat(MinioPresignEndpointResolver.resolve(
                "http://minio:9000",
                "http://custom:9000",
                "http://localhost:9000/bucket"
        )).isEqualTo("http://custom:9000");
    }

    @Test
    void localInternalEndpointStaysUnchanged() {
        assertThat(MinioPresignEndpointResolver.resolve(
                "http://localhost:9000",
                "",
                "http://localhost:9000/bucket"
        )).isEqualTo("http://localhost:9000");
    }

    @Test
    void dockerInternalHostnameUsesPublicUrlOrigin() {
        assertThat(MinioPresignEndpointResolver.resolve(
                "http://minio:9000",
                "",
                "http://localhost:9000/2hands-avatar"
        )).isEqualTo("http://localhost:9000");
    }
}
