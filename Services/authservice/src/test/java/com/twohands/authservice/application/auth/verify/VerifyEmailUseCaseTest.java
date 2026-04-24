package com.twohands.authservice.application.auth.verify;

import com.twohands.authservice.application.auth.port.AttemptStore;
import com.twohands.authservice.application.auth.port.OtpStore;
import com.twohands.authservice.delivery.http.exception.BadRequestException;
import com.twohands.authservice.delivery.http.exception.TooManyRequestsException;
import com.twohands.authservice.domain.user.User;
import com.twohands.authservice.domain.user.UserRepository;
import com.twohands.authservice.domain.user.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VerifyEmailUseCaseTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private OtpStore otpStore;
    @Mock
    private AttemptStore attemptStore;

    private VerifyEmailUseCase verifyEmailUseCase;

    @BeforeEach
    void setUp() {
        verifyEmailUseCase = new VerifyEmailUseCase(userRepository, otpStore, attemptStore);
        ReflectionTestUtils.setField(verifyEmailUseCase, "maxAttempts", 5);
        ReflectionTestUtils.setField(verifyEmailUseCase, "otpTtl", 300L);
    }

    @Test
    @DisplayName("should verify email successfully when OTP matches")
    void shouldVerifyEmailSuccessfully() {
        User user = pendingUser("user@example.com");
        when(userRepository.findByEmailNormalized("user@example.com")).thenReturn(Optional.of(user));
        when(attemptStore.getCount("auth:otp:fail:user@example.com")).thenReturn(0L);
        when(otpStore.get("auth:otp:register:user@example.com")).thenReturn("123456");
        when(userRepository.save(any(User.class))).thenReturn(user);

        verifyEmailUseCase.execute("user@example.com", "123456");

        verify(userRepository).save(any(User.class));
        verify(otpStore).delete("auth:otp:register:user@example.com");
        verify(attemptStore).delete("auth:otp:fail:user@example.com");
    }

    @Test
    @DisplayName("should set user status to ACTIVE after successful verification")
    void shouldActivateUserOnSuccess() {
        User user = pendingUser("active@example.com");
        when(userRepository.findByEmailNormalized("active@example.com")).thenReturn(Optional.of(user));
        when(attemptStore.getCount(any())).thenReturn(0L);
        when(otpStore.get(any())).thenReturn("999999");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        verifyEmailUseCase.execute("active@example.com", "999999");

        assert user.getStatus() == UserStatus.ACTIVE;
        assert Boolean.TRUE.equals(user.getEmailVerified());
    }

    @Test
    @DisplayName("should throw BadRequestException when user not found")
    void shouldThrowWhenUserNotFound() {
        when(userRepository.findByEmailNormalized("ghost@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> verifyEmailUseCase.execute("ghost@example.com", "123456"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("not found");
    }

    @Test
    @DisplayName("should throw BadRequestException when email already verified")
    void shouldThrowWhenAlreadyVerified() {
        User user = pendingUser("done@example.com");
        user.setEmailVerified(true);
        user.setStatus(UserStatus.ACTIVE);
        when(userRepository.findByEmailNormalized("done@example.com")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> verifyEmailUseCase.execute("done@example.com", "123456"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("already verified");
    }

    @Test
    @DisplayName("should throw TooManyRequestsException when max attempts already reached")
    void shouldThrowWhenMaxAttemptsReached() {
        User user = pendingUser("locked@example.com");
        when(userRepository.findByEmailNormalized("locked@example.com")).thenReturn(Optional.of(user));
        when(attemptStore.getCount("auth:otp:fail:locked@example.com")).thenReturn(5L);

        assertThatThrownBy(() -> verifyEmailUseCase.execute("locked@example.com", "123456"))
                .isInstanceOf(TooManyRequestsException.class)
                .hasMessageContaining("Maximum OTP attempts exceeded");

        verify(otpStore, never()).get(any());
    }

    @Test
    @DisplayName("should throw BadRequestException when OTP expired")
    void shouldThrowWhenOtpExpired() {
        User user = pendingUser("user@example.com");
        when(userRepository.findByEmailNormalized("user@example.com")).thenReturn(Optional.of(user));
        when(attemptStore.getCount(any())).thenReturn(0L);
        when(otpStore.get(any())).thenReturn(null);

        assertThatThrownBy(() -> verifyEmailUseCase.execute("user@example.com", "123456"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("expired");
    }

    @Test
    @DisplayName("should increment attempt count on wrong OTP")
    void shouldIncrementAttemptsOnWrongOtp() {
        User user = pendingUser("user@example.com");
        when(userRepository.findByEmailNormalized("user@example.com")).thenReturn(Optional.of(user));
        when(attemptStore.getCount(any())).thenReturn(1L);
        when(otpStore.get(any())).thenReturn("correct");
        when(attemptStore.increment(eq("auth:otp:fail:user@example.com"), eq(300L))).thenReturn(2L);

        assertThatThrownBy(() -> verifyEmailUseCase.execute("user@example.com", "wrong"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Invalid OTP");

        verify(attemptStore).increment("auth:otp:fail:user@example.com", 300L);
        verify(userRepository, never()).save(any());
    }

    private User pendingUser(String email) {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail(email);
        user.setEmailNormalized(email);
        user.setStatus(UserStatus.PENDING_VERIFICATION);
        user.setEmailVerified(false);
        return user;
    }
}
