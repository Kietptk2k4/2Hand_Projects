package com.twohands.auth_service.application.auth.adminlogin;

import com.twohands.auth_service.application.auth.login.LoginRateLimitService;
import com.twohands.auth_service.application.auth.login.LoginValidationService;
import com.twohands.auth_service.application.auth.register.PasswordHashingService;
import com.twohands.auth_service.domain.rbac.AdminPortalAccessPolicy;
import com.twohands.auth_service.domain.rbac.PermissionQueryRepository;
import com.twohands.auth_service.domain.session.RefreshTokenSession;
import com.twohands.auth_service.domain.session.RefreshTokenSessionRepository;
import com.twohands.auth_service.domain.user.LoginLog;
import com.twohands.auth_service.domain.user.LoginLogRepository;
import com.twohands.auth_service.domain.user.LoginMethod;
import com.twohands.auth_service.domain.user.User;
import com.twohands.auth_service.domain.user.UserRepository;
import com.twohands.auth_service.domain.user.UserStatus;
import com.twohands.auth_service.exception.AppException;
import com.twohands.auth_service.exception.ErrorCode;
import com.twohands.auth_service.security.jwt.JwtTokenIssuer;
import com.twohands.auth_service.security.token.TokenHashingService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class AdminLoginUseCase {

    private static final String GENERIC_INVALID_CREDENTIAL_MESSAGE = "Email hoac mat khau khong chinh xac.";
    private static final String LOGIN_SUCCESS_MESSAGE = "Dang nhap admin thanh cong.";

    private final UserRepository userRepository;
    private final LoginLogRepository loginLogRepository;
    private final RefreshTokenSessionRepository refreshTokenSessionRepository;
    private final PermissionQueryRepository permissionQueryRepository;
    private final PasswordHashingService passwordHashingService;
    private final LoginValidationService loginValidationService;
    private final JwtTokenIssuer jwtTokenIssuer;
    private final TokenHashingService tokenHashingService;
    private final LoginRateLimitService loginRateLimitService;

    public AdminLoginUseCase(
            UserRepository userRepository,
            LoginLogRepository loginLogRepository,
            RefreshTokenSessionRepository refreshTokenSessionRepository,
            PermissionQueryRepository permissionQueryRepository,
            PasswordHashingService passwordHashingService,
            LoginValidationService loginValidationService,
            JwtTokenIssuer jwtTokenIssuer,
            TokenHashingService tokenHashingService,
            LoginRateLimitService loginRateLimitService
    ) {
        this.userRepository = userRepository;
        this.loginLogRepository = loginLogRepository;
        this.refreshTokenSessionRepository = refreshTokenSessionRepository;
        this.permissionQueryRepository = permissionQueryRepository;
        this.passwordHashingService = passwordHashingService;
        this.loginValidationService = loginValidationService;
        this.jwtTokenIssuer = jwtTokenIssuer;
        this.tokenHashingService = tokenHashingService;
        this.loginRateLimitService = loginRateLimitService;
    }

    @Transactional(noRollbackFor = AppException.class)
    public AdminLoginResult execute(AdminLoginCommand command) {
        String normalizedEmail = loginValidationService.normalizeAndValidateEmail(command.email());
        loginValidationService.validatePassword(command.password());
        loginRateLimitService.validateLoginAttempt(normalizedEmail, command.ipAddress());

        User user = userRepository.findByEmailNormalized(normalizedEmail).orElse(null);
        if (user == null) {
            loginRateLimitService.recordFailedAttempt(normalizedEmail, command.ipAddress());
            throw invalidCredentialException();
        }

        if (user.status() == UserStatus.DELETED) {
            loginRateLimitService.recordFailedAttempt(normalizedEmail, command.ipAddress());
            throw invalidCredentialException();
        }

        if (!passwordHashingService.matches(command.password(), user.passwordHash())) {
            loginLogRepository.save(buildLoginLog(user.id(), command.ipAddress(), command.userAgent(), false, Instant.now()));
            loginRateLimitService.recordFailedAttempt(normalizedEmail, command.ipAddress());
            throw invalidCredentialException();
        }

        if (user.status() == UserStatus.SUSPENDED) {
            throw new AppException(ErrorCode.ACCOUNT_SUSPENDED, ErrorCode.ACCOUNT_SUSPENDED.defaultMessage());
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
        JwtTokenIssuer.TokenPair tokenPair = jwtTokenIssuer.issueAdminAccess(
                user.id(),
                user.email().normalizedValue(),
                user.status().name(),
                roleCodes,
                permissions,
                now
        );

        RefreshTokenSession session = RefreshTokenSession.createActive(
                UUID.randomUUID(),
                user.id(),
                tokenHashingService.sha256(tokenPair.refreshToken()),
                command.deviceId(),
                command.ipAddress(),
                command.userAgent(),
                tokenPair.refreshExpiresAt(),
                now
        );

        refreshTokenSessionRepository.save(session);
        userRepository.updateLastLoginAt(user.id(), now);
        loginLogRepository.save(buildLoginLog(user.id(), command.ipAddress(), command.userAgent(), true, now));

        return new AdminLoginResult(
                tokenPair.accessToken(),
                tokenPair.refreshToken(),
                tokenPair.accessExpiresInSeconds(),
                user.id(),
                user.email().normalizedValue(),
                user.status().name(),
                roleCodes,
                permissions
        );
    }

    public String loginSuccessMessage() {
        return LOGIN_SUCCESS_MESSAGE;
    }

    private LoginLog buildLoginLog(UUID userId, String ipAddress, String userAgent, boolean success, Instant now) {
        return new LoginLog(
                UUID.randomUUID(),
                userId,
                LoginMethod.EMAIL,
                ipAddress,
                userAgent,
                success,
                now
        );
    }

    private AppException invalidCredentialException() {
        return new AppException(ErrorCode.INVALID_LOGIN_CREDENTIALS, GENERIC_INVALID_CREDENTIAL_MESSAGE);
    }
}
