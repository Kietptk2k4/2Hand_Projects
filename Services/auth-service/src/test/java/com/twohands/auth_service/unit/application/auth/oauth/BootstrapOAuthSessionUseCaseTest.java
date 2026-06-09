package com.twohands.auth_service.unit.application.auth.oauth;

import com.twohands.auth_service.application.auth.oauth.BootstrapOAuthSessionCommand;
import com.twohands.auth_service.application.auth.oauth.BootstrapOAuthSessionResult;
import com.twohands.auth_service.application.auth.oauth.BootstrapOAuthSessionUseCase;
import com.twohands.auth_service.exception.AppException;
import com.twohands.auth_service.exception.ErrorCode;
import com.twohands.auth_service.security.jwt.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BootstrapOAuthSessionUseCaseTest {

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private BootstrapOAuthSessionUseCase useCase;

    @Test
    void shouldRejectWhenCookiesMissing() {
        AppException ex = assertThrows(AppException.class, () -> useCase.execute(
                new BootstrapOAuthSessionCommand(null, "refresh")
        ));

        assertEquals(ErrorCode.OAUTH_SESSION_INVALID, ex.getErrorCode());
    }

    @Test
    void shouldRejectWhenAccessTokenInvalid() {
        when(jwtTokenProvider.isValid("bad-access")).thenReturn(false);

        AppException ex = assertThrows(AppException.class, () -> useCase.execute(
                new BootstrapOAuthSessionCommand("bad-access", "refresh-token")
        ));

        assertEquals(ErrorCode.OAUTH_SESSION_INVALID, ex.getErrorCode());
    }

    @Test
    void shouldReturnSessionWhenCookiesValid() {
        UUID userId = UUID.randomUUID();
        when(jwtTokenProvider.isValid("access-token")).thenReturn(true);
        when(jwtTokenProvider.getSubject("access-token")).thenReturn(userId.toString());
        when(jwtTokenProvider.getEmail("access-token")).thenReturn("user@example.com");
        when(jwtTokenProvider.getStatus("access-token")).thenReturn("ACTIVE");
        when(jwtTokenProvider.getExpiresInSeconds("access-token")).thenReturn(900L);

        BootstrapOAuthSessionResult result = useCase.execute(
                new BootstrapOAuthSessionCommand("access-token", "refresh-token")
        );

        assertEquals("access-token", result.accessToken());
        assertEquals("refresh-token", result.refreshToken());
        assertEquals(userId, result.userId());
        assertEquals("user@example.com", result.email());
        assertEquals("ACTIVE", result.status());
        assertEquals(900L, result.expiresIn());
    }
}
