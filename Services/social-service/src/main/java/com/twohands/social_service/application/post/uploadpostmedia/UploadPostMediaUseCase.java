package com.twohands.social_service.application.post.uploadpostmedia;

import com.twohands.social_service.application.user.common.UserWriteGuard;
import com.twohands.social_service.config.SocialObjectStorageProperties;
import com.twohands.social_service.exception.AppException;
import com.twohands.social_service.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class UploadPostMediaUseCase {

    private static final String SUCCESS_MESSAGE = "Tao link upload media thanh cong.";

    private final UserWriteGuard userWriteGuard;
    private final UploadPostMediaValidationService validationService;
    private final PostMediaUploadRateLimitService rateLimitService;
    private final SocialObjectStorageProperties objectStorageProperties;
    private final Optional<PostMediaUploadStoragePort> postMediaUploadStoragePort;

    public UploadPostMediaUseCase(
            UserWriteGuard userWriteGuard,
            UploadPostMediaValidationService validationService,
            PostMediaUploadRateLimitService rateLimitService,
            SocialObjectStorageProperties objectStorageProperties,
            @Autowired(required = false) PostMediaUploadStoragePort postMediaUploadStoragePort
    ) {
        this.userWriteGuard = userWriteGuard;
        this.validationService = validationService;
        this.rateLimitService = rateLimitService;
        this.objectStorageProperties = objectStorageProperties;
        this.postMediaUploadStoragePort = Optional.ofNullable(postMediaUploadStoragePort);
    }

    public UploadPostMediaResult execute(UploadPostMediaCommand command) {
        userWriteGuard.assertCanWrite(command.userId());

        String mediaKind = validationService.validateMediaKind(command.mediaKind());
        String contentType = validationService.validateContentType(command.contentType(), mediaKind);
        validationService.validateFileSize(command.fileSizeBytes(), mediaKind);
        String clientUploadOrigin = validationService.validateClientUploadOrigin(command.clientUploadOrigin());
        rateLimitService.validateUploadUrlRequest(command.userId());

        if (!objectStorageProperties.isEnabled()) {
            throw new AppException(
                    ErrorCode.OBJECT_STORAGE_UNAVAILABLE,
                    ErrorCode.OBJECT_STORAGE_UNAVAILABLE.defaultMessage()
            );
        }

        PostMediaUploadStoragePort storagePort = postMediaUploadStoragePort.orElseThrow(() -> new AppException(
                ErrorCode.OBJECT_STORAGE_UNAVAILABLE,
                ErrorCode.OBJECT_STORAGE_UNAVAILABLE.defaultMessage()
        ));

        Instant expiresAt = Instant.now().plusSeconds(objectStorageProperties.getPresignedUrlTtlSeconds());
        PostMediaUploadIntent intent = storagePort.createUploadIntent(
                command.userId(),
                contentType,
                mediaKind,
                expiresAt,
                clientUploadOrigin
        );

        return new UploadPostMediaResult(
                intent.uploadUrl(),
                intent.objectKey(),
                intent.mediaUrl(),
                intent.mediaKind(),
                intent.expiresAt(),
                validationService.maxSizeFor(mediaKind),
                List.copyOf(objectStorageProperties.allAllowedContentTypes())
        );
    }

    public String successMessage() {
        return SUCCESS_MESSAGE;
    }
}
