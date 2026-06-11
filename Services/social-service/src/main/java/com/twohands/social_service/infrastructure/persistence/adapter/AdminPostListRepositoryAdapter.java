package com.twohands.social_service.infrastructure.persistence.adapter;

import com.twohands.social_service.domain.admin.AdminModerationListSortField;
import com.twohands.social_service.domain.admin.AdminPostListCriteria;
import com.twohands.social_service.domain.admin.AdminPostListItem;
import com.twohands.social_service.domain.admin.AdminPostListRepository;
import com.twohands.social_service.domain.post.PageResult;
import com.twohands.social_service.infrastructure.persistence.mongo.document.PostDocument;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Repository
public class AdminPostListRepositoryAdapter implements AdminPostListRepository {

    private static final int CAPTION_PREVIEW_MAX_LENGTH = 120;
    private static final Pattern OBJECT_ID_PATTERN = Pattern.compile("^[0-9a-fA-F]{24}$");

    private final MongoTemplate mongoTemplate;

    public AdminPostListRepositoryAdapter(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public PageResult<AdminPostListItem> findPage(AdminPostListCriteria criteria) {
        Query query = new Query();
        List<Criteria> filters = new ArrayList<>();

        criteria.status().ifPresent(status -> filters.add(Criteria.where("status").is(status)));
        criteria.moderationStatus().ifPresent(status -> {
            if ("NONE".equals(status)) {
                filters.add(new Criteria().orOperator(
                        Criteria.where("moderation_status").exists(false),
                        Criteria.where("moderation_status").is(null),
                        Criteria.where("moderation_status").is("NONE")
                ));
            } else {
                filters.add(Criteria.where("moderation_status").is(status));
            }
        });
        criteria.query().ifPresent(fragment -> filters.add(buildSearchCriteria(fragment)));

        if (!filters.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(filters.toArray(Criteria[]::new)));
        }

        long totalElements = mongoTemplate.count(query, PostDocument.class);
        int totalPages = totalElements == 0 ? 0 : (int) ((totalElements + criteria.size() - 1) / criteria.size());
        boolean hasNext = criteria.page() < totalPages;

        query.with(PageRequest.of(criteria.page() - 1, criteria.size(), buildSort(criteria.sortField())));

        List<AdminPostListItem> items = mongoTemplate.find(query, PostDocument.class).stream()
                .map(this::toListItem)
                .toList();

        return new PageResult<>(
                items,
                criteria.page(),
                criteria.size(),
                totalElements,
                totalPages,
                hasNext
        );
    }

    private Criteria buildSearchCriteria(String fragment) {
        if (OBJECT_ID_PATTERN.matcher(fragment).matches()) {
            return new Criteria().orOperator(
                    Criteria.where("_id").is(fragment),
                    Criteria.where("author_id").regex(Pattern.quote(fragment), "i"),
                    Criteria.where("caption").regex(Pattern.quote(fragment), "i")
            );
        }
        return new Criteria().orOperator(
                Criteria.where("_id").regex(Pattern.quote(fragment), "i"),
                Criteria.where("author_id").regex(Pattern.quote(fragment), "i"),
                Criteria.where("caption").regex(Pattern.quote(fragment), "i")
        );
    }

    private Sort buildSort(AdminModerationListSortField sortField) {
        return switch (sortField) {
            case CREATED_AT -> Sort.by(Sort.Order.desc("created_at"), Sort.Order.desc("_id"));
            case UPDATED_AT -> Sort.by(Sort.Order.desc("updated_at"), Sort.Order.desc("_id"));
            case MODERATION_STATUS -> Sort.by(
                    Sort.Order.asc("moderation_status"),
                    Sort.Order.desc("updated_at"),
                    Sort.Order.desc("_id")
            );
            case LIKE_COUNT -> Sort.by(Sort.Order.desc("like_count"), Sort.Order.desc("_id"));
        };
    }

    private AdminPostListItem toListItem(PostDocument document) {
        String moderationStatus = document.getModerationStatus();
        if (moderationStatus == null || moderationStatus.isBlank()) {
            moderationStatus = "NONE";
        }
        return new AdminPostListItem(
                document.getId(),
                document.getAuthorId(),
                toPreview(document.getCaption()),
                document.getStatus(),
                moderationStatus,
                document.getLikeCount(),
                document.getCreatedAt(),
                document.getUpdatedAt()
        );
    }

    private String toPreview(String caption) {
        if (caption == null || caption.isBlank()) {
            return "";
        }
        String trimmed = caption.trim();
        if (trimmed.length() <= CAPTION_PREVIEW_MAX_LENGTH) {
            return trimmed;
        }
        return trimmed.substring(0, CAPTION_PREVIEW_MAX_LENGTH) + "...";
    }
}
