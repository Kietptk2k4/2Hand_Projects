package com.twohands.social_service.infrastructure.persistence.mongo.repository;

import com.twohands.social_service.infrastructure.persistence.mongo.document.PostDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Collection;

public interface MongoPostRepository extends MongoRepository<PostDocument, String> {
    Page<PostDocument> findByStatusAndVisibilityOrderByCreatedAtDesc(String status, String visibility, Pageable pageable);

    Page<PostDocument> findByStatusAndAuthorIdInAndVisibilityInOrderByCreatedAtDesc(
            String status,
            Collection<String> authorIds,
            Collection<String> visibilities,
            Pageable pageable
    );
}
