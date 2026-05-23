package com.twohands.auth_service.application.auth.register;

import com.twohands.auth_service.application.auth.common.EmailVerificationOutboxService;
import com.twohands.auth_service.application.auth.common.UserCreatedOutboxService;
import com.twohands.auth_service.domain.outbox.OutboxEventRepository;
import com.twohands.auth_service.domain.user.EmailAddress;
import com.twohands.auth_service.domain.user.User;
import com.twohands.auth_service.domain.user.UserProfile;
import com.twohands.auth_service.domain.user.UserProfileRepository;
import com.twohands.auth_service.domain.user.UserRepository;
import com.twohands.auth_service.domain.user.UserSettings;
import com.twohands.auth_service.domain.user.UserSettingsRepository;
import com.twohands.auth_service.domain.user.UserStatus;
import com.twohands.auth_service.domain.user.VerificationToken;
import com.twohands.auth_service.domain.user.VerificationTokenRepository;
import com.twohands.auth_service.domain.user.VerificationTokenType;
import com.twohands.auth_service.exception.AppException;
import com.twohands.auth_service.exception.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.UUID;

@Service
public class RegisterUserUseCase {

    private static final Logger log = LoggerFactory.getLogger(RegisterUserUseCase.class);

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final UserSettingsRepository userSettingsRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final PasswordHashingService passwordHashingService;
    private final RegisterValidationService validationService;
    private final RegisterRateLimitService registerRateLimitService;
    private final UserCreatedOutboxService userCreatedOutboxService;
    private final EmailVerificationOutboxService emailVerificationOutboxService;
    private final SecureRandom secureRandom;
    private final long verificationTtlSeconds;

    public RegisterUserUseCase(
            UserRepository userRepository,
            UserProfileRepository userProfileRepository,
            UserSettingsRepository userSettingsRepository,
            VerificationTokenRepository verificationTokenRepository,
            OutboxEventRepository outboxEventRepository,
            PasswordHashingService passwordHashingService,
            RegisterValidationService validationService,
            RegisterRateLimitService registerRateLimitService,
            UserCreatedOutboxService userCreatedOutboxService,
            EmailVerificationOutboxService emailVerificationOutboxService,
            @Value("${auth.register.verify-token-ttl-seconds:900}") long verificationTtlSeconds
    ) {
        this.userRepository = userRepository;
        this.userProfileRepository = userProfileRepository;
        this.userSettingsRepository = userSettingsRepository;
        this.verificationTokenRepository = verificationTokenRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.passwordHashingService = passwordHashingService;
        this.validationService = validationService;
        this.registerRateLimitService = registerRateLimitService;
        this.userCreatedOutboxService = userCreatedOutboxService;
        this.emailVerificationOutboxService = emailVerificationOutboxService;
        this.secureRandom = new SecureRandom();
        this.verificationTtlSeconds = verificationTtlSeconds;
    }

    @Transactional
    public RegisterUserResult execute(RegisterUserCommand command) {
        registerRateLimitService.validateRegisterAttempt(command.ipAddress());

        String normalizedEmail = validationService.normalizeAndValidateEmail(command.email());
        validationService.validatePassword(command.password());
        validationService.validateConfirmPassword(command.password(), command.confirmPassword());

        if (userRepository.existsByEmailNormalized(normalizedEmail)) {
            throw duplicateEmailException();
        }

        Instant now = Instant.now();
        UUID userId = UUID.randomUUID();
        String displayName = buildDefaultDisplayName(normalizedEmail);

        try {
            User user = User.registerWithEmail(
                    userId,
                    EmailAddress.of(normalizedEmail),
                    passwordHashingService.hash(command.password()),
                    now
            );

            userRepository.save(user);
            userProfileRepository.save(UserProfile.createDefault(userId, displayName, now));
            userSettingsRepository.save(UserSettings.createDefault(userId, now));

            String verificationTokenRaw = generateVerificationToken();
            VerificationToken verificationToken = new VerificationToken(
                    UUID.randomUUID(),
                    userId,
                    passwordHashingService.hash(verificationTokenRaw).value(),
                    VerificationTokenType.EMAIL_VERIFY,
                    now.plusSeconds(verificationTtlSeconds),
                    null,
                    now
            );
            verificationTokenRepository.save(verificationToken);

            outboxEventRepository.save(
                    userCreatedOutboxService.build(
                            user.id(),
                            user.email().normalizedValue(),
                            user.status().name(),
                            now
                    )
            );
            outboxEventRepository.save(emailVerificationOutboxService.build(user, verificationTokenRaw, now));

            log.info("User registered with pending verification. userId={}, email={}", userId, normalizedEmail);
            return new RegisterUserResult(userId.toString(), normalizedEmail, UserStatus.PENDING_VERIFICATION.name());
        } catch (DataIntegrityViolationException ex) {
            if (isEmailUniqueViolation(ex)) {
                throw duplicateEmailException();
            }
            throw new AppException(ErrorCode.INTERNAL_ERROR, ErrorCode.INTERNAL_ERROR.defaultMessage(), ex);
        }
    }

    private String generateVerificationToken() {
        byte[] bytes = new byte[16];
        secureRandom.nextBytes(bytes);
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private String buildDefaultDisplayName(String normalizedEmail) {
        String localPart = normalizedEmail.split("@")[0];
        if (localPart.isBlank()) {
            return "user_" + System.currentTimeMillis();
        }
        return localPart.length() > 100 ? localPart.substring(0, 100) : localPart;
    }

    private boolean isEmailUniqueViolation(DataIntegrityViolationException ex) {
        Throwable cause = ex.getMostSpecificCause();
        return cause != null && cause.getMessage() != null && cause.getMessage().contains("email_normalized");
    }

    private AppException duplicateEmailException() {
        return new AppException(
                ErrorCode.DUPLICATE_EMAIL,
                ErrorCode.DUPLICATE_EMAIL.defaultMessage(),
                "email",
                "DUPLICATE"
        );
    }
}
