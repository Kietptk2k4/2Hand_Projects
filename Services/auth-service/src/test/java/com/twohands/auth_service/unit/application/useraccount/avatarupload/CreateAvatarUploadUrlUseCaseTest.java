package com.twohands.auth_service.unit.application.useraccount.avatarupload;

import com.twohands.auth_service.application.useraccount.avatarupload.AvatarUploadIntent;
import com.twohands.auth_service.application.useraccount.avatarupload.AvatarUploadRateLimitService;
import com.twohands.auth_service.application.useraccount.avatarupload.AvatarUploadStoragePort;
import com.twohands.auth_service.application.useraccount.avatarupload.CreateAvatarUploadUrlCommand;
import com.twohands.auth_service.application.useraccount.avatarupload.CreateAvatarUploadUrlResult;
import com.twohands.auth_service.application.useraccount.avatarupload.CreateAvatarUploadUrlUseCase;
import com.twohands.auth_service.application.useraccount.avatarupload.CreateAvatarUploadUrlValidationService;
import com.twohands.auth_service.application.useraccount.common.UserAccountAuthContextService;
import com.twohands.auth_service.config.AuthObjectStorageProperties;
import com.twohands.auth_service.domain.user.EmailAddress;
import com.twohands.auth_service.domain.user.PasswordHash;
import com.twohands.auth_service.domain.user.User;
import com.twohands.auth_service.domain.user.UserRepository;
import com.twohands.auth_service.domain.user.UserStatus;
import com.twohands.auth_service.exception.AppException;
import com.twohands.auth_service.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CreateAvatarUploadUrlUseCaseTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final AvatarUploadRateLimitService rateLimitService = mock(AvatarUploadRateLimitService.class);
    private final AvatarUploadStoragePort storagePort = mock(AvatarUploadStoragePort.class);
    private final AuthObjectStorageProperties properties = new AuthObjectStorageProperties();

    private CreateAvatarUploadUrlUseCase useCase;

    @BeforeEach
    void setup() {
        properties.setEnabled(true);
        properties.setAvatarMaxFileSizeBytes(5_242_880L);
        properties.setPresignedUrlTtlSeconds(900);
        properties.setAllowedAvatarContentTypes(List.of("image/jpeg", "image/png", "image/webp"));

        useCase = new CreateAvatarUploadUrlUseCase(
                new CreateAvatarUploadUrlValidationService(properties),
                userRepository,
                new UserAccountAuthContextService(),
                rateLimitService,
                properties,
                storagePort
        );
    }

    @Test
    void shouldReturnUploadIntentForActiveUser() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.of(buildUser(userId, UserStatus.ACTIVE)));
        Instant expiresAt = Instant.parse("2026-05-21T10:15:00Z");
        when(storagePort.createUploadIntent(eq(userId), eq("image/png"), any(Instant.class)))
                .thenReturn(new AvatarUploadIntent(
                        "https://minio.local/presigned",
                        "avatars/" + userId + "/file.png",
                        "https://cdn.2hands.vn/avatars/" + userId + "/file.png",
                        expiresAt
                ));

        CreateAvatarUploadUrlResult result = useCase.execute(
                new CreateAvatarUploadUrlCommand(userId, "image/png", 1_048_576L)
        );

        assertThat(result.uploadUrl()).contains("presigned");
        assertThat(result.objectKey()).startsWith("avatars/" + userId);
        assertThat(result.avatarUrl()).contains("cdn.2hands.vn");
        assertThat(result.allowedContentTypes()).contains("image/png");
        verify(rateLimitService).validateUploadUrlRequest(userId);
    }

    @Test
    void shouldRejectOversizedFile() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.of(buildUser(userId, UserStatus.ACTIVE)));

        assertThatThrownBy(() -> useCase.execute(
                new CreateAvatarUploadUrlCommand(userId, "image/jpeg", 6_000_000L)
        ))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> {
                    AppException appEx = (AppException) ex;
                    assertThat(appEx.getErrorCode()).isEqualTo(ErrorCode.VALIDATION_ERROR);
                    assertThat(appEx.getField()).isEqualTo("file_size_bytes");
                });
    }

    @Test
    void shouldRejectWhenObjectStorageDisabled() {
        properties.setEnabled(false);
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.of(buildUser(userId, UserStatus.ACTIVE)));

        assertThatThrownBy(() -> useCase.execute(
                new CreateAvatarUploadUrlCommand(userId, "image/jpeg", 1024L)
        ))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.OBJECT_STORAGE_UNAVAILABLE));
    }

    private User buildUser(UUID userId, UserStatus status) {
        Instant now = Instant.now();
        return User.rehydrate(
                userId,
                EmailAddress.of("user@example.com"),
                PasswordHash.of("$2a$10$abcdefghijklmnopqrstuv"),
                status,
                true,
                false,
                null,
                null,
                null,
                now,
                now
        );
    }
}
