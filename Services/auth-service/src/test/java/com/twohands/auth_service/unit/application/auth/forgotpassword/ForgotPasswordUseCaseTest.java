package com.twohands.auth_service.unit.application.auth.forgotpassword;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.auth_service.application.auth.forgotpassword.ForgotPasswordCommand;
import com.twohands.auth_service.application.auth.forgotpassword.ForgotPasswordRateLimitService;
import com.twohands.auth_service.application.auth.forgotpassword.ForgotPasswordUseCase;
import com.twohands.auth_service.application.auth.forgotpassword.ForgotPasswordValidationService;
import com.twohands.auth_service.domain.outbox.OutboxEventRepository;
import com.twohands.auth_service.domain.user.EmailAddress;
import com.twohands.auth_service.domain.user.PasswordHash;
import com.twohands.auth_service.domain.user.User;
import com.twohands.auth_service.domain.user.UserRepository;
import com.twohands.auth_service.domain.user.UserStatus;
import com.twohands.auth_service.domain.user.VerificationTokenRepository;
import com.twohands.auth_service.security.token.TokenHashingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ForgotPasswordUseCaseTest {

    private final UserRepository userRepository = Mockito.mock(UserRepository.class);
    private final VerificationTokenRepository verificationTokenRepository = Mockito.mock(VerificationTokenRepository.class);
    private final OutboxEventRepository outboxEventRepository = Mockito.mock(OutboxEventRepository.class);
    private final ForgotPasswordRateLimitService rateLimitService = Mockito.mock(ForgotPasswordRateLimitService.class);
    private final TokenHashingService tokenHashingService = Mockito.mock(TokenHashingService.class);

    private ForgotPasswordUseCase useCase;

    @BeforeEach
    void setup() {
        useCase = new ForgotPasswordUseCase(
                userRepository,
                verificationTokenRepository,
                outboxEventRepository,
                new ForgotPasswordValidationService(),
                rateLimitService,
                tokenHashingService,
                new ObjectMapper(),
                900
        );
        when(tokenHashingService.sha256(any())).thenReturn("hashed-token");
    }

    @Test
    void shouldNotCreateTokenOrEventWhenEmailNotExists() {
        when(userRepository.findByEmailNormalized("missing@example.com")).thenReturn(Optional.empty());

        useCase.execute(new ForgotPasswordCommand("missing@example.com", "127.0.0.1"));

        verify(verificationTokenRepository, never()).save(any());
        verify(outboxEventRepository, never()).save(any());
    }

    @Test
    void shouldCreateTokenAndEventWhenEmailExists() {
        when(userRepository.findByEmailNormalized("exists@example.com"))
                .thenReturn(Optional.of(buildUser("exists@example.com")));

        useCase.execute(new ForgotPasswordCommand("exists@example.com", "127.0.0.1"));

        verify(verificationTokenRepository).save(any());
        verify(outboxEventRepository).save(any());
    }

    private User buildUser(String email) {
        Instant now = Instant.now();
        return User.rehydrate(
                UUID.randomUUID(),
                EmailAddress.of(email),
                PasswordHash.of("$2a$10$abcdefghijklmnopqrstuv"),
                UserStatus.ACTIVE,
                true,
                false,
                null,
                null,
                null,
                now.minusSeconds(1000),
                now.minusSeconds(1000)
        );
    }
}
