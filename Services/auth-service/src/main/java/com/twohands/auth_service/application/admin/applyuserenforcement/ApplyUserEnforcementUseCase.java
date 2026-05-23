package com.twohands.auth_service.application.admin.applyuserenforcement;

import com.twohands.auth_service.domain.enforcement.UserEnforcementActionType;
import com.twohands.auth_service.domain.enforcement.UserEnforcementSnapshot;
import com.twohands.auth_service.domain.enforcement.UserEnforcementSnapshotRepository;
import com.twohands.auth_service.domain.enforcement.UserEnforcementSnapshotStatus;
import com.twohands.auth_service.domain.session.RefreshTokenSessionRepository;
import com.twohands.auth_service.domain.user.User;
import com.twohands.auth_service.domain.user.UserRepository;
import com.twohands.auth_service.domain.user.UserStatus;
import com.twohands.auth_service.exception.AppException;
import com.twohands.auth_service.exception.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class ApplyUserEnforcementUseCase {

    private static final Logger log = LoggerFactory.getLogger(ApplyUserEnforcementUseCase.class);

    private final UserRepository userRepository;
    private final RefreshTokenSessionRepository refreshTokenSessionRepository;
    private final UserEnforcementSnapshotRepository enforcementSnapshotRepository;

    public ApplyUserEnforcementUseCase(
            UserRepository userRepository,
            RefreshTokenSessionRepository refreshTokenSessionRepository,
            UserEnforcementSnapshotRepository enforcementSnapshotRepository
    ) {
        this.userRepository = userRepository;
        this.refreshTokenSessionRepository = refreshTokenSessionRepository;
        this.enforcementSnapshotRepository = enforcementSnapshotRepository;
    }

    @Transactional
    public ApplyUserEnforcementResult execute(ApplyUserEnforcementCommand command) {
        validateCommand(command);

        Optional<ApplyUserEnforcementResult> eventReplay = resolveEventIdempotentReplay(command);
        if (eventReplay.isPresent()) {
            return eventReplay.get();
        }

        Optional<ApplyUserEnforcementResult> enforcementReplay = resolveEnforcementIdempotentReplay(command);
        if (enforcementReplay.isPresent()) {
            return enforcementReplay.get();
        }

        User user = loadUser(command);
        Instant now = Instant.now();

        return switch (command.actionType()) {
            case SUSPEND, BAN -> applySuspendOrBan(command, user, now);
            case RESTRICT -> applyRestrict(command, user, now);
            case REVOKE -> applyRevoke(command, user, now, UserEnforcementSnapshotStatus.REVOKED);
            case EXPIRE -> applyRevoke(command, user, now, UserEnforcementSnapshotStatus.EXPIRED);
        };
    }

    private void validateCommand(ApplyUserEnforcementCommand command) {
        if (command.enforcementId() == null) {
            throw new AppException(ErrorCode.BAD_REQUEST, "enforcement_id is required");
        }
        if (command.userId() == null) {
            throw new AppException(ErrorCode.BAD_REQUEST, "user_id is required");
        }
        if (command.actionType() == null) {
            throw new AppException(ErrorCode.BAD_REQUEST, "action_type is required");
        }
        if (command.actionType().blocksLogin() || command.actionType() == UserEnforcementActionType.RESTRICT) {
            if (command.reasonCode() == null || command.reasonCode().isBlank()) {
                throw new AppException(ErrorCode.BAD_REQUEST, "reason_code is required");
            }
            if (command.description() == null || command.description().isBlank()) {
                throw new AppException(ErrorCode.BAD_REQUEST, "description is required");
            }
            if (command.expiresAt() != null && !command.expiresAt().isAfter(Instant.now())) {
                throw new AppException(ErrorCode.BAD_REQUEST, "expires_at must be in the future");
            }
        }
    }

    private Optional<ApplyUserEnforcementResult> resolveEventIdempotentReplay(ApplyUserEnforcementCommand command) {
        if (command.eventId() == null) {
            return Optional.empty();
        }
        return enforcementSnapshotRepository.findByEventId(command.eventId())
                .map(snapshot -> buildReplayResult(command.userId(), snapshot, true));
    }

    private Optional<ApplyUserEnforcementResult> resolveEnforcementIdempotentReplay(ApplyUserEnforcementCommand command) {
        if (command.actionType() == UserEnforcementActionType.REVOKE
                || command.actionType() == UserEnforcementActionType.EXPIRE) {
            return Optional.empty();
        }

        return enforcementSnapshotRepository.findByEnforcementId(command.enforcementId())
                .filter(snapshot -> snapshot.status() == UserEnforcementSnapshotStatus.APPLIED)
                .map(snapshot -> {
                    int revokedSessionCount = 0;
                    if (command.actionType().blocksLogin()) {
                        revokedSessionCount = refreshTokenSessionRepository.revokeAllByUserId(command.userId());
                    }
                    return new ApplyUserEnforcementResult(
                            command.userId(),
                            resolveStatusName(command.userId()),
                            revokedSessionCount,
                            true,
                            false
                    );
                });
    }

    private User loadUser(ApplyUserEnforcementCommand command) {
        Optional<User> userOptional = userRepository.findById(command.userId());
        if (userOptional.isEmpty()) {
            if (command.failOnMissingUser()) {
                throw new AppException(ErrorCode.RESOURCE_NOT_FOUND, ErrorCode.RESOURCE_NOT_FOUND.defaultMessage());
            }
            log.warn("Skip enforcement apply because user not found. userId={}, enforcementId={}",
                    command.userId(), command.enforcementId());
            return null;
        }

        User user = userOptional.get();
        if (user.status() == UserStatus.DELETED) {
            if (command.failOnMissingUser()) {
                throw new AppException(ErrorCode.RESOURCE_NOT_FOUND, ErrorCode.RESOURCE_NOT_FOUND.defaultMessage());
            }
            log.warn("Skip enforcement apply because user is deleted. userId={}, enforcementId={}",
                    command.userId(), command.enforcementId());
            return null;
        }
        return user;
    }

    private ApplyUserEnforcementResult applySuspendOrBan(
            ApplyUserEnforcementCommand command,
            User user,
            Instant now
    ) {
        if (user == null) {
            return skippedResult(command.userId());
        }

        if (user.status() != UserStatus.SUSPENDED) {
            user.suspend(now);
            userRepository.updateStatus(user.id(), user.status(), user.updatedAt());
        }

        int revokedSessionCount = refreshTokenSessionRepository.revokeAllByUserId(user.id());
        saveAppliedSnapshot(command, now);

        return new ApplyUserEnforcementResult(
                user.id(),
                UserStatus.SUSPENDED.name(),
                revokedSessionCount,
                false,
                false
        );
    }

    private ApplyUserEnforcementResult applyRestrict(ApplyUserEnforcementCommand command, User user, Instant now) {
        if (user == null) {
            return skippedResult(command.userId());
        }

        saveAppliedSnapshot(command, now);

        return new ApplyUserEnforcementResult(
                user.id(),
                user.status().name(),
                0,
                false,
                false
        );
    }

    private ApplyUserEnforcementResult applyRevoke(
            ApplyUserEnforcementCommand command,
            User user,
            Instant now,
            UserEnforcementSnapshotStatus targetStatus
    ) {
        if (user == null) {
            return skippedResult(command.userId());
        }

        enforcementSnapshotRepository.findByEnforcementId(command.enforcementId())
                .ifPresentOrElse(
                        snapshot -> enforcementSnapshotRepository.markStatus(
                                snapshot.enforcementId(),
                                targetStatus,
                                now
                        ),
                        () -> enforcementSnapshotRepository.save(new UserEnforcementSnapshot(
                                command.enforcementId(),
                                command.userId(),
                                resolveSnapshotActionType(command),
                                targetStatus,
                                command.reasonCode(),
                                command.description(),
                                command.expiresAt(),
                                command.eventId(),
                                now,
                                now,
                                now
                        ))
                );

        boolean reactivated = false;
        if (command.reactivateUser()
                && !enforcementSnapshotRepository.existsAppliedBlockingEnforcement(user.id())
                && user.status() == UserStatus.SUSPENDED) {
            user.reactivate(now);
            userRepository.updateStatus(user.id(), user.status(), user.updatedAt());
            reactivated = true;
        }

        return new ApplyUserEnforcementResult(
                user.id(),
                user.status().name(),
                0,
                false,
                reactivated
        );
    }

    private UserEnforcementActionType resolveSnapshotActionType(ApplyUserEnforcementCommand command) {
        if (command.reasonCode() != null && command.reasonCode().equalsIgnoreCase("RESTRICT")) {
            return UserEnforcementActionType.RESTRICT;
        }
        return UserEnforcementActionType.SUSPEND;
    }

    private void saveAppliedSnapshot(ApplyUserEnforcementCommand command, Instant now) {
        enforcementSnapshotRepository.save(new UserEnforcementSnapshot(
                command.enforcementId(),
                command.userId(),
                command.actionType(),
                UserEnforcementSnapshotStatus.APPLIED,
                command.reasonCode(),
                command.description(),
                command.expiresAt(),
                command.eventId(),
                now,
                now,
                now
        ));
    }

    private ApplyUserEnforcementResult buildReplayResult(
            UUID userId,
            UserEnforcementSnapshot snapshot,
            boolean idempotentReplay
    ) {
        return new ApplyUserEnforcementResult(
                userId,
                resolveStatusName(userId),
                snapshot.actionType().blocksLogin()
                        ? refreshTokenSessionRepository.revokeAllByUserId(userId)
                        : 0,
                idempotentReplay,
                false
        );
    }

    private String resolveStatusName(UUID userId) {
        return userRepository.findById(userId)
                .map(user -> user.status().name())
                .orElse(UserStatus.ACTIVE.name());
    }

    private ApplyUserEnforcementResult skippedResult(UUID userId) {
        return new ApplyUserEnforcementResult(userId, UserStatus.ACTIVE.name(), 0, true, false);
    }
}
