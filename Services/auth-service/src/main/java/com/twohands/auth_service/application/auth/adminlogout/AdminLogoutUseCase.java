package com.twohands.auth_service.application.auth.adminlogout;

import com.twohands.auth_service.application.auth.logout.LogoutRateLimitService;
import com.twohands.auth_service.application.auth.logout.LogoutValidationService;
import com.twohands.auth_service.domain.session.RefreshTokenSession;
import com.twohands.auth_service.domain.session.RefreshTokenSessionRepository;
import com.twohands.auth_service.exception.AppException;
import com.twohands.auth_service.exception.ErrorCode;
import com.twohands.auth_service.security.token.TokenHashingService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class AdminLogoutUseCase {

    private static final String LOGOUT_SUCCESS_MESSAGE = "Dang xuat admin thanh cong.";

    private final LogoutValidationService validationService;
    private final LogoutRateLimitService logoutRateLimitService;
    private final RefreshTokenSessionRepository refreshTokenSessionRepository;
    private final TokenHashingService tokenHashingService;

    public AdminLogoutUseCase(
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
    public void execute(AdminLogoutCommand command) {
        UUID authenticatedUserId = requireAuthenticatedUserId(command.authenticatedUserId());
        validationService.validateRefreshToken(command.refreshToken());
        logoutRateLimitService.validateLogoutAttempt(command.ipAddress());

        String tokenHash = tokenHashingService.sha256(command.refreshToken());
        RefreshTokenSession session = refreshTokenSessionRepository.findByTokenHash(tokenHash).orElse(null);
        if (session == null) {
            return;
        }
        if (!session.userId().equals(authenticatedUserId)) {
            throw new AppException(ErrorCode.FORBIDDEN, ErrorCode.FORBIDDEN.defaultMessage());
        }

        refreshTokenSessionRepository.markLoggedOutIfActive(session.id(), Instant.now());
    }

    public String logoutSuccessMessage() {
        return LOGOUT_SUCCESS_MESSAGE;
    }

    private UUID requireAuthenticatedUserId(UUID authenticatedUserId) {
        if (authenticatedUserId == null) {
            throw new AppException(ErrorCode.UNAUTHORIZED, ErrorCode.UNAUTHORIZED.defaultMessage());
        }
        return authenticatedUserId;
    }
}
