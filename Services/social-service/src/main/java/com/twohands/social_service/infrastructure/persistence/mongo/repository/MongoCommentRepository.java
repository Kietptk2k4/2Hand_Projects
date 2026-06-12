package com.twohands.social_service.infrastructure.persistence.mongo.repository;

import com.twohands.social_service.infrastructure.persistence.mongo.document.CommentDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.Optional;

public interface MongoCommentRepository extends MongoRepository<CommentDocument, String> {

    @Query("""
            {
              'post_id': ?0,
              'status': ?1,
              'parent_comment_id': null,
              '$or': [
                { 'moderation_status': { '$exists': false } },
                { 'moderation_status': null },
                { 'moderation_status': 'NONE' }
              ]
            }
            """)
    Page<CommentDocument> findByPostIdAndStatusAndParentCommentIdIsNull(
            String postId,
            String status,
            Pageable pageable
    );

    @Query("""
            {
              'post_id': ?0,
              'status': ?1,
              'parent_comment_id': ?2,
              '$or': [
                { 'moderation_status': { '$exists': false } },
                { 'moderation_status': null },
                { 'moderation_status': 'NONE' }
              ]
            }
            """)
    Page<CommentDocument> findByPostIdAndStatusAndParentCommentId(
            String postId,
            String status,
            String parentCommentId,
            Pageable pageable
    );

    @Query("""
            {
              'post_id': ?0,
              'status': ?1,
              'parent_comment_id': ?2,
              '$or': [
                { 'moderation_status': { '$exists': false } },
                { 'moderation_status': null },
                { 'moderation_status': 'NONE' }
              ]
            }
            """)
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
