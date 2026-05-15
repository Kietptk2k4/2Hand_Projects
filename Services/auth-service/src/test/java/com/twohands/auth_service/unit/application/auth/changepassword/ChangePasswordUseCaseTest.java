package com.twohands.auth_service.unit.application.auth.changepassword;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.auth_service.application.auth.changepassword.ChangePasswordCommand;
import com.twohands.auth_service.application.auth.changepassword.ChangePasswordUseCase;
import com.twohands.auth_service.application.auth.changepassword.ChangePasswordValidationService;
import com.twohands.auth_service.application.auth.register.PasswordHashingService;
import com.twohands.auth_service.domain.outbox.OutboxEventRepository;
import com.twohands.auth_service.domain.session.RefreshTokenSessionRepository;
import com.twohands.auth_service.domain.user.EmailAddress;
import com.twohands.auth_service.domain.user.PasswordHash;
import com.twohands.auth_service.domain.user.User;
import com.twohands.auth_service.domain.user.UserRepository;
import com.twohands.auth_service.domain.user.UserStatus;
import com.twohands.auth_service.exception.AppException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ChangePasswordUseCaseTest {

    private final UserRepository userRepository = Mockito.mock(UserRepository.class);
    private final PasswordHashingService passwordHashingService = Mockito.mock(PasswordHashingService.class);
    private final RefreshTokenSessionRepository refreshTokenSessionRepository = Mockito.mock(RefreshTokenSessionRepository.class);
    private final OutboxEventRepository outboxEventRepository = Mockito.mock(OutboxEventRepository.class);

    private ChangePasswordUseCase useCase;
    private UUID userId;
    private User user;

    @BeforeEach
    void setup() {
        useCase = new ChangePasswordUseCase(
                new ChangePasswordValidationService(),
                userRepository,
                passwordHashingService,
                refreshTokenSessionRepository,
                outboxEventRepository,
                new ObjectMapper()
        );
        userId = UUID.randomUUID();
        user = buildUser(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    }

    @Test
    void wrongCurrentPasswordShouldThrow400AndNotModifyData() {
        ChangePasswordCommand command = new ChangePasswordCommand(userId, "WrongPassword123", "NewPassword123", "NewPassword123");

        when(passwordHashingService.matches(command.currentPassword(), user.passwordHash())).thenReturn(false);

        AppException ex = assertThrows(AppException.class, () -> useCase.execute(command));

        assertEquals("Mat khau hien tai khong chinh xac.", ex.getMessage());
        verify(userRepository, never()).updatePassword(any(), any(), any());
        verify(refreshTokenSessionRepository, never()).revokeAllByUserId(any());
        verify(outboxEventRepository, never()).save(any());
    }

    private User buildUser(UUID id) {
        Instant now = Instant.now();
        return User.rehydrate(
                id,
                EmailAddress.of("change-password-user@example.com"),
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
