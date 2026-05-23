package com.twohands.social_service.infrastructure.persistence.mongo.repository;

import com.twohands.social_service.infrastructure.persistence.mongo.document.PostDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.Collection;
import java.util.List;

public interface MongoPostRepository extends MongoRepository<PostDocument, String> {
    List<PostDocument> findByIdIn(Collection<String> ids);

    @Query("""
            {
              'status': ?0,
              'visibility': ?1,
              '$or': [
                { 'moderation_status': { '$exists': false } },
                { 'moderation_status': 'NONE' }
              ]
            }
            """)
    Page<PostDocument> findByStatusAndVisibilityOrderByCreatedAtDesc(String status, String visibility, Pageable pageable);

    @Query("""
            {
              'status': ?0,
              'author_id': { '$in': ?1 },
              'visibility': { '$in': ?2 },
              '$or': [
                { 'moderation_status': { '$exists': false } },
                { 'moderation_status': 'NONE' }
              ]
            }
            """)
    Page<PostDocument> findByStatusAndAuthorIdInAndVisibilityInOrderByCreatedAtDesc(
            String status,
            Collection<String> authorIds,
            Collection<String> visibilities,
            Pageable pageable
    );

    @Query("""
            {
              'status': ?2,
              '$and': [
                {
                  '$or': [
                    { 'moderation_status': { '$exists': false } },
                    { 'moderation_status': 'NONE' }
                  ]
                },
                {
                  '$or': [
                    {
                      'visibility': ?3,
                      '$or': [
                        { 'caption': { '$regex': ?0, '$options': 'i' } },
                        { 'hashtags': { '$regex': ?0, '$options': 'i' } }
                      ]
                    },
                    {
                      'visibility': ?4,
                      'author_id': { '$in': ?1 },
                      '$or': [
                        { 'caption': { '$regex': ?0, '$options': 'i' } },
                        { 'hashtags': { '$regex': ?0, '$options': 'i' } }
                      ]
                    }
                  ]
                }
              ]
            }
            """)
    Page<PostDocument> searchActivePostsByKeyword(
            String keywordPattern,
            List<String> followeeAuthorIds,
            String status,
            String publicVisibility,
            String followersVisibility,
            Pageable pageable
    );

    @Query("""
            {
              'status': ?2,
              'hashtags': { '$in': ?0 },
              '$and': [
                {
                  '$or': [
                    { 'moderation_status': { '$exists': false } },
                    { 'moderation_status': 'NONE' }
                  ]
                },
                {
                  '$or': [
                    { 'visibility': ?3 },
                    { 'visibility': ?4, 'author_id': { '$in': ?1 } }
                  ]
                }
              ]
            }
            """)
    Page<PostDocument> searchActivePostsByHashtag(
            List<String> hashtagVariants,
            List<String> followeeAuthorIds,
            String status,
            String publicVisibility,
            String followersVisibility,
            Pageable pageable
    );

    Page<PostDocument> findByAuthorIdAndStatusOrderByCreatedAtDesc(
            String authorId,
            String status,
            Pageable pageable
    );

    Page<PostDocument> findByAuthorIdAndStatusInOrderByCreatedAtDesc(
            String authorId,
            Collection<String> statuses,
            Pageable pageable
    );

    @Query("""
            {
              'author_id': ?0,
              'status': ?1,
              'visibility': ?2,
              '$or': [
                { 'moderation_status': { '$exists': false } },
                { 'moderation_status': 'NONE' }
              ]
            }
            """)
    Page<PostDocument> findByAuthorIdAndStatusAndVisibilityOrderByCreatedAtDesc(
            String authorId,
            String status,
            String visibility,
            Pageable pageable
    );

    @Query("""
            {
              'author_id': ?0,
              'status': ?1,
              'visibility': { '$in': [?2, ?3] },
              '$or': [
                { 'moderation_status': { '$exists': false } },
                { 'moderation_status': 'NONE' }
              ]
            }
            """)
    Page<PostDocument> findByAuthorIdAndStatusAndVisibilityInOrderByCreatedAtDesc(
            String authorId,
            String status,
            String publicVisibility,
            String followersVisibility,
            Pageable pageable
    );
}
