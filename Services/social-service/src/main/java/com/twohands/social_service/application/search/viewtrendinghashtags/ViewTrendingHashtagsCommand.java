package com.twohands.social_service.application.search.viewtrendinghashtags;

import java.util.UUID;

public record ViewTrendingHashtagsCommand(UUID userId, int limit) {
}