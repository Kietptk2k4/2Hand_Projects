package com.twohands.notification_service.unit.application.devicetoken;

import com.twohands.notification_service.application.devicetoken.ViewUserDeviceTokensCommand;
import com.twohands.notification_service.application.devicetoken.ViewUserDeviceTokensUseCase;
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
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ViewUserDeviceTokensUseCaseTest {

    private static final UUID USER_ID = UUID.randomUUID();

    @Mock
    private UserDeviceTokenRepository userDeviceTokenRepository;

    private ViewUserDeviceTokensUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new ViewUserDeviceTokensUseCase(userDeviceTokenRepository);
    }

    @Test
    void execute_returnsMaskedTokensForCurrentUser() {
        UUID tokenId = UUID.randomUUID();
        when(userDeviceTokenRepository.findByUserIdOrderByActiveDescUpdatedAtDesc(USER_ID))
                .thenReturn(List.of(new UserDeviceToken(
                        tokenId,
                        USER_ID,
                        DeviceType.IOS,
                        "secret-token-1234",
                        true,
                        Instant.parse("2026-05-24T12:00:00Z"),
                        Instant.parse("2026-05-24T12:00:00Z"),
                        Instant.parse("2026-05-20T08:00:00Z")
                )));

        var result = useCase.execute(new ViewUserDeviceTokensCommand(USER_ID));

        assertEquals(USER_ID, result.userId());
        assertEquals(1, result.items().size());
        assertEquals(tokenId, result.items().getFirst().id());
        assertEquals("****1234", result.items().getFirst().maskedDeviceToken());
    }

    @Test
    void execute_returnsEmptyListWhenUserHasNoTokens() {
        when(userDeviceTokenRepository.findByUserIdOrderByActiveDescUpdatedAtDesc(USER_ID))
                .thenReturn(List.of());

        var result = useCase.execute(new ViewUserDeviceTokensCommand(USER_ID));

        assertEquals(0, result.items().size());
    }

    @Test
    void execute_throwsUnauthorizedWhenUserIdMissing() {
        AppException ex = assertThrows(AppException.class, () ->
                useCase.execute(new ViewUserDeviceTokensCommand(null))
        );

        assertEquals(ErrorCode.UNAUTHORIZED, ex.getErrorCode());
    }
}
