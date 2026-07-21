package com.twohands.social_service.application.admin.common;

import com.twohands.social_service.domain.user.UserProjection;
import com.twohands.social_service.domain.user.UserProjectionRepository;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class AdminModerationAuthorResolver {

    private final UserProjectionRepository userProjectionRepository;

    public AdminModerationAuthorResolver(UserProjectionRepository userProjectionRepository) {
        this.userProjectionRepository = userProjectionRepository;
    }

    public AuthorSummary resolveAuthor(String authorId) {
        if (authorId == null || authorId.isBlank()) {
            return new AuthorSummary(authorId, null, null);
        }

        UUID userId = parseUserId(authorId);
        if (userId == null) {
            return new AuthorSummary(authorId, null, null);
        }

        return userProjectionRepository.findByUserId(userId)
                .filter(projection -> !projection.isDeleted())
                .map(this::toSummary)
                .orElseGet(() -> new AuthorSummary(authorId, null, null));
    }

    public Map<String, AuthorSummary> resolveAuthors(Collection<String> authorIds) {
        Map<String, AuthorSummary> summaries = new HashMap<>();
        if (authorIds == null || authorIds.isEmpty()) {
            return summaries;
        }

        List<UUID> userIds = authorIds.stream()
                .map(this::parseUserId)
                .filter(java.util.Objects::nonNull)
                .distinct()
                .toList();

        if (userIds.isEmpty()) {
            return summaries;
        }

        for (UserProjection projection : userProjectionRepository.findByUserIds(userIds)) {
            if (projection == null || projection.isDeleted()) {
                continue;
            }
            summaries.put(projection.userId(), toSummary(projection));
        }

        return summaries;
    }

    private AuthorSummary toSummary(UserProjection projection) {
        return new AuthorSummary(
                projection.userId(),
                projection.displayName(),
                projection.avatarUrl()
        );
    }

    private UUID parseUserId(String authorId) {
        try {
            return UUID.fromString(authorId);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    public record AuthorSummary(
            String userId,
            String displayName,
            String avatarUrl
    ) {
    }
}
