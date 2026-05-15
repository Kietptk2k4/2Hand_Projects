package com.twohands.auth_service.application.auth.refresh;

import com.twohands.auth_service.domain.session.RefreshTokenSession;
import com.twohands.auth_service.domain.session.RefreshTokenSessionRepository;
import com.twohands.auth_service.domain.session.SessionDomainError;
import com.twohands.auth_service.domain.user.User;
import com.twohands.auth_service.domain.user.UserRepository;
import com.twohands.auth_service.domain.user.UserStatus;
import com.twohands.auth_service.exception.AppException;
import com.twohands.auth_service.exception.ErrorCode;
import com.twohands.auth_service.security.jwt.JwtTokenIssuer;
import com.twohands.auth_service.security.token.TokenHashingService;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class RefreshAccessTokenUseCase {

    private static final String REFRESH_SUCCESS_MESSAGE = "Lam moi access token thanh cong.";
    private static final String INVALID_REFRESH_SESSION_MESSAGE =
            "Phien dang nhap khong hop le hoac da het han. Vui long dang nhap lai.";

    private final RefreshAccessTokenValidationService validationService;
    private final RefreshTokenSessionRepository refreshTokenSessionRepository;
    private final UserRepository userRepository;
    private final TokenHashingService tokenHashingService;
    private final JwtTokenIssuer jwtTokenIssuer;
    private final RefreshRateLimitService refreshRateLimitService;

    public RefreshAccessTokenUseCase(
            RefreshAccessTokenValidationService validationService,
            RefreshTokenSessionRepository refreshTokenSessionRepository,
            UserRepository userRepository,
            TokenHashingService tokenHashingService,
            JwtTokenIssuer jwtTokenIssuer,
            RefreshRateLimitService refreshRateLimitService
    ) {
        this.validationService = validationService;
        this.refreshTokenSessionRepository = refreshTokenSessionRepository;
        this.userRepository = userRepository;
        this.tokenHashingService = tokenHashingService;
        this.jwtTokenIssuer = jwtTokenIssuer;
        this.refreshRateLimitService = refreshRateLimitService;
    }

    public RefreshAccessTokenResult execute(RefreshAccessTokenCommand command) {
        validationService.validateRefreshToken(command.refreshToken());
        refreshRateLimitService.validateRefreshAttempt(command.ipAddress());

        String tokenHash = tokenHashingService.sha256(command.refreshToken());
        RefreshTokenSession session = refreshTokenSessionRepository.findByTokenHash(tokenHash)
                .orElseThrow(this::invalidRefreshSessionException);

        try {
            session.ensureUsableForRefresh(Instant.now());
        } catch (SessionDomainError ex) {
            throw invalidRefreshSessionException();
        }

        User user = userRepository.findById(session.userId())
                .orElseThrow(this::invalidRefreshSessionException);

        if (user.status() == UserStatus.SUSPENDED || user.status() == UserStatus.DELETED) {
            throw invalidRefreshSessionException();
        }

        JwtTokenIssuer.AccessTokenOnly access = jwtTokenIssuer.issueAccessToken(
                user.id(),
                user.email().normalizedValue(),
                user.status().name(),
                Instant.now()
        );

        return new RefreshAccessTokenResult(access.accessToken(), access.accessExpiresInSeconds());
    }

    public String refreshSuccessMessage() {
        return REFRESH_SUCCESS_MESSAGE;
    }

    private AppException invalidRefreshSessionException() {
        return new AppException(ErrorCode.INVALID_REFRESH_SESSION, INVALID_REFRESH_SESSION_MESSAGE);
    }
}
