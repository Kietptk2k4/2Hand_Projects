package com.twohands.social_service.infrastructure.persistence.mongo.repository;

import com.twohands.social_service.infrastructure.persistence.mongo.document.PostDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.Collection;
import java.util.List;

public interface MongoPostRepository extends MongoRepository<PostDocument, String> {
    Page<PostDocument> findByStatusAndVisibilityOrderByCreatedAtDesc(String status, String visibility, Pageable pageable);

    Page<PostDocument> findByStatusAndAuthorIdInAndVisibilityInOrderByCreatedAtDesc(
            String status,
            Collection<String> authorIds,
            Collection<String> visibilities,
            Pageable pageable
    );

    @Query("""
            {
              'status': ?2,
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
              '$or': [
                { 'visibility': ?3 },
                { 'visibility': ?4, 'author_id': { '$in': ?1 } }
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
}
