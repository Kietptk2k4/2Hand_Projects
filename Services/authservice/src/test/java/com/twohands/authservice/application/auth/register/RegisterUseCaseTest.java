package com.twohands.authservice.application.auth.register;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.authservice.application.auth.event.OutboxRecord;
import com.twohands.authservice.application.auth.port.OtpGenerator;
import com.twohands.authservice.application.auth.port.OtpStore;
import com.twohands.authservice.application.auth.port.OutboxRepository;
import com.twohands.authservice.application.auth.port.PasswordHasher;
import com.twohands.authservice.delivery.http.exception.BadRequestException;
import com.twohands.authservice.domain.role.Role;
import com.twohands.authservice.domain.role.RoleRepository;
import com.twohands.authservice.domain.user.User;
import com.twohands.authservice.domain.user.UserRepository;
import com.twohands.authservice.domain.user.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegisterUseCaseTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private PasswordHasher passwordHasher;
    @Mock
    private OtpGenerator otpGenerator;
    @Mock
    private OtpStore otpStore;
    @Mock
    private OutboxRepository outboxRepository;

    private RegisterUseCase registerUseCase;

    @BeforeEach
    void setUp() {
        registerUseCase = new RegisterUseCase(
                userRepository, roleRepository, passwordHasher,
                otpGenerator, otpStore, outboxRepository, new ObjectMapper()
        );
    }

    @Test
    @DisplayName("should register user successfully and persist outbox event")
    void shouldRegisterSuccessfully() {
        RegisterCommand cmd = new RegisterCommand("Test@Example.com", "password123", "password123");

        when(userRepository.existsByEmailNormalized("test@example.com")).thenReturn(false);
        when(passwordHasher.hash("password123")).thenReturn("hashed_password");

        User firstSave = userWithId(UUID.randomUUID(), UserStatus.PENDING_VERIFICATION);
        when(userRepository.save(any(User.class))).thenReturn(firstSave);

        Role role = roleWithId(UUID.randomUUID(), "USER");
        when(roleRepository.findByCode("USER")).thenReturn(Optional.of(role));
        when(otpGenerator.generate()).thenReturn("654321");

        RegisterResult result = registerUseCase.execute(cmd);

        assertThat(result.userId()).isEqualTo(firstSave.getId());
        assertThat(result.status()).isEqualTo("PENDING_VERIFICATION");

        verify(otpStore).save(contains("auth:otp:register:test@example.com"), eq("654321"), eq(300L));
        verify(outboxRepository).save(any(OutboxRecord.class));
    }

    @Test
    @DisplayName("should throw BadRequestException when email already registered")
    void shouldThrowWhenEmailAlreadyExists() {
        RegisterCommand cmd = new RegisterCommand("existing@example.com", "password123", "password123");
        when(userRepository.existsByEmailNormalized("existing@example.com")).thenReturn(true);

        assertThatThrownBy(() -> registerUseCase.execute(cmd))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("already registered");

        verify(userRepository, never()).save(any());
        verify(outboxRepository, never()).save(any());
    }

    @Test
    @DisplayName("should throw BadRequestException when passwords do not match")
    void shouldThrowWhenPasswordsDoNotMatch() {
        RegisterCommand cmd = new RegisterCommand("user@example.com", "password123", "different");

        assertThatThrownBy(() -> registerUseCase.execute(cmd))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("do not match");

        verify(userRepository, never()).existsByEmailNormalized(anyString());
    }

    @Test
    @DisplayName("should throw BadRequestException when email is blank")
    void shouldThrowWhenEmailIsBlank() {
        RegisterCommand cmd = new RegisterCommand("  ", "password123", "password123");

        assertThatThrownBy(() -> registerUseCase.execute(cmd))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("required");
    }

    @Test
    @DisplayName("should persist outbox record with correct event type")
    void shouldPersistCorrectOutboxRecord() {
        RegisterCommand cmd = new RegisterCommand("user@example.com", "pass", "pass");

        when(userRepository.existsByEmailNormalized("user@example.com")).thenReturn(false);
        when(passwordHasher.hash("pass")).thenReturn("hashed");

        User savedUser = userWithId(UUID.randomUUID(), UserStatus.PENDING_VERIFICATION);
        savedUser.setEmail("user@example.com");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        Role role = roleWithId(UUID.randomUUID(), "USER");
        when(roleRepository.findByCode("USER")).thenReturn(Optional.of(role));
        when(otpGenerator.generate()).thenReturn("111222");

        registerUseCase.execute(cmd);

        ArgumentCaptor<OutboxRecord> captor = ArgumentCaptor.forClass(OutboxRecord.class);
        verify(outboxRepository).save(captor.capture());

        OutboxRecord record = captor.getValue();
        assertThat(record.eventType()).isEqualTo("USER_REGISTERED");
        assertThat(record.source()).isEqualTo("auth-service");
        assertThat(record.payload()).contains("user@example.com");
        assertThat(record.payload()).contains("111222");
    }

    private User userWithId(UUID id, UserStatus status) {
        User user = new User();
        user.setId(id);
        user.setStatus(status);
        return user;
    }

    private Role roleWithId(UUID id, String code) {
        Role role = new Role();
        role.setId(id);
        role.setCode(code);
        return role;
    }
}
