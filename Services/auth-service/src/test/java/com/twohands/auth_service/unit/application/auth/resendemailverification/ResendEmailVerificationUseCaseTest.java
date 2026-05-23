package com.twohands.auth_service.unit.application.auth.resendemailverification;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.auth_service.application.auth.common.EmailVerificationOutboxService;
import com.twohands.auth_service.application.auth.register.PasswordHashingService;
import com.twohands.auth_service.application.auth.resendemailverification.ResendEmailVerificationCommand;
import com.twohands.auth_service.application.auth.resendemailverification.ResendEmailVerificationRateLimitService;
import com.twohands.auth_service.application.auth.resendemailverification.ResendEmailVerificationUseCase;
import com.twohands.auth_service.application.auth.resendemailverification.ResendEmailVerificationValidationService;
import com.twohands.auth_service.domain.outbox.OutboxEventRepository;
import com.twohands.auth_service.domain.user.EmailAddress;
import com.twohands.auth_service.domain.user.PasswordHash;
import com.twohands.auth_service.domain.user.User;
import com.twohands.auth_service.domain.user.UserRepository;
import com.twohands.auth_service.domain.user.UserStatus;
import com.twohands.auth_service.domain.user.VerificationTokenRepository;
import com.twohands.auth_service.domain.user.VerificationTokenType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ResendEmailVerificationUseCaseTest {

    private final UserRepository userRepository = Mockito.mock(UserRepository.class);
    private final VerificationTokenRepository verificationTokenRepository = Mockito.mock(VerificationTokenRepository.class);
    private final OutboxEventRepository outboxEventRepository = Mockito.mock(OutboxEventRepository.class);
    private final ResendEmailVerificationRateLimitService rateLimitService =
            Mockito.mock(ResendEmailVerificationRateLimitService.class);
    private final PasswordHashingService passwordHashingService = Mockito.mock(PasswordHashingService.class);

    private ResendEmailVerificationUseCase useCase;

    @BeforeEach
    void setup() {
        useCase = new ResendEmailVerificationUseCase(
                userRepository,
                verificationTokenRepository,
                outboxEventRepository,
                new ResendEmailVerificationValidationService(),
                rateLimitService,
                new EmailVerificationOutboxService(new ObjectMapper()),
                passwordHashingService,
                900
        );
        when(passwordHashingService.hash(any())).thenReturn(PasswordHash.of("hashed-token"));
    }

    @Test
    void shouldNotCreateTokenOrEventWhenEmailNotExists() {
        when(userRepository.findByEmailNormalized("missing@example.com")).thenReturn(Optional.empty());

        useCase.execute(new ResendEmailVerificationCommand("missing@example.com", "127.0.0.1"));

        verify(verificationTokenRepository, never()).save(any());
        verify(outboxEventRepository, never()).save(any());
    }

    @Test
    void shouldNotCreateTokenOrEventWhenUserIsActive() {
        when(userRepository.findByEmailNormalized("active@example.com"))
                .thenReturn(Optional.of(buildUser("active@example.com", UserStatus.ACTIVE)));

        useCase.execute(new ResendEmailVerificationCommand("active@example.com", "127.0.0.1"));

        verify(verificationTokenRepository, never()).save(any());
        verify(outboxEventRepository, never()).save(any());
    }

    @Test
    void shouldInvalidateOldTokenCreateNewTokenAndOutboxWhenPendingVerification() {
        User user = buildUser("pending@example.com", UserStatus.PENDING_VERIFICATION);
        when(userRepository.findByEmailNormalized("pending@example.com")).thenReturn(Optional.of(user));

        useCase.execute(new ResendEmailVerificationCommand("pending@example.com", "127.0.0.1"));

        verify(verificationTokenRepository).markUnusedAsUsedByUserIdAndType(
                eq(user.id()),
                eq(VerificationTokenType.EMAIL_VERIFY),
                any(Instant.class)
        );
        verify(verificationTokenRepository).save(any());
        verify(outboxEventRepository).save(any());
    }

    private User buildUser(String email, UserStatus status) {
        Instant now = Instant.now();
        return User.rehydrate(
                UUID.randomUUID(),
                EmailAddress.of(email),
                PasswordHash.of("$2a$10$abcdefghijklmnopqrstuv"),
                status,
                status == UserStatus.ACTIVE,
                false,
                null,
                null,
                null,
                now.minusSeconds(1000),
                now.minusSeconds(1000)
        );
    }
}
