package com.twohands.auth_service.application.useraccount.updateavatar;

import com.twohands.auth_service.application.useraccount.common.UserAccountAuthContextService;
import com.twohands.auth_service.application.useraccount.common.UserAccountOutboxService;
import com.twohands.auth_service.application.useraccount.common.UserProjectionSyncPayload;
import com.twohands.auth_service.domain.outbox.OutboxEventRepository;
import com.twohands.auth_service.domain.user.User;
import com.twohands.auth_service.domain.user.UserProfile;
import com.twohands.auth_service.domain.user.UserProfileRepository;
import com.twohands.auth_service.domain.user.UserRepository;
import com.twohands.auth_service.exception.AppException;
import com.twohands.auth_service.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class UpdateAvatarUseCase {

    private static final String SUCCESS_MESSAGE = "Cap nhat avatar thanh cong.";

    private final UpdateAvatarValidationService validationService;
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final UserAccountOutboxService outboxService;
    private final UserAccountAuthContextService authContextService;

    public UpdateAvatarUseCase(
            UpdateAvatarValidationService validationService,
            UserRepository userRepository,
            UserProfileRepository userProfileRepository,
            OutboxEventRepository outboxEventRepository,
            UserAccountOutboxService outboxService,
            UserAccountAuthContextService authContextService
    ) {
        this.validationService = validationService;
        this.userRepository = userRepository;
        this.userProfileRepository = userProfileRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.outboxService = outboxService;
        this.authContextService = authContextService;
    }

    @Transactional
    public void execute(UpdateAvatarCommand command) {
        UUID userId = authContextService.requireUserId(command.userId());
        validationService.validate(command);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHORIZED, ErrorCode.UNAUTHORIZED.defaultMessage()));
        authContextService.ensureUserActive(user.status());

        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "User profile not found"));

        Instant now = Instant.now();
        profile.updateAvatar(command.avatarUrl(), now);
        userProfileRepository.updateByUserId(profile);
        outboxEventRepository.save(outboxService.userUpdated(
                user.id(),
                user.email().normalizedValue(),
                now,
                UserProjectionSyncPayload.profileOnly(null, profile.avatarUrl(), null)
        ));
    }

    public String successMessage() {
        return SUCCESS_MESSAGE;
    }
}
