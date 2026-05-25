package com.twohands.notification_service.unit.application.devicetoken;

import com.twohands.notification_service.application.devicetoken.DeactivateInvalidDeviceTokenCommand;
import com.twohands.notification_service.application.devicetoken.DeactivateInvalidDeviceTokenUseCase;
import com.twohands.notification_service.domain.devicetoken.DeactivateInvalidDeviceTokenOutcome;
import com.twohands.notification_service.domain.devicetoken.DeviceType;
import com.twohands.notification_service.domain.devicetoken.UserDeviceToken;
import com.twohands.notification_service.domain.devicetoken.UserDeviceTokenRepository;
import com.twohands.notification_service.exception.AppException;
import com.twohands.notification_service.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeactivateInvalidDeviceTokenUseCaseTest {

    private static final UUID TOKEN_ID = UUID.randomUUID();
    private static final UUID USER_ID = UUID.randomUUID();
    private static final Instant NOW = Instant.parse("2026-05-24T12:00:00Z");

    @Mock
    private UserDeviceTokenRepository userDeviceTokenRepository;

    private DeactivateInvalidDeviceTokenUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new DeactivateInvalidDeviceTokenUseCase(userDeviceTokenRepository);
    }

    @Test
    void execute_deactivatesActiveToken() {
        when(userDeviceTokenRepository.findByDeviceToken("stale-fcm-token"))
                .thenReturn(Optional.of(activeToken()));
        when(userDeviceTokenRepository.save(any(UserDeviceToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var result = useCase.execute(new DeactivateInvalidDeviceTokenCommand(" stale-fcm-token "));

        assertEquals(DeactivateInvalidDeviceTokenOutcome.DEACTIVATED, result.outcome());
        verify(userDeviceTokenRepository).save(any(UserDeviceToken.class));
    }

    @Test
    void execute_returnsAlreadyInactiveWhenTokenInactive() {
        when(userDeviceTokenRepository.findByDeviceToken("stale-fcm-token"))
                .thenReturn(Optional.of(inactiveToken()));

        var result = useCase.execute(new DeactivateInvalidDeviceTokenCommand("stale-fcm-token"));

        assertEquals(DeactivateInvalidDeviceTokenOutcome.ALREADY_INACTIVE, result.outcome());
        verify(userDeviceTokenRepository, never()).save(any());
    }

    @Test
    void execute_returnsNotFoundWhenTokenMissing() {
        when(userDeviceTokenRepository.findByDeviceToken("missing-token")).thenReturn(Optional.empty());

        var result = useCase.execute(new DeactivateInvalidDeviceTokenCommand("missing-token"));

        assertEquals(DeactivateInvalidDeviceTokenOutcome.NOT_FOUND, result.outcome());
        verify(userDeviceTokenRepository, never()).save(any());
    }

    @Test
    void execute_throwsValidationErrorWhenTokenBlank() {
        AppException ex = assertThrows(AppException.class, () ->
                useCase.execute(new DeactivateInvalidDeviceTokenCommand("   "))
        );

        assertEquals(ErrorCode.VALIDATION_ERROR, ex.getErrorCode());
    }

    private UserDeviceToken activeToken() {
        return new UserDeviceToken(
                TOKEN_ID,
                USER_ID,
                DeviceType.ANDROID,
                "stale-fcm-token",
                true,
                NOW,
                null,
                NOW
        );
    }

    private UserDeviceToken inactiveToken() {
        return new UserDeviceToken(
                TOKEN_ID,
                USER_ID,
                DeviceType.ANDROID,
                "stale-fcm-token",
                false,
                NOW,
                null,
                NOW
        );
    }
}
