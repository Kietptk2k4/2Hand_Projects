package com.twohands.auth_service.application.useraccount.logoutallsesssion;

import com.twohands.auth_service.application.useraccount.common.UserAccountAuthContextService;
import com.twohands.auth_service.domain.session.RefreshTokenSessionRepository;
import com.twohands.auth_service.domain.user.User;
import com.twohands.auth_service.domain.user.UserRepository;
import com.twohands.auth_service.exception.AppException;
import com.twohands.auth_service.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class LogoutAllSesssionUseCase {

    private static final String SUCCESS_MESSAGE = "Dang xuat tat ca phien dang nhap thanh cong.";

    private final UserRepository userRepository;
    private final RefreshTokenSessionRepository refreshTokenSessionRepository;
    private final UserAccountAuthContextService authContextService;

    public LogoutAllSesssionUseCase(
            UserRepository userRepository,
            RefreshTokenSessionRepository refreshTokenSessionRepository,
            UserAccountAuthContextService authContextService
    ) {
        this.userRepository = userRepository;
        this.refreshTokenSessionRepository = refreshTokenSessionRepository;
        this.authContextService = authContextService;
    }

    @Transactional
    public void execute(UUID actorUserId) {
        UUID userId = authContextService.requireUserId(actorUserId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHORIZED, ErrorCode.UNAUTHORIZED.defaultMessage()));
        authContextService.ensureUserActive(user.status());

        refreshTokenSessionRepository.revokeAllByUserId(userId);
    }

    public String successMessage() {
        return SUCCESS_MESSAGE;
    }
}
