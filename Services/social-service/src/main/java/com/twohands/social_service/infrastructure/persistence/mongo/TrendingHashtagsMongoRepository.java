package com.twohands.social_service.infrastructure.persistence.mongo;

import com.twohands.social_service.domain.post.PostStatus;
import com.twohands.social_service.domain.post.PostVisibility;
import com.twohands.social_service.domain.trendinghashtag.TrendingHashtag;
import com.twohands.social_service.domain.trendinghashtag.TrendingHashtagsRepository;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

@Repository
public class TrendingHashtagsMongoRepository implements TrendingHashtagsRepository {

    private static final String COLLECTION = "posts";
    private static final Pattern HASHTAG_PATTERN = Pattern.compile("^[a-zA-Z0-9_]+$");

    private final MongoTemplate mongoTemplate;

    public TrendingHashtagsMongoRepository(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public List<TrendingHashtag> findTrendingHashtags(Instant createdAfter, int limit, int postCountWeight) {
        if (limit <= 0) {
            return List.of();
        }

        List<Document> pipeline = buildPipeline(createdAfter, limit, postCountWeight);
        List<TrendingHashtag> results = new ArrayList<>();

        for (Document document : mongoTemplate.getCollection(COLLECTION).aggregate(pipeline)) {
            TrendingHashtag hashtag = toTrendingHashtag(document);
            if (hashtag != null) {
                results.add(hashtag);
            }
        }

        return results;
    }

    private List<Document> buildPipeline(Instant createdAfter, int limit, int postCountWeight) {
        Document match = new Document("$match", new Document()
                .append("status", PostStatus.ACTIVE.name())
                .append("visibility", PostVisibility.PUBLIC.name())
                .append("created_at", new Document("$gte", Date.from(createdAfter)))
                .append("hashtags", new Document("$exists", true).append("$ne", List.of()))
                .append("$and", List.of(
                        new Document("$or", List.of(
                                new Document("deleted_at", null),
                                new Document("deleted_at", new Document("$exists", false))
                        )),
                        new Document("$or", List.of(
                                new Document("moderation_status", "NONE"),
                                new Document("moderation_status", new Document("$exists", false))
                        ))
                )));

        Document trimTag = new Document("$trim", new Document("input", "$hashtags"));
        Document stripHash = new Document("$cond", List.of(
                new Document("$eq", List.of(new Document("$substrCP", List.of(trimTag, 0, 1)), "#")),
                new Document("$substrCP", List.of(trimTag, 1, new Document("$subtract", List.of(
                        new Document("$strLenCP", trimTag),
                        1
                )))),
                trimTag
        ));

        Document addFields = new Document("$addFields", new Document()
                .append("display_tag", stripHash)
                .append("normalized_tag", new Document("$toLower", stripHash)));

        Document validTagMatch = new Document("$match", new Document("normalized_tag", new Document(
                "$regex",
                "^[a-zA-Z0-9_]+$"
        )));

        Document group = new Document("$group", new Document()
                .append("_id", "$normalized_tag")
                .append("tag", new Document("$first", "$display_tag"))
                .append("postCount", new Document("$sum", 1))
                .append("totalLikes", new Document("$sum", "$like_count"))
                .append("totalReplies", new Document("$sum", "$reply_count")));

        Document scoreFields = new Document("$addFields", new Document()
                .append("engagementCount", new Document("$add", List.of("$totalLikes", "$totalReplies")))
                .append("score", new Document("$add", List.of(
                        new Document("$multiply", List.of("$postCount", postCountWeight)),
                        "$totalLikes",
                        "$totalReplies"
                ))));

        Document sort = new Document("$sort", new Document()
                .append("score", -1)
                .append("postCount", -1)
                .append("engagementCount", -1)
                .append("tag", 1));

        Document limitStage = new Document("$limit", limit);

        return List.of(
                match,
                new Document("$unwind", "$hashtags"),
                addFields,
                validTagMatch,
                group,
                scoreFields,
                sort,
                limitStage
        );
    }

    private TrendingHashtag toTrendingHashtag(Document document) {
        String tag = document.getString("tag");
        if (tag == null || tag.isBlank() || !HASHTAG_PATTERN.matcher(tag).matches()) {
            return null;
        }

        long postCount = readLong(document, "postCount");
        long totalLikes = readLong(document, "totalLikes");
        long totalReplies = readLong(document, "totalReplies");
        long engagementCount = readLong(document, "engagementCount");
        long score = readLong(document, "score");

        return new TrendingHashtag(tag, postCount, totalLikes, totalReplies, engagementCount, score);
    }

    private long readLong(Document document, String field) {
        Object value = document.get(field);
        if (value instanceof Number number) {
            return number.longValue();
        }
        return 0L;
    }
}