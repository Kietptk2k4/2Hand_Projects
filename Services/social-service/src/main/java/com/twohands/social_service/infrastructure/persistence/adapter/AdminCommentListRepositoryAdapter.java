package com.twohands.social_service.infrastructure.persistence.adapter;

import com.twohands.social_service.domain.admin.AdminCommentListCriteria;
import com.twohands.social_service.domain.admin.AdminCommentListItem;
import com.twohands.social_service.domain.admin.AdminCommentListRepository;
import com.twohands.social_service.domain.admin.AdminCommentListSortField;
import com.twohands.social_service.domain.post.PageResult;
import com.twohands.social_service.infrastructure.persistence.mongo.document.CommentDocument;
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
public class AdminCommentListRepositoryAdapter implements AdminCommentListRepository {

    private static final int CONTENT_PREVIEW_MAX_LENGTH = 120;
    private static final Pattern OBJECT_ID_PATTERN = Pattern.compile("^[0-9a-fA-F]{24}$");

    private final MongoTemplate mongoTemplate;

    public AdminCommentListRepositoryAdapter(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public PageResult<AdminCommentListItem> findPage(AdminCommentListCriteria criteria) {
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
        criteria.postId().ifPresent(postId -> filters.add(Criteria.where("post_id").is(postId)));
        criteria.query().ifPresent(fragment -> filters.add(buildSearchCriteria(fragment)));

        if (!filters.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(filters.toArray(Criteria[]::new)));
        }

        long totalElements = mongoTemplate.count(query, CommentDocument.class);
        int totalPages = totalElements == 0 ? 0 : (int) ((totalElements + criteria.size() - 1) / criteria.size());
        boolean hasNext = criteria.page() < totalPages;

        query.with(PageRequest.of(criteria.page() - 1, criteria.size(), buildSort(criteria.sortField())));

        List<AdminCommentListItem> items = mongoTemplate.find(query, CommentDocument.class).stream()
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
                    Criteria.where("post_id").is(fragment),
                    Criteria.where("author_id").regex(Pattern.quote(fragment), "i"),
                    Criteria.where("content_text").regex(Pattern.quote(fragment), "i")
            );
        }
        return new Criteria().orOperator(
                Criteria.where("_id").regex(Pattern.quote(fragment), "i"),
                Criteria.where("post_id").regex(Pattern.quote(fragment), "i"),
                Criteria.where("author_id").regex(Pattern.quote(fragment), "i"),
                Criteria.where("content_text").regex(Pattern.quote(fragment), "i")
        );
    }

    private Sort buildSort(AdminCommentListSortField sortField) {
        return switch (sortField) {
            case CREATED_AT -> Sort.by(Sort.Order.desc("created_at"), Sort.Order.desc("_id"));
            case UPDATED_AT -> Sort.by(Sort.Order.desc("updated_at"), Sort.Order.desc("_id"));
            case MODERATION_STATUS -> Sort.by(Sort.Order.desc("moderation_status"), Sort.Order.desc("_id"));
            case LIKE_COUNT -> Sort.by(Sort.Order.desc("like_count"), Sort.Order.desc("_id"));
        };
    }

    private AdminCommentListItem toListItem(CommentDocument document) {
        String moderationStatus = document.getModerationStatus();
        if (moderationStatus == null || moderationStatus.isBlank()) {
            moderationStatus = "NONE";
        }
        List<CommentDocument.MediaDocument> media = document.getMedia() == null ? List.of() : document.getMedia();

        return new AdminCommentListItem(
                document.getId(),
                document.getPostId(),
                document.getAuthorId(),
                null,
                null,
                document.getParentCommentId(),
                toPreview(document.getContentText()),
                document.getStatus(),
                moderationStatus,
                media.size(),
                document.getLikeCount(),
                document.getCreatedAt(),
                document.getUpdatedAt()
        );
    }

    private String toPreview(String content) {
        if (content == null || content.isBlank()) {
            return "";
        }
        String trimmed = content.trim();
        if (trimmed.length() <= CONTENT_PREVIEW_MAX_LENGTH) {
            return trimmed;
        }
        return trimmed.substring(0, CONTENT_PREVIEW_MAX_LENGTH) + "...";
    }
}
