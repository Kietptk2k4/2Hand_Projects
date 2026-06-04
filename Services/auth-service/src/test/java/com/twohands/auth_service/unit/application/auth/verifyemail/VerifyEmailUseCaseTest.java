package com.twohands.auth_service.unit.application.auth.verifyemail;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.auth_service.application.auth.register.PasswordHashingService;
import com.twohands.auth_service.application.auth.verifyemail.VerifyEmailCommand;
import com.twohands.auth_service.application.auth.verifyemail.VerifyEmailResult;
import com.twohands.auth_service.application.auth.verifyemail.VerifyEmailUseCase;
import com.twohands.auth_service.application.auth.verifyemail.VerifyEmailValidationService;
import com.twohands.auth_service.application.useraccount.common.UserAccountOutboxService;
import com.twohands.auth_service.domain.outbox.OutboxEventRepository;
import com.twohands.auth_service.domain.user.EmailAddress;
import com.twohands.auth_service.domain.user.PasswordHash;
import com.twohands.auth_service.domain.user.User;
import com.twohands.auth_service.domain.user.UserRepository;
import com.twohands.auth_service.domain.user.UserStatus;
import com.twohands.auth_service.domain.user.VerificationToken;
import com.twohands.auth_service.domain.user.VerificationTokenRepository;
import com.twohands.auth_service.domain.user.VerificationTokenType;
import com.twohands.auth_service.exception.AppException;
import com.twohands.auth_service.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class VerifyEmailUseCaseTest {

    private final UserRepository userRepository = Mockito.mock(UserRepository.class);
    private final VerificationTokenRepository verificationTokenRepository = Mockito.mock(VerificationTokenRepository.class);
    private final OutboxEventRepository outboxEventRepository = Mockito.mock(OutboxEventRepository.class);
    private final PasswordHashingService passwordHashingService = Mockito.mock(PasswordHashingService.class);

    private VerifyEmailUseCase useCase;

    @BeforeEach
    void setup() {
        useCase = new VerifyEmailUseCase(
                userRepository,
                verificationTokenRepository,
                outboxEventRepository,
                new VerifyEmailValidationService(),
                passwordHashingService,
                new UserAccountOutboxService(new ObjectMapper())
        );
    }

    @Test
    void shouldActivatePendingUserWhenTokenIsValid() {
        UUID userId = UUID.randomUUID();
        UUID tokenId = UUID.randomUUID();
        Instant now = Instant.now();
        VerificationToken token = new VerificationToken(
                tokenId,
                userId,
                "bcrypt-hash",
                VerificationTokenType.EMAIL_VERIFY,
                now.plusSeconds(600),
                null,
                now
        );
        User user = buildUser(userId, UserStatus.PENDING_VERIFICATION, false);

        when(verificationTokenRepository.findUnusedByType(eq(VerificationTokenType.EMAIL_VERIFY), any(Instant.class)))
                .thenReturn(List.of(token));
        when(passwordHashingService.matches(eq("valid-token"), any(PasswordHash.class))).thenReturn(true);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(verificationTokenRepository.markUsedById(eq(tokenId), any(Instant.class))).thenReturn(1);
        when(userRepository.markEmailVerifiedAndActive(eq(userId), any(Instant.class))).thenReturn(1);

        VerifyEmailResult result = useCase.execute(new VerifyEmailCommand("valid-token"));

        assertEquals(userId.toString(), result.userId());
        assertEquals(UserStatus.ACTIVE.name(), result.status());
        assertEquals("Xac thuc email thanh cong.", result.message());
        verify(outboxEventRepository).save(any());
    }

    @Test
    void shouldReturnIdempotentResultWhenUsedTokenMatchesActiveUser() {
        UUID userId = UUID.randomUUID();
        UUID tokenId = UUID.randomUUID();
        Instant now = Instant.now();
        VerificationToken usedToken = new VerificationToken(
                tokenId,
                userId,
                "used-bcrypt-hash",
                VerificationTokenType.EMAIL_VERIFY,
                now.plusSeconds(600),
                now.minusSeconds(30),
                now.minusSeconds(120)
        );
        User user = buildUser(userId, UserStatus.ACTIVE, true);

        when(verificationTokenRepository.findUnusedByType(any(), any())).thenReturn(List.of());
        when(verificationTokenRepository.findUsedByType(VerificationTokenType.EMAIL_VERIFY))
                .thenReturn(List.of(usedToken));
        when(passwordHashingService.matches(eq("used-token"), any(PasswordHash.class))).thenReturn(true);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        VerifyEmailResult result = useCase.execute(new VerifyEmailCommand("used-token"));

        assertEquals("Tai khoan da duoc xac thuc truoc do.", result.message());
        verify(userRepository, never()).markEmailVerifiedAndActive(any(), any());
        verify(outboxEventRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenTokenDoesNotMatch() {
        when(verificationTokenRepository.findUnusedByType(any(), any())).thenReturn(List.of());

        AppException ex = assertThrows(AppException.class, () -> useCase.execute(new VerifyEmailCommand("bad-token")));

        assertEquals(ErrorCode.INVALID_EMAIL_VERIFICATION_TOKEN, ex.getErrorCode());
        assertEquals("token", ex.getField());
        assertEquals("INVALID_OR_EXPIRED", ex.getReason());
    }

    private User buildUser(UUID userId, UserStatus status, boolean emailVerified) {
        Instant now = Instant.now();
        return User.rehydrate(
                userId,
                EmailAddress.of("verify@example.com"),
                PasswordHash.of("$2a$10$abcdefghijklmnopqrstuv"),
                status,
                emailVerified,
                false,
                null,
                null,
                null,
                now.minusSeconds(1000),
                now.minusSeconds(1000)
        );
    }
}
