package com.twohands.social_service.infrastructure.persistence.mongo.repository;

import com.twohands.social_service.infrastructure.persistence.mongo.document.CommentDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface MongoCommentRepository extends MongoRepository<CommentDocument, String> {

    Page<CommentDocument> findByPostIdAndStatusAndParentCommentIdIsNull(
            String postId,
            String status,
            Pageable pageable
    );

    Page<CommentDocument> findByPostIdAndStatusAndParentCommentId(
            String postId,
            String status,
            String parentCommentId,
            Pageable pageable
    );

    long countByPostIdAndStatusAndParentCommentId(
            String postId,
            String status,
            String parentCommentId
    );

    Optional<CommentDocument> findByIdAndPostIdAndStatus(
            String id,
            String postId,
            String status
    );
}
