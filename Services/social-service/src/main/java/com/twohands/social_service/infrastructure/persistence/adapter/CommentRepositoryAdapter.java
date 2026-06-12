package com.twohands.social_service.infrastructure.persistence.adapter;

import com.twohands.social_service.domain.comment.Comment;
import com.twohands.social_service.domain.comment.CommentListQuery;
import com.twohands.social_service.domain.comment.CommentMediaItem;
import com.twohands.social_service.domain.comment.CommentRepository;
import com.twohands.social_service.domain.comment.CommentSortOrder;
import com.twohands.social_service.domain.comment.CommentModerationStatus;
import com.twohands.social_service.domain.comment.CommentStatus;
import com.twohands.social_service.domain.post.PageResult;
import com.twohands.social_service.infrastructure.persistence.mongo.document.CommentDocument;
import com.twohands.social_service.infrastructure.persistence.mongo.repository.MongoCommentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class CommentRepositoryAdapter implements CommentRepository {

    private final MongoCommentRepository mongoCommentRepository;

    public CommentRepositoryAdapter(MongoCommentRepository mongoCommentRepository) {
        this.mongoCommentRepository = mongoCommentRepository;
    }

    @Override
    public Comment save(Comment comment) {
        CommentDocument saved = mongoCommentRepository.save(toDocument(comment));
        return toDomain(saved);
    }

    @Override
    public Optional<Comment> findById(String commentId) {
        return mongoCommentRepository.findById(commentId).map(this::toDomain);
    }

    @Override
    public Optional<Comment> findActiveByIdAndPostId(String commentId, String postId) {
        return mongoCommentRepository.findByIdAndPostIdAndStatus(
                commentId,
                postId,
                CommentStatus.ACTIVE.name()
        ).map(this::toDomain);
    }

    @Override
    public PageResult<Comment> findActiveByPost(CommentListQuery query) {
        Pageable pageable = PageRequest.of(
                query.page(),
                query.size(),
                toSort(query.sort())
        );
        Page<CommentDocument> page = query.parentCommentId() == null
                ? mongoCommentRepository.findByPostIdAndStatusAndParentCommentIdIsNull(
                        query.postId(),
                        CommentStatus.ACTIVE.name(),
                        pageable
                )
                : mongoCommentRepository.findByPostIdAndStatusAndParentCommentId(
                        query.postId(),
                        CommentStatus.ACTIVE.name(),
                        query.parentCommentId(),
                        pageable
                );

        List<Comment> items = page.getContent().stream()
                .map(this::toDomain)
                .toList();

        return new PageResult<>(
                items,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.hasNext()
        );
    }

    @Override
    public long countActiveReplies(String postId, String parentCommentId) {
        return mongoCommentRepository.countByPostIdAndStatusAndParentCommentId(
                postId,
                CommentStatus.ACTIVE.name(),
                parentCommentId
        );
    }

    @Override
    public void incrementLikeCount(String commentId) {
        mongoCommentRepository.findById(commentId).ifPresent(document -> {
            document.setLikeCount(document.getLikeCount() + 1);
            mongoCommentRepository.save(document);
        });
    }

    @Override
    public void decrementLikeCount(String commentId) {
        mongoCommentRepository.findById(commentId).ifPresent(document -> {
            document.setLikeCount(Math.max(0, document.getLikeCount() - 1));
            mongoCommentRepository.save(document);
        });
    }

    private CommentDocument toDocument(Comment comment) {
        CommentDocument doc = new CommentDocument();
        if (comment.id() != null) {
            doc.setId(comment.id());
        }
        doc.setPostId(comment.postId());
        doc.setAuthorId(comment.authorId());
        doc.setParentCommentId(comment.parentCommentId());
        doc.setContentText(comment.contentText());
        doc.setMedia(comment.media().stream()
                .map(m -> new CommentDocument.MediaDocument(m.url(), m.type()))
                .toList());
        doc.setStatus(comment.status().name());
        doc.setModerationStatus(comment.moderationStatusOrDefault().name());
        doc.setModerationReason(comment.moderationReason());
        doc.setLastModerationLogId(comment.lastModerationLogId());
        doc.setLikeCount(comment.likeCount());
        doc.setCreatedAt(comment.createdAt());
        doc.setUpdatedAt(comment.updatedAt());
        doc.setDeletedAt(comment.deletedAt());
        return doc;
    }

    private Sort toSort(CommentSortOrder sort) {
        if (sort == CommentSortOrder.CREATED_AT_DESC) {
            return Sort.by(Sort.Direction.DESC, "createdAt");
        }
        return Sort.by(Sort.Direction.ASC, "createdAt");
    }

    private Comment toDomain(CommentDocument document) {
        List<CommentMediaItem> media = document.getMedia() != null
                ? document.getMedia().stream()
                        .map(m -> new CommentMediaItem(m.getUrl(), m.getType()))
                        .toList()
                : List.of();
        CommentModerationStatus moderationStatus = parseModerationStatus(document.getModerationStatus());
        return new Comment(
                document.getId(),
                document.getPostId(),
                document.getAuthorId(),
                document.getParentCommentId(),
                document.getContentText(),
                media,
                CommentStatus.valueOf(document.getStatus()),
                moderationStatus,
                document.getModerationReason(),
                document.getLastModerationLogId(),
                document.getLikeCount(),
                document.getCreatedAt(),
                document.getUpdatedAt(),
                document.getDeletedAt()
        );
    }

    private CommentModerationStatus parseModerationStatus(String rawStatus) {
        if (rawStatus == null || rawStatus.isBlank()) {
            return CommentModerationStatus.NONE;
        }
        try {
            return CommentModerationStatus.valueOf(rawStatus);
        } catch (IllegalArgumentException ex) {
            return CommentModerationStatus.NONE;
        }
    }
}
