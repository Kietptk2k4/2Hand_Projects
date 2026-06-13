package com.twohands.auth_service.application.useraccount.coverupload;

import com.twohands.auth_service.application.useraccount.avatarupload.AvatarUploadIntent;
import com.twohands.auth_service.application.useraccount.avatarupload.AvatarUploadRateLimitService;
import com.twohands.auth_service.application.useraccount.avatarupload.AvatarUploadStoragePort;
import com.twohands.auth_service.application.useraccount.avatarupload.CreateAvatarUploadUrlValidationService;
import com.twohands.auth_service.application.useraccount.common.UserAccountAuthContextService;
import com.twohands.auth_service.config.AuthObjectStorageProperties;
import com.twohands.auth_service.domain.user.UserRepository;
import com.twohands.auth_service.exception.AppException;
import com.twohands.auth_service.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class CreateCoverUploadUrlUseCase {

    private static final String SUCCESS_MESSAGE = "Tao link upload anh bia thanh cong.";

    private final CreateAvatarUploadUrlValidationService validationService;
    private final UserRepository userRepository;
    private final UserAccountAuthContextService authContextService;
    private final AvatarUploadRateLimitService rateLimitService;
    private final AuthObjectStorageProperties objectStorageProperties;
    private final Optional<AvatarUploadStoragePort> avatarUploadStoragePort;

    public CreateCoverUploadUrlUseCase(
            CreateAvatarUploadUrlValidationService validationService,
            UserRepository userRepository,
            UserAccountAuthContextService authContextService,
            AvatarUploadRateLimitService rateLimitService,
            AuthObjectStorageProperties objectStorageProperties,
            @Autowired(required = false) AvatarUploadStoragePort avatarUploadStoragePort
    ) {
        this.validationService = validationService;
        this.userRepository = userRepository;
        this.authContextService = authContextService;
        this.rateLimitService = rateLimitService;
        this.objectStorageProperties = objectStorageProperties;
        this.avatarUploadStoragePort = Optional.ofNullable(avatarUploadStoragePort);
    }

    public CreateCoverUploadUrlResult execute(CreateCoverUploadUrlCommand command) {
        UUID userId = authContextService.requireUserId(command.userId());
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHORIZED, ErrorCode.UNAUTHORIZED.defaultMessage()));
        authContextService.ensureUserActive(user.status());

        String contentType = validationService.validateContentType(command.contentType());
        validationService.validateFileSize(command.fileSizeBytes());
        rateLimitService.validateUploadUrlRequest(userId);

        if (!objectStorageProperties.isEnabled()) {
            throw new AppException(
                    ErrorCode.OBJECT_STORAGE_UNAVAILABLE,
                    ErrorCode.OBJECT_STORAGE_UNAVAILABLE.defaultMessage()
            );
        }

        AvatarUploadStoragePort storagePort = avatarUploadStoragePort.orElseThrow(() -> new AppException(
                ErrorCode.OBJECT_STORAGE_UNAVAILABLE,
                ErrorCode.OBJECT_STORAGE_UNAVAILABLE.defaultMessage()
        ));

        Instant expiresAt = Instant.now().plusSeconds(objectStorageProperties.getPresignedUrlTtlSeconds());
        AvatarUploadIntent intent = storagePort.createCoverUploadIntent(userId, contentType, expiresAt);

        return new CreateCoverUploadUrlResult(
                intent.uploadUrl(),
                intent.objectKey(),
                intent.avatarUrl(),
                intent.expiresAt(),
                objectStorageProperties.getAvatarMaxFileSizeBytes(),
                List.copyOf(objectStorageProperties.getAllowedAvatarContentTypes())
        );
    }

    public String successMessage() {
        return SUCCESS_MESSAGE;
    }
}
