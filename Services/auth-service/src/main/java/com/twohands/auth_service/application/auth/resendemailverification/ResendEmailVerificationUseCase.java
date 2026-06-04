package com.twohands.auth_service.application.auth.resendemailverification;

import com.twohands.auth_service.application.auth.common.EmailVerificationOtpGenerator;
import com.twohands.auth_service.application.auth.common.EmailVerificationOutboxService;
import com.twohands.auth_service.application.auth.register.PasswordHashingService;
import com.twohands.auth_service.domain.outbox.OutboxEventRepository;
import com.twohands.auth_service.domain.user.User;
import com.twohands.auth_service.domain.user.UserRepository;
import com.twohands.auth_service.domain.user.UserStatus;
import com.twohands.auth_service.domain.user.VerificationToken;
import com.twohands.auth_service.domain.user.VerificationTokenRepository;
import com.twohands.auth_service.domain.user.VerificationTokenType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class ResendEmailVerificationUseCase {

    private static final Logger log = LoggerFactory.getLogger(ResendEmailVerificationUseCase.class);
    private static final String SUCCESS_MESSAGE =
            "Neu email hop le va chua xac thuc, chung toi da gui lai ma xac thuc.";

    private final UserRepository userRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final ResendEmailVerificationValidationService validationService;
    private final ResendEmailVerificationRateLimitService rateLimitService;
    private final EmailVerificationOutboxService emailVerificationOutboxService;
    private final PasswordHashingService passwordHashingService;
    private final EmailVerificationOtpGenerator emailVerificationOtpGenerator;
    private final long verificationTtlSeconds;

    public ResendEmailVerificationUseCase(
            UserRepository userRepository,
            VerificationTokenRepository verificationTokenRepository,
            OutboxEventRepository outboxEventRepository,
            ResendEmailVerificationValidationService validationService,
            ResendEmailVerificationRateLimitService rateLimitService,
            EmailVerificationOutboxService emailVerificationOutboxService,
            PasswordHashingService passwordHashingService,
            EmailVerificationOtpGenerator emailVerificationOtpGenerator,
            @Value("${auth.register.verify-token-ttl-seconds:900}") long verificationTtlSeconds
    ) {
        this.userRepository = userRepository;
        this.verificationTokenRepository = verificationTokenRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.validationService = validationService;
        this.rateLimitService = rateLimitService;
        this.emailVerificationOutboxService = emailVerificationOutboxService;
        this.passwordHashingService = passwordHashingService;
        this.emailVerificationOtpGenerator = emailVerificationOtpGenerator;
        this.verificationTtlSeconds = verificationTtlSeconds;
    }

    @Transactional
    public void execute(ResendEmailVerificationCommand command) {
        String normalizedEmail = validationService.normalizeAndValidateEmail(command.email());
        rateLimitService.validateResendAttempt(normalizedEmail, command.ipAddress());

        User user = userRepository.findByEmailNormalized(normalizedEmail).orElse(null);
        if (user == null || user.status() != UserStatus.PENDING_VERIFICATION) {
            return;
        }

        Instant now = Instant.now();
        verificationTokenRepository.markUnusedAsUsedByUserIdAndType(
                user.id(),
                VerificationTokenType.EMAIL_VERIFY,
                now
        );

        String verificationTokenRaw = emailVerificationOtpGenerator.generate();
        VerificationToken verificationToken = new VerificationToken(
                UUID.randomUUID(),
                user.id(),
                passwordHashingService.hash(verificationTokenRaw).value(),
                VerificationTokenType.EMAIL_VERIFY,
                now.plusSeconds(verificationTtlSeconds),
                null,
                now
        );
        verificationTokenRepository.save(verificationToken);
        outboxEventRepository.save(emailVerificationOutboxService.build(user, verificationTokenRaw, now));

        log.info("Resend email verification processed. userId={}", user.id());
    }

    public String successMessage() {
        return SUCCESS_MESSAGE;
    }

}
