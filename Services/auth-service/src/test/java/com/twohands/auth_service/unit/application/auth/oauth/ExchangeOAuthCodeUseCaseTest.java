package com.twohands.auth_service.unit.application.auth.oauth;

import com.twohands.auth_service.application.auth.oauth.ExchangeOAuthCodeUseCase;
import com.twohands.auth_service.application.auth.oauth.OAuthExchangeCodePayload;
import com.twohands.auth_service.application.auth.oauth.OAuthExchangeCodeStore;
import com.twohands.auth_service.exception.AppException;
import com.twohands.auth_service.exception.ErrorCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExchangeOAuthCodeUseCaseTest {

    @Mock
    private OAuthExchangeCodeStore exchangeCodeStore;

    @InjectMocks
    private ExchangeOAuthCodeUseCase useCase;

    @Test
    void shouldReturnPayloadWhenCodeValid() {
        UUID userId = UUID.randomUUID();
        OAuthExchangeCodePayload payload = new OAuthExchangeCodePayload(
                "access-token",
                "refresh-token",
                900L,
                userId,
                "user@example.com",
                "ACTIVE"
        );
        when(exchangeCodeStore.consume("valid-code")).thenReturn(Optional.of(payload));

        OAuthExchangeCodePayload result = useCase.execute("valid-code");

        assertEquals("access-token", result.accessToken());
        verify(exchangeCodeStore).consume("valid-code");
    }

    @Test
    void shouldRejectWhenCodeMissing() {
        when(exchangeCodeStore.consume("missing")).thenReturn(Optional.empty());

        AppException ex = assertThrows(AppException.class, () -> useCase.execute("missing"));

        assertEquals(ErrorCode.OAUTH_SESSION_INVALID, ex.getErrorCode());
    }
}
