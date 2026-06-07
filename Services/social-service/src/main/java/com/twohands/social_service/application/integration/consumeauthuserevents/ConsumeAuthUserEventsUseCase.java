package com.twohands.social_service.application.integration.consumeauthuserevents;

import com.twohands.social_service.domain.integration.ProcessedDomainEventRepository;
import com.twohands.social_service.domain.user.UserProjection;
import com.twohands.social_service.domain.user.UserProjectionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ConsumeAuthUserEventsUseCase {

    public static final String CONSUMER_NAME = "social-user-projection";

    private static final Logger log = LoggerFactory.getLogger(ConsumeAuthUserEventsUseCase.class);

    private final ProcessedDomainEventRepository processedDomainEventRepository;
    private final UserProjectionRepository userProjectionRepository;

    public ConsumeAuthUserEventsUseCase(
            ProcessedDomainEventRepository processedDomainEventRepository,
            UserProjectionRepository userProjectionRepository
    ) {
        this.processedDomainEventRepository = processedDomainEventRepository;
        this.userProjectionRepository = userProjectionRepository;
    }

    @Transactional
    public ConsumeAuthUserEventResult execute(ConsumeAuthUserEventCommand command) {
        requireEventId(command.eventId());
        requireUserId(command.userId());

        if (processedDomainEventRepository.existsByEventId(command.eventId())) {
            log.debug("Skip duplicate auth user event. eventId={}, eventType={}", command.eventId(), command.eventType());
            return new ConsumeAuthUserEventResult(
                    command.eventId(),
                    command.userId(),
                    resolveExistingStatus(command.userId()),
                    true
            );
        }

        String targetStatus = resolveTargetStatus(command);
        UserProjection merged = mergeProjection(command, targetStatus);
        UserProjection saved = userProjectionRepository.upsert(merged);

        processedDomainEventRepository.markProcessed(
                command.eventId(),
                CONSUMER_NAME,
                command.eventType().name()
        );

        log.info(
                "Applied auth user event to projection. eventId={}, eventType={}, userId={}, status={}",
                command.eventId(),
                command.eventType(),
                command.userId(),
                saved.status()
        );

        return new ConsumeAuthUserEventResult(
                command.eventId(),
                command.userId(),
                saved.status(),
                false
        );
    }

    private void requireEventId(UUID eventId) {
        if (eventId == null) {
            throw new InvalidAuthUserEventException("event_id is required");
        }
    }

    private void requireUserId(UUID userId) {
        if (userId == null) {
            throw new InvalidAuthUserEventException("user_id is required");
        }
    }

    private String resolveTargetStatus(ConsumeAuthUserEventCommand command) {
        return switch (command.eventType()) {
            case USER_CREATED -> firstNonBlank(command.status(), "ACTIVE");
            case USER_UPDATED -> command.status();
            case USER_DELETED -> "DELETED";
            case USER_SUSPENDED, USER_BANNED -> "SUSPENDED";
            case USER_ENFORCEMENT_REVOKED, USER_ENFORCEMENT_EXPIRED -> "ACTIVE";
            case USER_RESTRICTED -> command.status();
        };
    }

    private UserProjection mergeProjection(ConsumeAuthUserEventCommand command, String targetStatus) {
        UserProjection existing = userProjectionRepository.findByUserId(command.userId()).orElse(null);

        String status = targetStatus != null
                ? targetStatus
                : existing != null ? existing.status() : "ACTIVE";
        String displayName = resolveDisplayName(command, existing);
        String avatarUrl = command.avatarUrl() != null
                ? command.avatarUrl()
                : existing != null ? existing.avatarUrl() : null;
        Boolean isPrivate = command.isPrivate() != null
                ? command.isPrivate()
                : existing != null ? existing.isPrivate() : false;

        return new UserProjection(
                command.userId().toString(),
                status,
                displayName,
                avatarUrl,
                isPrivate
        );
    }

    private String resolveExistingStatus(UUID userId) {
        return userProjectionRepository.findByUserId(userId)
                .map(UserProjection::status)
                .orElse("ACTIVE");
    }

    private String resolveDisplayName(ConsumeAuthUserEventCommand command, UserProjection existing) {
        if (command.displayName() != null && !command.displayName().isBlank()) {
            return command.displayName();
        }
        String emailDisplayName = displayNameFromEmail(command.email());
        if (emailDisplayName != null) {
            return emailDisplayName;
        }
        if (existing != null
                && existing.displayName() != null
                && !existing.displayName().isBlank()
                && !isPlaceholderDisplayName(existing.displayName(), command.userId())) {
            return existing.displayName();
        }
        return defaultDisplayName(command.userId());
    }

    private boolean isPlaceholderDisplayName(String displayName, UUID userId) {
        if (displayName == null || displayName.isBlank() || userId == null) {
            return true;
        }
        String trimmed = displayName.trim();
        if (trimmed.equalsIgnoreCase(userId.toString())) {
            return true;
        }
        return trimmed.equalsIgnoreCase(defaultDisplayName(userId));
    }

    private String displayNameFromEmail(String email) {
        if (email == null || email.isBlank() || !email.contains("@")) {
            return null;
        }
        String localPart = email.substring(0, email.indexOf('@')).trim();
        if (localPart.isBlank()) {
            return null;
        }
        return localPart.length() > 100 ? localPart.substring(0, 100) : localPart;
    }

    private String defaultDisplayName(UUID userId) {
        return "User " + userId.toString().substring(0, 8);
    }

    private String firstNonBlank(String value, String fallback) {
        return value != null && !value.isBlank() ? value : fallback;
    }
}
