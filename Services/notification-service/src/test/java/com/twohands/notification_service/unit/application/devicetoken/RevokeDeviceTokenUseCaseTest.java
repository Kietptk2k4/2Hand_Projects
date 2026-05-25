package com.twohands.notification_service.unit.application.devicetoken;

import com.twohands.notification_service.application.devicetoken.RevokeDeviceTokenCommand;
import com.twohands.notification_service.application.devicetoken.RevokeDeviceTokenUseCase;
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RevokeDeviceTokenUseCaseTest {

    private static final UUID USER_ID = UUID.randomUUID();
    private static final UUID OTHER_USER_ID = UUID.randomUUID();
    private static final UUID TOKEN_ID = UUID.randomUUID();
    private static final Instant NOW = Instant.parse("2026-05-24T12:00:00Z");

    @Mock
    private UserDeviceTokenRepository userDeviceTokenRepository;

    private RevokeDeviceTokenUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new RevokeDeviceTokenUseCase(userDeviceTokenRepository);
    }

    @Test
    void execute_deactivatesOwnedActiveToken() {
        when(userDeviceTokenRepository.findByDeviceToken("fcm-token"))
                .thenReturn(Optional.of(activeToken(USER_ID)));
        when(userDeviceTokenRepository.save(any(UserDeviceToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var result = useCase.execute(new RevokeDeviceTokenCommand(USER_ID, " fcm-token "));

        assertEquals(TOKEN_ID, result.id());
        assertFalse(result.active());
        assertFalse(result.alreadyRevoked());
        verify(userDeviceTokenRepository).save(any(UserDeviceToken.class));
    }

    @Test
    void execute_isIdempotentWhenTokenAlreadyInactive() {
        when(userDeviceTokenRepository.findByDeviceToken("fcm-token"))
                .thenReturn(Optional.of(inactiveToken(USER_ID)));

        var result = useCase.execute(new RevokeDeviceTokenCommand(USER_ID, "fcm-token"));

        assertFalse(result.active());
        assertTrue(result.alreadyRevoked());
        verify(userDeviceTokenRepository, never()).save(any());
    }

    @Test
    void execute_throwsNotFoundWhenTokenMissing() {
        when(userDeviceTokenRepository.findByDeviceToken("missing")).thenReturn(Optional.empty());

        AppException ex = assertThrows(AppException.class, () ->
                useCase.execute(new RevokeDeviceTokenCommand(USER_ID, "missing"))
        );

        assertEquals(ErrorCode.DEVICE_TOKEN_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    void execute_throwsNotFoundWhenTokenOwnedByAnotherUser() {
        when(userDeviceTokenRepository.findByDeviceToken("fcm-token"))
                .thenReturn(Optional.of(activeToken(OTHER_USER_ID)));

        AppException ex = assertThrows(AppException.class, () ->
                useCase.execute(new RevokeDeviceTokenCommand(USER_ID, "fcm-token"))
        );

        assertEquals(ErrorCode.DEVICE_TOKEN_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    void execute_throwsUnauthorizedWhenUserIdMissing() {
        AppException ex = assertThrows(AppException.class, () ->
                useCase.execute(new RevokeDeviceTokenCommand(null, "fcm-token"))
        );

        assertEquals(ErrorCode.UNAUTHORIZED, ex.getErrorCode());
    }

    private UserDeviceToken activeToken(UUID userId) {
        return new UserDeviceToken(
                TOKEN_ID,
                userId,
                DeviceType.WEB,
                "fcm-token",
                true,
                NOW,
                NOW,
                NOW
        );
    }

    private UserDeviceToken inactiveToken(UUID userId) {
        return new UserDeviceToken(
                TOKEN_ID,
                userId,
                DeviceType.WEB,
                "fcm-token",
                false,
                NOW,
                NOW,
                NOW
        );
    }
}
