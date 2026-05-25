package com.twohands.notification_service.unit.application.devicetoken;

import com.twohands.notification_service.application.devicetoken.RegisterDeviceTokenCommand;
import com.twohands.notification_service.application.devicetoken.RegisterDeviceTokenUseCase;
import com.twohands.notification_service.domain.devicetoken.DeviceType;
import com.twohands.notification_service.domain.devicetoken.UserDeviceToken;
import com.twohands.notification_service.domain.devicetoken.UserDeviceTokenRepository;
import com.twohands.notification_service.exception.AppException;
import com.twohands.notification_service.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegisterDeviceTokenUseCaseTest {

    private static final UUID USER_ID = UUID.randomUUID();
    private static final UUID OTHER_USER_ID = UUID.randomUUID();
    private static final UUID TOKEN_ID = UUID.randomUUID();
    private static final Instant CREATED_AT = Instant.parse("2026-05-20T08:00:00Z");

    @Mock
    private UserDeviceTokenRepository userDeviceTokenRepository;

    private RegisterDeviceTokenUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new RegisterDeviceTokenUseCase(userDeviceTokenRepository);
    }

    @Test
    void execute_registersNewActiveToken() {
        when(userDeviceTokenRepository.findByDeviceToken("fcm-token-1")).thenReturn(Optional.empty());
        when(userDeviceTokenRepository.save(any(UserDeviceToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var result = useCase.execute(new RegisterDeviceTokenCommand(USER_ID, "android", " fcm-token-1 "));

        assertEquals(DeviceType.ANDROID, result.deviceType());
        assertTrue(result.active());
        assertFalse(result.alreadyRegistered());

        ArgumentCaptor<UserDeviceToken> captor = ArgumentCaptor.forClass(UserDeviceToken.class);
        verify(userDeviceTokenRepository).save(captor.capture());
        assertEquals(USER_ID, captor.getValue().userId());
        assertEquals("fcm-token-1", captor.getValue().deviceToken());
        assertTrue(captor.getValue().active());
    }

    @Test
    void execute_reassignsTokenFromAnotherUserAndReactivates() {
        when(userDeviceTokenRepository.findByDeviceToken("shared-token"))
                .thenReturn(Optional.of(new UserDeviceToken(
                        TOKEN_ID,
                        OTHER_USER_ID,
                        DeviceType.IOS,
                        "shared-token",
                        false,
                        CREATED_AT,
                        CREATED_AT,
                        CREATED_AT
                )));
        when(userDeviceTokenRepository.save(any(UserDeviceToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var result = useCase.execute(new RegisterDeviceTokenCommand(USER_ID, "ANDROID", "shared-token"));

        assertEquals(TOKEN_ID, result.id());
        assertEquals(USER_ID, result.userId());
        assertEquals(DeviceType.ANDROID, result.deviceType());
        assertTrue(result.active());
        assertFalse(result.alreadyRegistered());
        assertEquals(CREATED_AT, result.createdAt());
    }

    @Test
    void execute_marksAlreadyRegisteredForSameActiveToken() {
        when(userDeviceTokenRepository.findByDeviceToken("same-token"))
                .thenReturn(Optional.of(new UserDeviceToken(
                        TOKEN_ID,
                        USER_ID,
                        DeviceType.WEB,
                        "same-token",
                        true,
                        CREATED_AT,
                        CREATED_AT,
                        CREATED_AT
                )));
        when(userDeviceTokenRepository.save(any(UserDeviceToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var result = useCase.execute(new RegisterDeviceTokenCommand(USER_ID, "WEB", "same-token"));

        assertTrue(result.alreadyRegistered());
    }

    @Test
    void execute_throwsForInvalidDeviceType() {
        AppException ex = assertThrows(AppException.class, () ->
                useCase.execute(new RegisterDeviceTokenCommand(USER_ID, "WINDOWS", "token"))
        );

        assertEquals(ErrorCode.VALIDATION_ERROR, ex.getErrorCode());
        assertEquals("deviceType", ex.getField());
    }

    @Test
    void execute_throwsUnauthorizedWhenUserIdMissing() {
        AppException ex = assertThrows(AppException.class, () ->
                useCase.execute(new RegisterDeviceTokenCommand(null, "IOS", "token"))
        );

        assertEquals(ErrorCode.UNAUTHORIZED, ex.getErrorCode());
    }
}
