package com.twohands.social_service.infrastructure.persistence.mongo.repository;

import com.twohands.social_service.infrastructure.persistence.mongo.document.CommentDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface MongoCommentRepository extends MongoRepository<CommentDocument, String> {
}
