package com.twohands.auth_service.application.auth.forgotpassword;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.auth_service.domain.outbox.OutboxEvent;
import com.twohands.auth_service.domain.outbox.OutboxEventRepository;
import com.twohands.auth_service.domain.outbox.OutboxStatus;
import com.twohands.auth_service.domain.user.VerificationToken;
import com.twohands.auth_service.domain.user.VerificationTokenRepository;
import com.twohands.auth_service.domain.user.VerificationTokenType;
import com.twohands.auth_service.domain.user.User;
import com.twohands.auth_service.domain.user.UserRepository;
import com.twohands.auth_service.exception.AppException;
import com.twohands.auth_service.exception.ErrorCode;
import com.twohands.auth_service.security.token.TokenHashingService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class ForgotPasswordUseCase {

    private static final String FORGOT_PASSWORD_SUCCESS_MESSAGE =
            "Neu email hop le, chung toi da gui huong dan dat lai mat khau.";

    private final UserRepository userRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final ForgotPasswordValidationService validationService;
    private final ForgotPasswordRateLimitService forgotPasswordRateLimitService;
    private final TokenHashingService tokenHashingService;
    private final ObjectMapper objectMapper;
    private final SecureRandom secureRandom = new SecureRandom();
    private final long resetTokenTtlSeconds;

    public ForgotPasswordUseCase(
            UserRepository userRepository,
            VerificationTokenRepository verificationTokenRepository,
            OutboxEventRepository outboxEventRepository,
            ForgotPasswordValidationService validationService,
            ForgotPasswordRateLimitService forgotPasswordRateLimitService,
            TokenHashingService tokenHashingService,
            ObjectMapper objectMapper,
            @Value("${auth.forgot-password.reset-token-ttl-seconds:900}") long resetTokenTtlSeconds
    ) {
        this.userRepository = userRepository;
        this.verificationTokenRepository = verificationTokenRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.validationService = validationService;
        this.forgotPasswordRateLimitService = forgotPasswordRateLimitService;
        this.tokenHashingService = tokenHashingService;
        this.objectMapper = objectMapper;
        this.resetTokenTtlSeconds = resetTokenTtlSeconds;
    }

    @Transactional
    public void execute(ForgotPasswordCommand command) {
        String normalizedEmail = validationService.normalizeAndValidateEmail(command.email());
        forgotPasswordRateLimitService.validateForgotPasswordAttempt(normalizedEmail, command.ipAddress());

        User user = userRepository.findByEmailNormalized(normalizedEmail).orElse(null);
        if (user == null) {
            return;
        }

        Instant now = Instant.now();
        String rawResetToken = generateResetToken();
        VerificationToken token = new VerificationToken(
                UUID.randomUUID(),
                user.id(),
                tokenHashingService.sha256(rawResetToken),
                VerificationTokenType.PASSWORD_RESET,
                now.plusSeconds(resetTokenTtlSeconds),
                null,
                now
        );
        verificationTokenRepository.save(token);
        outboxEventRepository.save(buildResetOutboxEvent(user, rawResetToken, now));
    }

    public String successMessage() {
        return FORGOT_PASSWORD_SUCCESS_MESSAGE;
    }

    private OutboxEvent buildResetOutboxEvent(User user, String rawResetToken, Instant now) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("user_id", user.id().toString());
        payload.put("email", user.email().normalizedValue());
        payload.put("verification_token", rawResetToken);
        payload.put("verification_token_type", VerificationTokenType.PASSWORD_RESET.name());

        String payloadJson;
        try {
            payloadJson = objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException ex) {
            throw new AppException(ErrorCode.INTERNAL_ERROR, "Cannot serialize outbox payload", ex);
        }

        return new OutboxEvent(
                UUID.randomUUID(),
                "PASSWORD_RESET_REQUESTED",
                "auth-service",
                payloadJson,
                OutboxStatus.PENDING,
                0,
                now,
                null,
                null
        );
    }

    private String generateResetToken() {
        byte[] bytes = new byte[16];
        secureRandom.nextBytes(bytes);
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
