package com.twohands.commerce_service.unit.common.media;

import com.twohands.commerce_service.common.media.CommerceClientUploadOriginValidator;
import com.twohands.commerce_service.config.CommerceObjectStorageProperties;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CommerceClientUploadOriginValidatorTest {

    private CommerceObjectStorageProperties properties;
    private CommerceClientUploadOriginValidator validator;

    @BeforeEach
    void setUp() {
        properties = new CommerceObjectStorageProperties();
        properties.setAllowClientUploadOrigin(true);
        validator = new CommerceClientUploadOriginValidator(properties);
    }

    @Test
    void shouldReturnNullWhenOmitted() {
        assertThat(validator.validate(null)).isNull();
        assertThat(validator.validate("  ")).isNull();
    }

    @Test
    void shouldNormalizeLanOrigin() {
        assertThat(validator.validate("http://192.168.1.52:9000"))
                .isEqualTo("http://192.168.1.52:9000");
    }

    @Test
    void shouldRejectWhenNotAllowed() {
        properties.setAllowClientUploadOrigin(false);

        assertThatThrownBy(() -> validator.validate("http://192.168.1.52:9000"))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> {
                    AppException appEx = (AppException) ex;
                    assertThat(appEx.getErrorCode()).isEqualTo(ErrorCode.VALIDATION_ERROR);
                    assertThat(appEx.getField()).isEqualTo("client_upload_origin");
                });
    }

    @Test
    void shouldRejectPublicHost() {
        assertThatThrownBy(() -> validator.validate("http://example.com:9000"))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getField()).isEqualTo("client_upload_origin"));
    }
}
