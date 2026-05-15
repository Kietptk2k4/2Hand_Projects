package com.twohands.auth_service.application.auth.logout;

import com.twohands.auth_service.domain.session.RefreshTokenSession;
import com.twohands.auth_service.domain.session.RefreshTokenSessionRepository;
import com.twohands.auth_service.security.token.TokenHashingService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class LogoutUseCase {

    private static final String LOGOUT_SUCCESS_MESSAGE = "Dang xuat thanh cong.";

    private final LogoutValidationService validationService;
    private final LogoutRateLimitService logoutRateLimitService;
    private final RefreshTokenSessionRepository refreshTokenSessionRepository;
    private final TokenHashingService tokenHashingService;

    public LogoutUseCase(
            LogoutValidationService validationService,
            LogoutRateLimitService logoutRateLimitService,
            RefreshTokenSessionRepository refreshTokenSessionRepository,
            TokenHashingService tokenHashingService
    ) {
        this.validationService = validationService;
        this.logoutRateLimitService = logoutRateLimitService;
        this.refreshTokenSessionRepository = refreshTokenSessionRepository;
        this.tokenHashingService = tokenHashingService;
    }

    @Transactional
    public void execute(LogoutCommand command) {
        validationService.validateRefreshToken(command.refreshToken());
        logoutRateLimitService.validateLogoutAttempt(command.ipAddress());

        String tokenHash = tokenHashingService.sha256(command.refreshToken());
        RefreshTokenSession session = refreshTokenSessionRepository.findByTokenHash(tokenHash).orElse(null);
        if (session == null) {
            return;
        }

        refreshTokenSessionRepository.markLoggedOutIfActive(session.id(), Instant.now());
    }

    public String logoutSuccessMessage() {
        return LOGOUT_SUCCESS_MESSAGE;
    }
}
