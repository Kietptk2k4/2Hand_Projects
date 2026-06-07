package com.twohands.social_service.infrastructure.persistence.mongo.repository;

import com.twohands.social_service.infrastructure.persistence.mongo.document.UserProjectionDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface MongoUserProjectionRepository extends MongoRepository<UserProjectionDocument, String> {
    Optional<UserProjectionDocument> findByUserId(String userId);

    List<UserProjectionDocument> findByUserIdIn(Collection<String> userIds);
}
