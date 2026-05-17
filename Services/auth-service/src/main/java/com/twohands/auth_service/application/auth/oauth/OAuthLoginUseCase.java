package com.twohands.auth_service.application.auth.oauth;

import com.twohands.auth_service.application.auth.common.UserCreatedOutboxService;
import com.twohands.auth_service.domain.oauth.OAuthAccount;
import com.twohands.auth_service.domain.oauth.OAuthAccountRepository;
import com.twohands.auth_service.domain.outbox.OutboxEventRepository;
import com.twohands.auth_service.domain.session.RefreshTokenSession;
import com.twohands.auth_service.domain.session.RefreshTokenSessionRepository;
import com.twohands.auth_service.domain.user.EmailAddress;
import com.twohands.auth_service.domain.user.LoginLog;
import com.twohands.auth_service.domain.user.LoginLogRepository;
import com.twohands.auth_service.domain.user.User;
import com.twohands.auth_service.domain.user.UserProfile;
import com.twohands.auth_service.domain.user.UserProfileRepository;
import com.twohands.auth_service.domain.user.UserRepository;
import com.twohands.auth_service.domain.user.UserSettings;
import com.twohands.auth_service.domain.user.UserSettingsRepository;
import com.twohands.auth_service.domain.user.UserStatus;
import com.twohands.auth_service.exception.AppException;
import com.twohands.auth_service.exception.ErrorCode;
import com.twohands.auth_service.security.jwt.JwtTokenIssuer;
import com.twohands.auth_service.security.token.TokenHashingService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class OAuthLoginUseCase {

    private static final String OAUTH_EMAIL_REQUIRED_MESSAGE = "Vui long cap quyen Email de su dung tinh nang nay.";
    private static final String OAUTH_ACCOUNT_UNAVAILABLE_MESSAGE = "Tai khoan hien khong kha dung.";

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final UserSettingsRepository userSettingsRepository;
    private final OAuthAccountRepository oAuthAccountRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final LoginLogRepository loginLogRepository;
    private final RefreshTokenSessionRepository refreshTokenSessionRepository;
    private final JwtTokenIssuer jwtTokenIssuer;
    private final TokenHashingService tokenHashingService;
    private final UserCreatedOutboxService userCreatedOutboxService;

    public OAuthLoginUseCase(
            UserRepository userRepository,
            UserProfileRepository userProfileRepository,
            UserSettingsRepository userSettingsRepository,
            OAuthAccountRepository oAuthAccountRepository,
            OutboxEventRepository outboxEventRepository,
            LoginLogRepository loginLogRepository,
            RefreshTokenSessionRepository refreshTokenSessionRepository,
            JwtTokenIssuer jwtTokenIssuer,
            TokenHashingService tokenHashingService,
            UserCreatedOutboxService userCreatedOutboxService
    ) {
        this.userRepository = userRepository;
        this.userProfileRepository = userProfileRepository;
        this.userSettingsRepository = userSettingsRepository;
        this.oAuthAccountRepository = oAuthAccountRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.loginLogRepository = loginLogRepository;
        this.refreshTokenSessionRepository = refreshTokenSessionRepository;
        this.jwtTokenIssuer = jwtTokenIssuer;
        this.tokenHashingService = tokenHashingService;
        this.userCreatedOutboxService = userCreatedOutboxService;
    }

    @Transactional
    public OAuthLoginResult execute(OAuthLoginCommand command) {
        OAuthProfile profile = command.profile();
        if (profile == null || profile.email() == null || profile.email().isBlank()) {
            throw new AppException(ErrorCode.OAUTH_EMAIL_MISSING, OAUTH_EMAIL_REQUIRED_MESSAGE);
        }

        Instant now = Instant.now();
        String normalizedEmail = EmailAddress.of(profile.email()).normalizedValue();
        User user = userRepository.findByEmailNormalized(normalizedEmail).orElse(null);
        boolean firstLogin = false;

        if (user == null) {
            UUID newUserId = UUID.randomUUID();
            User newUser = User.registerWithOAuth(newUserId, EmailAddress.of(normalizedEmail), now);
            userRepository.save(newUser);
            userProfileRepository.save(UserProfile.rehydrate(
                    newUserId,
                    resolveDisplayName(profile.name(), normalizedEmail),
                    profile.avatarUrl(),
                    null,
                    null,
                    java.util.Map.of(),
                    false,
                    now,
                    now
            ));
            userSettingsRepository.save(UserSettings.createDefault(newUserId, now));
            outboxEventRepository.save(
                    userCreatedOutboxService.build(
                            newUser.id(),
                            newUser.email().normalizedValue(),
                            newUser.status().name(),
                            now
                    )
            );
            user = newUser;
            firstLogin = true;
        }

        if (user.status() == UserStatus.SUSPENDED || user.status() == UserStatus.DELETED) {
            throw new AppException(ErrorCode.OAUTH_ACCOUNT_UNAVAILABLE, OAUTH_ACCOUNT_UNAVAILABLE_MESSAGE);
        }

        ensureOAuthAccountLinked(user.id(), profile, normalizedEmail, now);

        JwtTokenIssuer.TokenPair tokenPair = jwtTokenIssuer.issue(
                user.id(),
                user.email().normalizedValue(),
                user.status().name(),
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
        loginLogRepository.save(new LoginLog(
                UUID.randomUUID(),
                user.id(),
                profile.provider().loginMethod(),
                command.ipAddress(),
                command.userAgent(),
                true,
                now
        ));

        return new OAuthLoginResult(
                tokenPair.accessToken(),
                tokenPair.refreshToken(),
                tokenPair.accessExpiresInSeconds(),
                user.id(),
                user.email().normalizedValue(),
                user.status().name(),
                firstLogin
        );
    }

    private void ensureOAuthAccountLinked(UUID userId, OAuthProfile profile, String normalizedEmail, Instant now) {
        if (profile.providerUserId() == null || profile.providerUserId().isBlank()) {
            throw new AppException(ErrorCode.OAUTH_PROVIDER_PROFILE_INVALID, "Thong tin tai khoan OAuth khong hop le.");
        }
        OAuthAccount existingByProviderUser = oAuthAccountRepository
                .findByProviderAndProviderUserId(profile.provider(), profile.providerUserId())
                .orElse(null);
        if (existingByProviderUser != null) {
            if (!existingByProviderUser.userId().equals(userId)) {
                throw new AppException(ErrorCode.OAUTH_PROVIDER_PROFILE_INVALID, "Xac thuc OAuth that bai.");
            }
            return;
        }
        if (oAuthAccountRepository.findByUserIdAndProvider(userId, profile.provider()).isPresent()) {
            return;
        }
        oAuthAccountRepository.save(OAuthAccount.create(
                userId,
                profile.provider(),
                profile.providerUserId(),
                normalizedEmail,
                now
        ));
    }

    private String resolveDisplayName(String profileName, String normalizedEmail) {
        if (profileName != null && !profileName.isBlank()) {
            return profileName.trim();
        }
        String localPart = normalizedEmail.split("@")[0];
        if (localPart.isBlank()) {
            return "user_" + System.currentTimeMillis();
        }
        return localPart.length() > 100 ? localPart.substring(0, 100) : localPart;
    }
}
