package com.twohands.social_service.application.user.common;

import com.twohands.social_service.domain.user.UserProjection;
import com.twohands.social_service.domain.user.UserProjectionRepository;
import com.twohands.social_service.exception.AppException;
import com.twohands.social_service.exception.ErrorCode;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class UserWriteGuard {

    private static final Set<String> MODERATION_ROLES = Set.of("MODERATOR", "ADMIN");

    private final UserProjectionRepository userProjectionRepository;

    public UserWriteGuard(UserProjectionRepository userProjectionRepository) {
        this.userProjectionRepository = userProjectionRepository;
    }

    public void assertCanWrite(UUID actorUserId) {
        requireActor(actorUserId);
        enforceProjectionStatus(actorUserId);
    }

    public void assertCanWrite(UUID actorUserId, Collection<String> actorRoles) {
        requireActor(actorUserId);
        if (hasModerationRole(actorRoles)) {
            return;
        }
        enforceProjectionStatus(actorUserId);
    }

    public boolean canWrite(UUID actorUserId) {
        try {
            assertCanWrite(actorUserId);
            return true;
        } catch (AppException ex) {
            if (ex.getErrorCode() == ErrorCode.UNAUTHORIZED) {
                throw ex;
            }
            return false;
        }
    }

    private void requireActor(UUID actorUserId) {
        if (actorUserId == null) {
            throw new AppException(ErrorCode.UNAUTHORIZED, "Authentication required");
        }
    }

    private void enforceProjectionStatus(UUID actorUserId) {
        UserProjection user = userProjectionRepository.findByUserId(actorUserId)
                .orElseThrow(() -> new AppException(
                        ErrorCode.FORBIDDEN,
                        ErrorCode.FORBIDDEN.defaultMessage()
                ));

        if (user.isSuspended()) {
            throw new AppException(
                    ErrorCode.ACCOUNT_SUSPENDED,
                    ErrorCode.ACCOUNT_SUSPENDED.defaultMessage()
            );
        }
        if (user.isDeleted()) {
            throw new AppException(
                    ErrorCode.FORBIDDEN,
                    ErrorCode.FORBIDDEN.defaultMessage()
            );
        }
    }

    private boolean hasModerationRole(Collection<String> actorRoles) {
        if (actorRoles == null || actorRoles.isEmpty()) {
            return false;
        }
        return actorRoles.stream()
                .map(role -> role.toUpperCase(Locale.ROOT))
                .anyMatch(MODERATION_ROLES::contains);
    }
}
