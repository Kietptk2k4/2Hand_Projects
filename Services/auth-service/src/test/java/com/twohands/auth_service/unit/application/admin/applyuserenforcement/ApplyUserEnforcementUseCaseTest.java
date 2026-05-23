package com.twohands.auth_service.unit.application.admin.applyuserenforcement;

import com.twohands.auth_service.application.admin.applyuserenforcement.ApplyUserEnforcementCommand;
import com.twohands.auth_service.application.admin.applyuserenforcement.ApplyUserEnforcementUseCase;
import com.twohands.auth_service.domain.enforcement.UserEnforcementActionType;
import com.twohands.auth_service.domain.enforcement.UserEnforcementSnapshot;
import com.twohands.auth_service.domain.enforcement.UserEnforcementSnapshotRepository;
import com.twohands.auth_service.domain.enforcement.UserEnforcementSnapshotStatus;
import com.twohands.auth_service.domain.session.RefreshTokenSessionRepository;
import com.twohands.auth_service.domain.user.EmailAddress;
import com.twohands.auth_service.domain.user.PasswordHash;
import com.twohands.auth_service.domain.user.User;
import com.twohands.auth_service.domain.user.UserRepository;
import com.twohands.auth_service.domain.user.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ApplyUserEnforcementUseCaseTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final RefreshTokenSessionRepository refreshTokenSessionRepository = mock(RefreshTokenSessionRepository.class);
    private final UserEnforcementSnapshotRepository enforcementSnapshotRepository =
            mock(UserEnforcementSnapshotRepository.class);

    private ApplyUserEnforcementUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new ApplyUserEnforcementUseCase(
                userRepository,
                refreshTokenSessionRepository,
                enforcementSnapshotRepository
        );
    }

    @Test
    void shouldSuspendUserAndStoreSnapshot() {
        UUID userId = UUID.randomUUID();
        UUID enforcementId = UUID.randomUUID();
        User user = activeUser(userId);

        when(enforcementSnapshotRepository.findByEnforcementId(enforcementId)).thenReturn(Optional.empty());
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(refreshTokenSessionRepository.revokeAllByUserId(userId)).thenReturn(2);

        var result = useCase.execute(ApplyUserEnforcementCommand.forSyncApply(
                enforcementId,
                userId,
                UserEnforcementActionType.SUSPEND,
                "ABUSE",
                "Spam",
                null
        ));

        assertThat(result.status()).isEqualTo("SUSPENDED");
        assertThat(result.revokedSessionCount()).isEqualTo(2);
        assertThat(result.idempotentReplay()).isFalse();
        verify(userRepository).updateStatus(eq(userId), eq(UserStatus.SUSPENDED), any(Instant.class));
        verify(enforcementSnapshotRepository).save(any(UserEnforcementSnapshot.class));
    }

    @Test
    void shouldReplaySuspendIdempotentlyAndRevokeSessionsAgain() {
        UUID userId = UUID.randomUUID();
        UUID enforcementId = UUID.randomUUID();
        Instant now = Instant.now();
        UserEnforcementSnapshot snapshot = new UserEnforcementSnapshot(
                enforcementId,
                userId,
                UserEnforcementActionType.SUSPEND,
                UserEnforcementSnapshotStatus.APPLIED,
                "ABUSE",
                "Spam",
                null,
                null,
                now,
                now,
                now
        );

        when(enforcementSnapshotRepository.findByEnforcementId(enforcementId)).thenReturn(Optional.of(snapshot));
        when(userRepository.findById(userId)).thenReturn(Optional.of(activeUser(userId)));
        when(refreshTokenSessionRepository.revokeAllByUserId(userId)).thenReturn(1);

        var result = useCase.execute(ApplyUserEnforcementCommand.forSyncApply(
                enforcementId,
                userId,
                UserEnforcementActionType.SUSPEND,
                "ABUSE",
                "Spam",
                null
        ));

        assertThat(result.idempotentReplay()).isTrue();
        assertThat(result.revokedSessionCount()).isEqualTo(1);
        verify(userRepository, never()).updateStatus(any(), any(), any());
        verify(enforcementSnapshotRepository, never()).save(any());
    }

    @Test
    void shouldReactivateUserWhenNoBlockingEnforcementRemains() {
        UUID userId = UUID.randomUUID();
        UUID enforcementId = UUID.randomUUID();
        User user = suspendedUser(userId);

        when(enforcementSnapshotRepository.findByEnforcementId(enforcementId)).thenReturn(Optional.empty());
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(enforcementSnapshotRepository.existsAppliedBlockingEnforcement(userId)).thenReturn(false);

        var result = useCase.execute(ApplyUserEnforcementCommand.forSyncRevoke(
                enforcementId,
                userId,
                UserEnforcementActionType.REVOKE,
                "APPEAL_APPROVED",
                "note",
                true
        ));

        assertThat(result.reactivated()).isTrue();
        assertThat(result.status()).isEqualTo("ACTIVE");
        verify(userRepository).updateStatus(eq(userId), eq(UserStatus.ACTIVE), any(Instant.class));
    }

    private User activeUser(UUID userId) {
        Instant now = Instant.now();
        return User.rehydrate(
                userId,
                EmailAddress.of("user@example.com"),
                PasswordHash.of("hash"),
                UserStatus.ACTIVE,
                true,
                false,
                null,
                null,
                null,
                now,
                now
        );
    }

    private User suspendedUser(UUID userId) {
        Instant now = Instant.now();
        return User.rehydrate(
                userId,
                EmailAddress.of("user@example.com"),
                PasswordHash.of("hash"),
                UserStatus.SUSPENDED,
                true,
                false,
                null,
                null,
                null,
                now,
                now
        );
    }
}
