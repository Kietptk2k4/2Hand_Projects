package com.twohands.auth_service.application.auth.verifyemail;

import com.twohands.auth_service.application.auth.register.PasswordHashingService;
import com.twohands.auth_service.application.useraccount.common.UserAccountOutboxService;
import com.twohands.auth_service.domain.outbox.OutboxEventRepository;
import com.twohands.auth_service.domain.user.PasswordHash;
import com.twohands.auth_service.domain.user.User;
import com.twohands.auth_service.domain.user.UserDomainError;
import com.twohands.auth_service.domain.user.UserRepository;
import com.twohands.auth_service.domain.user.UserStatus;
import com.twohands.auth_service.domain.user.VerificationToken;
import com.twohands.auth_service.domain.user.VerificationTokenRepository;
import com.twohands.auth_service.domain.user.VerificationTokenType;
import com.twohands.auth_service.exception.AppException;
import com.twohands.auth_service.exception.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class VerifyEmailUseCase {

    private static final Logger log = LoggerFactory.getLogger(VerifyEmailUseCase.class);
    private static final String SUCCESS_MESSAGE = "Xac thuc email thanh cong.";
    private static final String ALREADY_VERIFIED_MESSAGE = "Tai khoan da duoc xac thuc truoc do.";

    private final UserRepository userRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final VerifyEmailValidationService validationService;
    private final PasswordHashingService passwordHashingService;
    private final UserAccountOutboxService userAccountOutboxService;
    private final VerifyEmailRateLimitService rateLimitService;

    public VerifyEmailUseCase(
            UserRepository userRepository,
            VerificationTokenRepository verificationTokenRepository,
            OutboxEventRepository outboxEventRepository,
            VerifyEmailValidationService validationService,
            PasswordHashingService passwordHashingService,
            UserAccountOutboxService userAccountOutboxService,
            VerifyEmailRateLimitService rateLimitService
    ) {
        this.userRepository = userRepository;
        this.verificationTokenRepository = verificationTokenRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.validationService = validationService;
        this.passwordHashingService = passwordHashingService;
        this.userAccountOutboxService = userAccountOutboxService;
        this.rateLimitService = rateLimitService;
    }

    @Transactional
    public VerifyEmailResult execute(VerifyEmailCommand command) {
        rateLimitService.validateAttempt(command.ipAddress());
        String rawToken = validationService.validateAndNormalizeToken(command.token());
        Instant now = Instant.now();

        Optional<VerificationToken> matchedToken = resolveVerificationToken(rawToken, now);
        if (matchedToken.isEmpty()) {
            return tryIdempotentWithUsedToken(rawToken).orElseThrow(this::invalidTokenException);
        }

        VerificationToken token = matchedToken.get();
        User user = userRepository.findById(token.userId())
                .orElseThrow(this::invalidTokenException);

        if (user.status() == UserStatus.ACTIVE && user.emailVerified()) {
            return alreadyVerifiedResult(user);
        }

        if (user.status() != UserStatus.PENDING_VERIFICATION) {
            throw invalidTokenException();
        }

        try {
            token.markUsed(now);
            user.markEmailVerified(now);
        } catch (UserDomainError ex) {
            throw invalidTokenException();
        }

        int tokenRowsUpdated = verificationTokenRepository.markUsedById(token.id(), now);
        if (tokenRowsUpdated == 0) {
            throw invalidTokenException();
        }

        int userRowsUpdated = userRepository.markEmailVerifiedAndActive(user.id(), now);
        if (userRowsUpdated == 0) {
            User latestUser = userRepository.findById(user.id()).orElseThrow(this::invalidTokenException);
            if (latestUser.status() == UserStatus.ACTIVE && latestUser.emailVerified()) {
                return alreadyVerifiedResult(latestUser);
            }
            throw invalidTokenException();
        }

        outboxEventRepository.save(
                userAccountOutboxService.userActivatedAfterEmailVerification(
                        user.id(),
                        user.email().normalizedValue(),
                        now
                )
        );

        log.info("Email verified successfully. userId={}", user.id());
        return new VerifyEmailResult(
                user.id().toString(),
                true,
                UserStatus.ACTIVE.name(),
                SUCCESS_MESSAGE
        );
    }

    private Optional<VerificationToken> resolveVerificationToken(String rawToken, Instant now) {
        return findMatchingToken(verificationTokenRepository.findUnusedByType(
                VerificationTokenType.EMAIL_VERIFY,
                now
        ), rawToken);
    }

    private Optional<VerifyEmailResult> tryIdempotentWithUsedToken(String rawToken) {
        Optional<VerificationToken> usedToken = findMatchingToken(
                verificationTokenRepository.findUsedByType(VerificationTokenType.EMAIL_VERIFY),
                rawToken
        );
        if (usedToken.isEmpty()) {
            return Optional.empty();
        }
        return userRepository.findById(usedToken.get().userId())
                .filter(user -> user.status() == UserStatus.ACTIVE && user.emailVerified())
                .map(this::alreadyVerifiedResult);
    }

    private Optional<VerificationToken> findMatchingToken(List<VerificationToken> candidates, String rawToken) {
        for (VerificationToken candidate : candidates) {
            if (passwordHashingService.matches(rawToken, PasswordHash.of(candidate.tokenHash()))) {
                return Optional.of(candidate);
            }
        }
        return Optional.empty();
    }

    private VerifyEmailResult alreadyVerifiedResult(User user) {
        return new VerifyEmailResult(
                user.id().toString(),
                true,
                UserStatus.ACTIVE.name(),
                ALREADY_VERIFIED_MESSAGE
        );
    }

    private AppException invalidTokenException() {
        return new AppException(
                ErrorCode.INVALID_EMAIL_VERIFICATION_TOKEN,
                ErrorCode.INVALID_EMAIL_VERIFICATION_TOKEN.defaultMessage(),
                "token",
                "INVALID_OR_EXPIRED"
        );
    }
}
