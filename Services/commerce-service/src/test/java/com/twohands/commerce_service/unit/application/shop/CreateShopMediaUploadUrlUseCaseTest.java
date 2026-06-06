package com.twohands.commerce_service.unit.application.shop;

import com.twohands.commerce_service.application.shop.uploadshopmedia.CreateShopMediaUploadUrlCommand;
import com.twohands.commerce_service.application.shop.uploadshopmedia.CreateShopMediaUploadUrlUseCase;
import com.twohands.commerce_service.application.shop.uploadshopmedia.CreateShopMediaUploadUrlValidationService;
import com.twohands.commerce_service.application.shop.uploadshopmedia.ShopMediaUploadIntent;
import com.twohands.commerce_service.application.shop.uploadshopmedia.ShopMediaUploadStoragePort;
import com.twohands.commerce_service.config.CommerceObjectStorageProperties;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateShopMediaUploadUrlUseCaseTest {

    @Mock
    private ShopMediaUploadStoragePort shopMediaUploadStoragePort;

    private CreateShopMediaUploadUrlUseCase useCase;
    private UUID sellerId;

    @BeforeEach
    void setUp() {
        CommerceObjectStorageProperties properties = new CommerceObjectStorageProperties();
        properties.setEnabled(true);
        properties.setShopMediaMaxFileSizeBytes(5_242_880L);
        properties.setPresignedUrlTtlSeconds(900);
        properties.setAllowedShopMediaContentTypes(List.of("image/jpeg", "image/png", "image/webp"));

        useCase = new CreateShopMediaUploadUrlUseCase(
                new CreateShopMediaUploadUrlValidationService(properties),
                properties,
                shopMediaUploadStoragePort
        );
        sellerId = UUID.randomUUID();
    }

    @Test
    void shouldIssueUploadUrlForShopAvatar() {
        Instant expiresAt = Instant.parse("2026-06-05T10:00:00Z");
        when(shopMediaUploadStoragePort.createUploadIntent(
                eq(sellerId),
                eq("image/jpeg"),
                eq("SHOP_AVATAR"),
                any(Instant.class)
        )).thenReturn(new ShopMediaUploadIntent(
                "https://minio/upload",
                "shops/" + sellerId + "/avatar/test.jpg",
                "http://localhost:9000/2hands-commerce-shop/shops/" + sellerId + "/avatar/test.jpg",
                "SHOP_AVATAR",
                expiresAt
        ));

        var result = useCase.execute(new CreateShopMediaUploadUrlCommand(
                sellerId,
                "image/jpeg",
                1024L,
                "SHOP_AVATAR"
        ));

        assertThat(result.mediaUrl()).contains("/2hands-commerce-shop/");
        assertThat(result.mediaKind()).isEqualTo("SHOP_AVATAR");
        verify(shopMediaUploadStoragePort).createUploadIntent(
                eq(sellerId),
                eq("image/jpeg"),
                eq("SHOP_AVATAR"),
                any(Instant.class)
        );
    }

    @Test
    void shouldRejectWhenMinioDisabled() {
        CommerceObjectStorageProperties properties = new CommerceObjectStorageProperties();
        properties.setEnabled(false);
        CreateShopMediaUploadUrlUseCase disabledUseCase = new CreateShopMediaUploadUrlUseCase(
                new CreateShopMediaUploadUrlValidationService(properties),
                properties,
                shopMediaUploadStoragePort
        );

        assertThatThrownBy(() -> disabledUseCase.execute(new CreateShopMediaUploadUrlCommand(
                sellerId,
                "image/jpeg",
                1024L,
                "SHOP_AVATAR"
        )))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.OBJECT_STORAGE_UNAVAILABLE);
    }
}
