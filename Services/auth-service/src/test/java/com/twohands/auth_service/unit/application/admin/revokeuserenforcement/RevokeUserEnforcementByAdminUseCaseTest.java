package com.twohands.auth_service.unit.application.admin.revokeuserenforcement;

import com.twohands.auth_service.application.admin.revokeuserenforcement.RevokeUserEnforcementByAdminCommand;
import com.twohands.auth_service.application.admin.revokeuserenforcement.RevokeUserEnforcementByAdminUseCase;
import com.twohands.auth_service.domain.rbac.PermissionQueryRepository;
import com.twohands.auth_service.domain.user.EmailAddress;
import com.twohands.auth_service.domain.user.PasswordHash;
import com.twohands.auth_service.domain.user.User;
import com.twohands.auth_service.domain.user.UserRepository;
import com.twohands.auth_service.domain.user.UserStatus;
import com.twohands.auth_service.exception.AppException;
import com.twohands.auth_service.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RevokeUserEnforcementByAdminUseCaseTest {

    private final UserRepository userRepository = Mockito.mock(UserRepository.class);
    private final PermissionQueryRepository permissionQueryRepository = Mockito.mock(PermissionQueryRepository.class);

    private RevokeUserEnforcementByAdminUseCase useCase;

    @BeforeEach
    void setup() {
        useCase = new RevokeUserEnforcementByAdminUseCase(
                userRepository,
                permissionQueryRepository
        );
    }

    @Test
    void shouldReactivateSuspendedUserWhenRequested() {
        UUID actorId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        User user = suspendedUser(userId);

        when(permissionQueryRepository.findPermissionCodesByUserId(actorId)).thenReturn(Set.of("USER_ENFORCEMENT_REVOKE"));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        var result = useCase.execute(new RevokeUserEnforcementByAdminCommand(
                actorId,
                UUID.randomUUID(),
                userId,
                "SUSPEND",
                true,
                null,
                null
        ));

        assertEquals("ACTIVE", result.status());
        assertTrue(result.reactivated());
        verify(userRepository).updateStatus(eq(userId), eq(UserStatus.ACTIVE), any(Instant.class));
    }

    @Test
    void shouldNotReactivateWhenFlagFalse() {
        UUID actorId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(permissionQueryRepository.findPermissionCodesByUserId(actorId)).thenReturn(Set.of("USER_ENFORCEMENT_REVOKE"));
        when(userRepository.findById(userId)).thenReturn(Optional.of(suspendedUser(userId)));

        var result = useCase.execute(new RevokeUserEnforcementByAdminCommand(
                actorId, UUID.randomUUID(), userId, "RESTRICT", false, null, null
        ));

        assertEquals("SUSPENDED", result.status());
        assertFalse(result.reactivated());
        verify(userRepository, never()).updateStatus(any(), any(), any());
    }

    @Test
    void shouldRejectWhenActorLacksPermission() {
        UUID actorId = UUID.randomUUID();
        when(permissionQueryRepository.findPermissionCodesByUserId(actorId)).thenReturn(Set.of());
        when(permissionQueryRepository.findRoleCodesByUserId(actorId)).thenReturn(List.of("MODERATOR"));

        AppException ex = org.junit.jupiter.api.Assertions.assertThrows(AppException.class, () -> useCase.execute(
                new RevokeUserEnforcementByAdminCommand(actorId, UUID.randomUUID(), UUID.randomUUID(), "RESTRICT", false, null, null)
        ));
        assertEquals(ErrorCode.FORBIDDEN, ex.getErrorCode());
    }

    private User suspendedUser(UUID userId) {
        Instant now = Instant.now();
        return User.rehydrate(
                userId,
                EmailAddress.of("user@2hands.vn"),
                PasswordHash.of("$2a$10$abcdefghijklmnopqrstuv"),
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
