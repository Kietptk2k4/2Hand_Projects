package com.twohands.social_service.infrastructure.persistence.adapter;

import com.twohands.social_service.domain.comment.Comment;
import com.twohands.social_service.domain.comment.CommentMediaItem;
import com.twohands.social_service.domain.comment.CommentRepository;
import com.twohands.social_service.domain.comment.CommentStatus;
import com.twohands.social_service.infrastructure.persistence.mongo.document.CommentDocument;
import com.twohands.social_service.infrastructure.persistence.mongo.repository.MongoCommentRepository;
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
        doc.setLikeCount(comment.likeCount());
        doc.setCreatedAt(comment.createdAt());
        doc.setUpdatedAt(comment.updatedAt());
        doc.setDeletedAt(comment.deletedAt());
        return doc;
    }

    private Comment toDomain(CommentDocument document) {
        List<CommentMediaItem> media = document.getMedia() != null
                ? document.getMedia().stream()
                        .map(m -> new CommentMediaItem(m.getUrl(), m.getType()))
                        .toList()
                : List.of();
        return new Comment(
                document.getId(),
                document.getPostId(),
                document.getAuthorId(),
                document.getParentCommentId(),
                document.getContentText(),
                media,
                CommentStatus.valueOf(document.getStatus()),
                document.getLikeCount(),
                document.getCreatedAt(),
                document.getUpdatedAt(),
                document.getDeletedAt()
        );
    }
}
