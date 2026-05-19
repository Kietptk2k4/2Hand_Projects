package com.twohands.social_service.domain.search;

import java.util.UUID;

public interface SearchHistoryRepository {
    void saveOrRefresh(UUID userId, String keyword);
}
