package com.twohands.social_service.unit.application.post.uploadpostmedia;

import com.twohands.social_service.application.post.uploadpostmedia.PostMediaUploadIntent;
import com.twohands.social_service.application.post.uploadpostmedia.PostMediaUploadRateLimitService;
import com.twohands.social_service.application.post.uploadpostmedia.PostMediaUploadStoragePort;
import com.twohands.social_service.application.post.uploadpostmedia.UploadPostMediaCommand;
import com.twohands.social_service.application.post.uploadpostmedia.UploadPostMediaResult;
import com.twohands.social_service.application.post.uploadpostmedia.UploadPostMediaUseCase;
import com.twohands.social_service.application.post.uploadpostmedia.UploadPostMediaValidationService;
import com.twohands.social_service.application.user.common.UserWriteGuard;
import com.twohands.social_service.config.SocialObjectStorageProperties;
import com.twohands.social_service.domain.user.UserProjectionRepository;
import com.twohands.social_service.exception.AppException;
import com.twohands.social_service.exception.ErrorCode;
import com.twohands.social_service.testsupport.UserProjectionTestFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UploadPostMediaUseCaseTest {

    private final UserProjectionRepository userProjectionRepository = mock(UserProjectionRepository.class);
    private final UserWriteGuard userWriteGuard = new UserWriteGuard(userProjectionRepository);
    private final PostMediaUploadRateLimitService rateLimitService = mock(PostMediaUploadRateLimitService.class);
    private final PostMediaUploadStoragePort storagePort = mock(PostMediaUploadStoragePort.class);
    private final SocialObjectStorageProperties properties = new SocialObjectStorageProperties();

    private UploadPostMediaUseCase useCase;

    @BeforeEach
    void setup() {
        properties.setEnabled(true);
        properties.setImageMaxFileSizeBytes(10_485_760L);
        properties.setVideoMaxFileSizeBytes(104_857_600L);
        properties.setPresignedUrlTtlSeconds(900);
        properties.setAllowedImageContentTypes(List.of("image/jpeg", "image/png", "image/webp"));
        properties.setAllowedVideoContentTypes(List.of("video/mp4"));
        properties.setPublicUrl("https://cdn.2hands.vn");
        properties.setPublicPathPrefix("social");

        useCase = new UploadPostMediaUseCase(
                userWriteGuard,
                new UploadPostMediaValidationService(properties),
                rateLimitService,
                properties,
                storagePort
        );
    }

    @Test
    void shouldReturnUploadIntentForActiveUser() {
        UUID userId = UUID.randomUUID();
        when(userProjectionRepository.findByUserId(userId)).thenReturn(UserProjectionTestFixtures.activeOptional(userId));
        Instant expiresAt = Instant.parse("2026-05-21T10:15:00Z");
        when(storagePort.createUploadIntent(eq(userId), eq("image/png"), eq("IMAGE"), any(Instant.class)))
                .thenReturn(new PostMediaUploadIntent(
                        "https://minio.local/presigned",
                        "posts/" + userId + "/file.png",
                        "https://cdn.2hands.vn/social/posts/" + userId + "/file.png",
                        "IMAGE",
                        expiresAt
                ));

        UploadPostMediaResult result = useCase.execute(
                new UploadPostMediaCommand(userId, "image/png", 1_048_576L, "IMAGE")
        );

        assertThat(result.uploadUrl()).contains("presigned");
        assertThat(result.objectKey()).startsWith("posts/" + userId);
        assertThat(result.mediaUrl()).contains("/social/posts/" + userId);
        assertThat(result.mediaKind()).isEqualTo("IMAGE");
        assertThat(result.maxFileSizeBytes()).isEqualTo(10_485_760L);
        verify(rateLimitService).validateUploadUrlRequest(userId);
    }

    @Test
    void shouldRejectSuspendedUser() {
        UUID userId = UUID.randomUUID();
        when(userProjectionRepository.findByUserId(userId)).thenReturn(java.util.Optional.of(
                new com.twohands.social_service.domain.user.UserProjection(
                        userId.toString(), "SUSPENDED", "User", null, null, false)
        ));

        assertThatThrownBy(() -> useCase.execute(
                new UploadPostMediaCommand(userId, "image/jpeg", 1024L, "IMAGE")
        ))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode()).isEqualTo(ErrorCode.ACCOUNT_SUSPENDED));
    }

    @Test
    void shouldRejectInvalidContentType() {
        UUID userId = UUID.randomUUID();
        when(userProjectionRepository.findByUserId(userId)).thenReturn(UserProjectionTestFixtures.activeOptional(userId));

        assertThatThrownBy(() -> useCase.execute(
                new UploadPostMediaCommand(userId, "application/pdf", 1024L, "IMAGE")
        ))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> {
                    AppException appEx = (AppException) ex;
                    assertThat(appEx.getErrorCode()).isEqualTo(ErrorCode.VALIDATION_ERROR);
                    assertThat(appEx.getField()).isEqualTo("content_type");
                });
    }

    @Test
    void shouldRejectOversizedImage() {
        UUID userId = UUID.randomUUID();
        when(userProjectionRepository.findByUserId(userId)).thenReturn(UserProjectionTestFixtures.activeOptional(userId));

        assertThatThrownBy(() -> useCase.execute(
                new UploadPostMediaCommand(userId, "image/jpeg", 20_000_000L, "IMAGE")
        ))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getField()).isEqualTo("file_size_bytes"));
    }

    @Test
    void shouldRejectWhenObjectStorageDisabled() {
        properties.setEnabled(false);
        UUID userId = UUID.randomUUID();
        when(userProjectionRepository.findByUserId(userId)).thenReturn(UserProjectionTestFixtures.activeOptional(userId));

        assertThatThrownBy(() -> useCase.execute(
                new UploadPostMediaCommand(userId, "image/jpeg", 1024L, "IMAGE")
        ))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.OBJECT_STORAGE_UNAVAILABLE));
    }

    @Test
    void shouldPropagateRateLimitError() {
        UUID userId = UUID.randomUUID();
        when(userProjectionRepository.findByUserId(userId)).thenReturn(UserProjectionTestFixtures.activeOptional(userId));
        doThrow(new AppException(ErrorCode.POST_MEDIA_UPLOAD_RATE_LIMITED,
                ErrorCode.POST_MEDIA_UPLOAD_RATE_LIMITED.defaultMessage()))
                .when(rateLimitService).validateUploadUrlRequest(userId);

        assertThatThrownBy(() -> useCase.execute(
                new UploadPostMediaCommand(userId, "image/jpeg", 1024L, "IMAGE")
        ))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.POST_MEDIA_UPLOAD_RATE_LIMITED));
    }
}
