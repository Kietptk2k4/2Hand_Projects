package com.twohands.auth_service.application.auth.adminrefresh;

import com.twohands.auth_service.application.auth.refresh.RefreshAccessTokenValidationService;
import com.twohands.auth_service.application.auth.refresh.RefreshRateLimitService;
import com.twohands.auth_service.domain.rbac.AdminPortalAccessPolicy;
import com.twohands.auth_service.domain.rbac.PermissionQueryRepository;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class AdminRefreshAccessTokenUseCase {

    private static final String REFRESH_SUCCESS_MESSAGE = "Lam moi access token admin thanh cong.";
    private static final String INVALID_REFRESH_SESSION_MESSAGE =
            "Phien dang nhap khong hop le hoac da het han. Vui long dang nhap lai.";

    private final RefreshAccessTokenValidationService validationService;
    private final RefreshTokenSessionRepository refreshTokenSessionRepository;
    private final UserRepository userRepository;
    private final PermissionQueryRepository permissionQueryRepository;
    private final TokenHashingService tokenHashingService;
    private final JwtTokenIssuer jwtTokenIssuer;
    private final RefreshRateLimitService refreshRateLimitService;

    public AdminRefreshAccessTokenUseCase(
            RefreshAccessTokenValidationService validationService,
            RefreshTokenSessionRepository refreshTokenSessionRepository,
            UserRepository userRepository,
            PermissionQueryRepository permissionQueryRepository,
            TokenHashingService tokenHashingService,
            JwtTokenIssuer jwtTokenIssuer,
            RefreshRateLimitService refreshRateLimitService
    ) {
        this.validationService = validationService;
        this.refreshTokenSessionRepository = refreshTokenSessionRepository;
        this.userRepository = userRepository;
        this.permissionQueryRepository = permissionQueryRepository;
        this.tokenHashingService = tokenHashingService;
        this.jwtTokenIssuer = jwtTokenIssuer;
        this.refreshRateLimitService = refreshRateLimitService;
    }

    public AdminRefreshAccessTokenResult execute(AdminRefreshAccessTokenCommand command) {
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

        if (user.status() == UserStatus.SUSPENDED) {
            throw new AppException(ErrorCode.ACCOUNT_SUSPENDED, ErrorCode.ACCOUNT_SUSPENDED.defaultMessage());
        }
        if (user.status() == UserStatus.DELETED) {
            throw invalidRefreshSessionException();
        }

        List<String> roleCodes = permissionQueryRepository.findRoleCodesByUserId(user.id());
        Set<String> permissionCodes = permissionQueryRepository.findPermissionCodesByUserId(user.id());
        if (!AdminPortalAccessPolicy.canAccessAdminPortal(roleCodes, permissionCodes)) {
            throw new AppException(
                    ErrorCode.ADMIN_PORTAL_ACCESS_DENIED,
                    ErrorCode.ADMIN_PORTAL_ACCESS_DENIED.defaultMessage()
            );
        }

        List<String> permissions = new ArrayList<>(permissionCodes);
        permissions.sort(String::compareTo);

        Instant now = Instant.now();
        JwtTokenIssuer.AccessTokenOnly access = jwtTokenIssuer.issueAdminAccessOnly(
                user.id(),
                user.email().normalizedValue(),
                user.status().name(),
                roleCodes,
                permissions,
                now
        );

        return new AdminRefreshAccessTokenResult(
                access.accessToken(),
                access.accessExpiresInSeconds(),
                user.id(),
                user.email().normalizedValue(),
                user.status().name(),
                roleCodes,
                permissions
        );
    }

    public String refreshSuccessMessage() {
        return REFRESH_SUCCESS_MESSAGE;
    }

    private AppException invalidRefreshSessionException() {
        return new AppException(ErrorCode.INVALID_REFRESH_SESSION, INVALID_REFRESH_SESSION_MESSAGE);
    }
}
