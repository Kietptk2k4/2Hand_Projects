package com.twohands.social_service.unit.application.post.uploadpostmedia;

import com.twohands.social_service.application.post.uploadpostmedia.UploadPostMediaValidationService;
import com.twohands.social_service.config.SocialObjectStorageProperties;
import com.twohands.social_service.exception.AppException;
import com.twohands.social_service.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UploadPostMediaValidationServiceTest {

    private SocialObjectStorageProperties properties;
    private UploadPostMediaValidationService validationService;

    @BeforeEach
    void setup() {
        properties = new SocialObjectStorageProperties();
        properties.setAllowedImageContentTypes(List.of("image/jpeg"));
        properties.setAllowedVideoContentTypes(List.of("video/mp4"));
        validationService = new UploadPostMediaValidationService(properties);
    }

    @Test
    void shouldReturnNullWhenClientUploadOriginOmitted() {
        assertThat(validationService.validateClientUploadOrigin(null)).isNull();
        assertThat(validationService.validateClientUploadOrigin("  ")).isNull();
    }

    @Test
    void shouldNormalizeLanClientUploadOriginWhenAllowed() {
        properties.setAllowClientUploadOrigin(true);

        assertThat(validationService.validateClientUploadOrigin("http://192.168.1.52:9000"))
                .isEqualTo("http://192.168.1.52:9000");
    }

    @Test
    void shouldRejectClientUploadOriginWhenFeatureDisabled() {
        assertThatThrownBy(() -> validationService.validateClientUploadOrigin("http://192.168.1.52:9000"))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> {
                    AppException appEx = (AppException) ex;
                    assertThat(appEx.getErrorCode()).isEqualTo(ErrorCode.VALIDATION_ERROR);
                    assertThat(appEx.getField()).isEqualTo("client_upload_origin");
                    assertThat(appEx.getReason()).isEqualTo("NOT_ALLOWED");
                });
    }

    @Test
    void shouldRejectPublicClientUploadOrigin() {
        properties.setAllowClientUploadOrigin(true);

        assertThatThrownBy(() -> validationService.validateClientUploadOrigin("https://evil.example.com:9000"))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getField()).isEqualTo("client_upload_origin"));
    }
}
