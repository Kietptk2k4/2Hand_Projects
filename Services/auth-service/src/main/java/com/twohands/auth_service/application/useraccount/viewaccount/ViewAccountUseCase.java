package com.twohands.auth_service.application.useraccount.viewaccount;

import com.twohands.auth_service.application.useraccount.common.UserAccountAuthContextService;
import com.twohands.auth_service.domain.user.User;
import com.twohands.auth_service.domain.user.UserProfile;
import com.twohands.auth_service.domain.user.UserProfileRepository;
import com.twohands.auth_service.domain.user.UserRepository;
import com.twohands.auth_service.domain.user.UserSettings;
import com.twohands.auth_service.domain.user.UserSettingsRepository;
import com.twohands.auth_service.exception.AppException;
import com.twohands.auth_service.exception.ErrorCode;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ViewAccountUseCase {

    private static final String SUCCESS_MESSAGE = "Lay thong tin tai khoan thanh cong.";

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final UserSettingsRepository userSettingsRepository;
    private final UserAccountAuthContextService authContextService;

    public ViewAccountUseCase(
            UserRepository userRepository,
            UserProfileRepository userProfileRepository,
            UserSettingsRepository userSettingsRepository,
            UserAccountAuthContextService authContextService
    ) {
        this.userRepository = userRepository;
        this.userProfileRepository = userProfileRepository;
        this.userSettingsRepository = userSettingsRepository;
        this.authContextService = authContextService;
    }

    public ViewAccountResult execute(UUID actorUserId) {
        UUID userId = authContextService.requireUserId(actorUserId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHORIZED, ErrorCode.UNAUTHORIZED.defaultMessage()));
        authContextService.ensureUserActive(user.status());

        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "User profile not found"));
        UserSettings settings = userSettingsRepository.findByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "User settings not found"));

        return new ViewAccountResult(
                new ViewAccountResult.UserData(
                        user.id(),
                        user.email().normalizedValue(),
                        user.status().name(),
                        user.emailVerified(),
                        null,
                        user.lastLoginAt()
                ),
                new ViewAccountResult.ProfileData(
                        profile.displayName(),
                        profile.avatarUrl(),
                        profile.coverUrl(),
                        profile.bio(),
                        profile.website(),
                        profile.socialLinks(),
                        profile.isPrivate()
                ),
                new ViewAccountResult.SettingsData(settings.appearanceMode().name())
        );
    }

    public String successMessage() {
        return SUCCESS_MESSAGE;
    }
}
