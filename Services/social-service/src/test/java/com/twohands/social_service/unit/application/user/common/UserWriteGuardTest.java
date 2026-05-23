package com.twohands.social_service.unit.application.user.common;

import com.twohands.social_service.application.user.common.UserWriteGuard;
import com.twohands.social_service.domain.user.UserProjection;
import com.twohands.social_service.domain.user.UserProjectionRepository;
import com.twohands.social_service.exception.AppException;
import com.twohands.social_service.exception.ErrorCode;
import com.twohands.social_service.testsupport.UserProjectionTestFixtures;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UserWriteGuardTest {

    private final UserProjectionRepository userProjectionRepository = mock(UserProjectionRepository.class);
    private final UserWriteGuard guard = new UserWriteGuard(userProjectionRepository);

    @Test
    void shouldAllowWriteWhenProjectionIsActive() {
        UUID userId = UUID.randomUUID();
        when(userProjectionRepository.findByUserId(userId)).thenReturn(UserProjectionTestFixtures.activeOptional(userId));

        assertThatCode(() -> guard.assertCanWrite(userId)).doesNotThrowAnyException();
        assertThat(guard.canWrite(userId)).isTrue();
    }

    @Test
    void shouldRejectWriteWhenProjectionMissing() {
        UUID userId = UUID.randomUUID();
        when(userProjectionRepository.findByUserId(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> guard.assertCanWrite(userId))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode()).isEqualTo(ErrorCode.FORBIDDEN));
        assertThat(guard.canWrite(userId)).isFalse();
    }

    @Test
    void shouldRejectWriteWhenUserSuspended() {
        UUID userId = UUID.randomUUID();
        when(userProjectionRepository.findByUserId(userId)).thenReturn(Optional.of(
                new UserProjection(userId.toString(), "SUSPENDED", "User", null, false)
        ));

        assertThatThrownBy(() -> guard.assertCanWrite(userId))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode()).isEqualTo(ErrorCode.ACCOUNT_SUSPENDED));
    }

    @Test
    void shouldRejectWriteWhenUserDeleted() {
        UUID userId = UUID.randomUUID();
        when(userProjectionRepository.findByUserId(userId)).thenReturn(Optional.of(
                new UserProjection(userId.toString(), "DELETED", "User", null, false)
        ));

        assertThatThrownBy(() -> guard.assertCanWrite(userId))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode()).isEqualTo(ErrorCode.FORBIDDEN));
    }

    @Test
    void shouldBypassStatusCheckForModeratorRole() {
        UUID userId = UUID.randomUUID();

        assertThatCode(() -> guard.assertCanWrite(userId, List.of("MODERATOR"))).doesNotThrowAnyException();
    }

    @Test
    void shouldThrowUnauthorizedWhenActorMissing() {
        assertThatThrownBy(() -> guard.assertCanWrite(null))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode()).isEqualTo(ErrorCode.UNAUTHORIZED));
    }
}
