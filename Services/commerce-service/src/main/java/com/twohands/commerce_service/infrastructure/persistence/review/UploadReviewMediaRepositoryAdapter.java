package com.twohands.commerce_service.infrastructure.persistence.review;

import com.twohands.commerce_service.domain.review.ReviewMediaInsertDraft;
import com.twohands.commerce_service.domain.review.ReviewMediaItem;
import com.twohands.commerce_service.domain.review.ReviewMediaType;
import com.twohands.commerce_service.domain.review.ReviewStatus;
import com.twohands.commerce_service.domain.review.UploadReviewMediaOwnedReview;
import com.twohands.commerce_service.domain.review.UploadReviewMediaRepository;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class UploadReviewMediaRepositoryAdapter implements UploadReviewMediaRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public UploadReviewMediaRepositoryAdapter(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<UploadReviewMediaOwnedReview> findOwnedReview(UUID reviewId, UUID buyerId) {
        String sql = """
                SELECT id, buyer_id, status::text AS review_status
                FROM reviews
                WHERE id = :reviewId
                  AND buyer_id = :buyerId
                """;
        List<UploadReviewMediaOwnedReview> rows = jdbcTemplate.query(
                sql,
                new MapSqlParameterSource()
                        .addValue("reviewId", reviewId)
                        .addValue("buyerId", buyerId),
                (rs, rowNum) -> new UploadReviewMediaOwnedReview(
                        UUID.fromString(rs.getString("id")),
                        UUID.fromString(rs.getString("buyer_id")),
                        ReviewStatus.valueOf(rs.getString("review_status"))
                )
        );
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.getFirst());
    }

    @Override
    public int countMediaByReviewId(UUID reviewId) {
        Integer count = jdbcTemplate.queryForObject(
                """
                        SELECT COUNT(*)
                        FROM review_media
                        WHERE review_id = :reviewId
                        """,
                new MapSqlParameterSource("reviewId", reviewId),
                Integer.class
        );
        return count == null ? 0 : count;
    }

    @Override
    @Transactional
    public List<ReviewMediaItem> insertMedia(UUID reviewId, List<ReviewMediaInsertDraft> drafts) {
        List<ReviewMediaItem> saved = new ArrayList<>();
        for (ReviewMediaInsertDraft draft : drafts) {
            UUID mediaId = UUID.randomUUID();
            jdbcTemplate.update(
                    """
                            INSERT INTO review_media (id, review_id, url, type)
                            VALUES (:id, :reviewId, :url, :type)
                            """,
                    new MapSqlParameterSource()
                            .addValue("id", mediaId)
                            .addValue("reviewId", reviewId)
                            .addValue("url", draft.url())
                            .addValue("type", draft.type().name())
            );
            saved.add(new ReviewMediaItem(mediaId, draft.url(), draft.type()));
        }
        return saved;
    }
}
