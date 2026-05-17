package com.twohands.auth_service.application.useraccount.updatesettings;

import com.twohands.auth_service.application.useraccount.common.UserAccountAuthContextService;
import com.twohands.auth_service.domain.user.AppearanceMode;
import com.twohands.auth_service.domain.user.User;
import com.twohands.auth_service.domain.user.UserRepository;
import com.twohands.auth_service.domain.user.UserSettings;
import com.twohands.auth_service.domain.user.UserSettingsRepository;
import com.twohands.auth_service.exception.AppException;
import com.twohands.auth_service.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class UpdateUserSettingsUseCase {

    private static final String SUCCESS_MESSAGE = "Cap nhat cai dat thanh cong.";

    private final UpdateUserSettingsValidationService validationService;
    private final UserRepository userRepository;
    private final UserSettingsRepository userSettingsRepository;
    private final UserAccountAuthContextService authContextService;

    public UpdateUserSettingsUseCase(
            UpdateUserSettingsValidationService validationService,
            UserRepository userRepository,
            UserSettingsRepository userSettingsRepository,
            UserAccountAuthContextService authContextService
    ) {
        this.validationService = validationService;
        this.userRepository = userRepository;
        this.userSettingsRepository = userSettingsRepository;
        this.authContextService = authContextService;
    }

    @Transactional
    public UpdateUserSettingsResult execute(UpdateUserSettingsCommand command) {
        UUID userId = authContextService.requireUserId(command.userId());
        AppearanceMode mode = validationService.validateAndParseAppearanceMode(command.appearanceMode());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHORIZED, ErrorCode.UNAUTHORIZED.defaultMessage()));
        authContextService.ensureUserActive(user.status());

        UserSettings settings = userSettingsRepository.findByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "User settings not found"));

        settings.updateAppearanceMode(mode, Instant.now());
        userSettingsRepository.updateByUserId(settings);

        return new UpdateUserSettingsResult(settings.appearanceMode().name());
    }

    public String successMessage() {
        return SUCCESS_MESSAGE;
    }
}
