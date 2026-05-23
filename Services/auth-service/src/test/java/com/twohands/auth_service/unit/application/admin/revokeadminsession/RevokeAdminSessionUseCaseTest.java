package com.twohands.auth_service.unit.application.admin.revokeadminsession;

import com.twohands.auth_service.application.admin.revokeadminsession.RevokeAdminSessionCommand;
import com.twohands.auth_service.application.admin.revokeadminsession.RevokeAdminSessionUseCase;
import com.twohands.auth_service.domain.rbac.PermissionQueryRepository;
import com.twohands.auth_service.domain.session.RefreshTokenSession;
import com.twohands.auth_service.domain.session.RefreshTokenSessionRepository;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RevokeAdminSessionUseCaseTest {

    private final RefreshTokenSessionRepository refreshTokenSessionRepository = Mockito.mock(RefreshTokenSessionRepository.class);
    private final UserRepository userRepository = Mockito.mock(UserRepository.class);
    private final PermissionQueryRepository permissionQueryRepository = Mockito.mock(PermissionQueryRepository.class);

    private RevokeAdminSessionUseCase useCase;

    @BeforeEach
    void setup() {
        useCase = new RevokeAdminSessionUseCase(
                refreshTokenSessionRepository,
                userRepository,
                permissionQueryRepository
        );
    }

    @Test
    void shouldRevokeSingleActiveSession() {
        UUID actorId = UUID.randomUUID();
        UUID targetUserId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        RefreshTokenSession session = activeSession(sessionId, targetUserId);

        when(permissionQueryRepository.findPermissionCodesByUserId(actorId)).thenReturn(Set.of("ADMIN_SESSION_REVOKE"));
        when(refreshTokenSessionRepository.findById(sessionId)).thenReturn(Optional.of(session));
        when(userRepository.findById(targetUserId)).thenReturn(Optional.of(activeUser(targetUserId)));
        when(permissionQueryRepository.findRoleCodesByUserId(targetUserId)).thenReturn(List.of("ADMIN"));
        when(permissionQueryRepository.findPermissionCodesByUserId(targetUserId)).thenReturn(Set.of("ADMIN_ACCESS"));
        when(refreshTokenSessionRepository.markRevokedIfActive(eq(sessionId), any(Instant.class))).thenReturn(1);

        var result = useCase.execute(new RevokeAdminSessionCommand(actorId, sessionId, false));

        assertEquals(1, result.revokedSessionCount());
        assertEquals(targetUserId, result.targetAdminUserId());
    }

    @Test
    void shouldRevokeAllActiveSessionsWhenRequested() {
        UUID actorId = UUID.randomUUID();
        UUID targetUserId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        RefreshTokenSession session = activeSession(sessionId, targetUserId);

        when(permissionQueryRepository.findPermissionCodesByUserId(actorId)).thenReturn(Set.of("ADMIN_SESSION_REVOKE"));
        when(refreshTokenSessionRepository.findById(sessionId)).thenReturn(Optional.of(session));
        when(userRepository.findById(targetUserId)).thenReturn(Optional.of(activeUser(targetUserId)));
        when(permissionQueryRepository.findRoleCodesByUserId(targetUserId)).thenReturn(List.of("MODERATOR"));
        when(permissionQueryRepository.findPermissionCodesByUserId(targetUserId)).thenReturn(Set.of());
        when(refreshTokenSessionRepository.revokeAllByUserId(targetUserId)).thenReturn(3);

        var result = useCase.execute(new RevokeAdminSessionCommand(actorId, sessionId, true));

        assertEquals(3, result.revokedSessionCount());
        verify(refreshTokenSessionRepository, never()).markRevokedIfActive(any(), any());
    }

    @Test
    void shouldReturn404WhenSessionMissing() {
        UUID actorId = UUID.randomUUID();
        when(permissionQueryRepository.findPermissionCodesByUserId(actorId)).thenReturn(Set.of("ADMIN_SESSION_REVOKE"));
        when(refreshTokenSessionRepository.findById(any())).thenReturn(Optional.empty());

        AppException ex = assertThrows(AppException.class, () -> useCase.execute(
                new RevokeAdminSessionCommand(actorId, UUID.randomUUID(), false)
        ));
        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    void shouldRejectWhenTargetIsNotAdminUser() {
        UUID actorId = UUID.randomUUID();
        UUID targetUserId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        RefreshTokenSession session = activeSession(sessionId, targetUserId);

        when(permissionQueryRepository.findPermissionCodesByUserId(actorId)).thenReturn(Set.of("ADMIN_SESSION_REVOKE"));
        when(refreshTokenSessionRepository.findById(sessionId)).thenReturn(Optional.of(session));
        when(userRepository.findById(targetUserId)).thenReturn(Optional.of(activeUser(targetUserId)));
        when(permissionQueryRepository.findRoleCodesByUserId(targetUserId)).thenReturn(List.of());
        when(permissionQueryRepository.findPermissionCodesByUserId(targetUserId)).thenReturn(Set.of());

        AppException ex = assertThrows(AppException.class, () -> useCase.execute(
                new RevokeAdminSessionCommand(actorId, sessionId, false)
        ));
        assertEquals(ErrorCode.FORBIDDEN, ex.getErrorCode());
    }

    private RefreshTokenSession activeSession(UUID sessionId, UUID userId) {
        Instant now = Instant.now();
        return RefreshTokenSession.createActive(
                sessionId,
                userId,
                "hash",
                "device",
                "127.0.0.1",
                "JUnit",
                now.plusSeconds(3600),
                now
        );
    }

    private User activeUser(UUID userId) {
        Instant now = Instant.now();
        return User.rehydrate(
                userId,
                EmailAddress.of("admin@2hands.vn"),
                PasswordHash.of("$2a$10$abcdefghijklmnopqrstuv"),
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
}
