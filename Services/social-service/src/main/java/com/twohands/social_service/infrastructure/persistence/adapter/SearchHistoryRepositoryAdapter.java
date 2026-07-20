package com.twohands.social_service.infrastructure.persistence.adapter;

import com.twohands.social_service.domain.search.SearchHistoryRepository;
import com.twohands.social_service.infrastructure.persistence.jpa.entity.SearchHistoryEntity;
import com.twohands.social_service.infrastructure.persistence.jpa.repository.JpaSearchHistoryRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.PageRequest;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Component
public class SearchHistoryRepositoryAdapter implements SearchHistoryRepository {

    private final JpaSearchHistoryRepository jpaSearchHistoryRepository;

    public SearchHistoryRepositoryAdapter(JpaSearchHistoryRepository jpaSearchHistoryRepository) {
        this.jpaSearchHistoryRepository = jpaSearchHistoryRepository;
    }

    @Override
    @Transactional
    public void saveOrRefresh(UUID userId, String keyword) {
        Instant now = Instant.now();
        jpaSearchHistoryRepository.findByUserIdAndKeywordIgnoreCase(userId, keyword)
                .ifPresentOrElse(existing -> {
                    existing.setUpdatedAt(now);
                    jpaSearchHistoryRepository.save(existing);
                }, () -> {
                    SearchHistoryEntity entity = new SearchHistoryEntity();
                    entity.setId(UUID.randomUUID());
                    entity.setUserId(userId);
                    entity.setKeyword(keyword);
                    entity.setCreatedAt(now);
                    entity.setUpdatedAt(now);
                    jpaSearchHistoryRepository.save(entity);
                });
    }

    @Override
    public List<String> findRecentKeywordsByUserId(UUID userId, int limit) {
        return jpaSearchHistoryRepository.findRecentKeywordsByUserId(userId, PageRequest.of(0, limit));
    }
}
