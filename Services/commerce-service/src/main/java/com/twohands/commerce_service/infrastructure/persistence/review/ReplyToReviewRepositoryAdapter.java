package com.twohands.commerce_service.infrastructure.persistence.review;

import com.twohands.commerce_service.domain.review.ReplyToReviewRepository;
import com.twohands.commerce_service.domain.review.ReplyToReviewResult;
import com.twohands.commerce_service.domain.review.ReviewForSellerReply;
import com.twohands.commerce_service.domain.review.ReviewStatus;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class ReplyToReviewRepositoryAdapter implements ReplyToReviewRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public ReplyToReviewRepositoryAdapter(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<ReviewForSellerReply> findReviewById(UUID reviewId) {
        String sql = """
                SELECT id, seller_id, buyer_id, status::text AS status
                FROM reviews
                WHERE id = :reviewId
                """;
        List<ReviewForSellerReply> rows = jdbcTemplate.query(
                sql,
                new MapSqlParameterSource("reviewId", reviewId),
                this::mapReview
        );
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.getFirst());
    }

    @Override
    public boolean existsReplyByReviewId(UUID reviewId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM review_replies WHERE review_id = :reviewId",
                new MapSqlParameterSource("reviewId", reviewId),
                Integer.class
        );
        return count != null && count > 0;
    }

    @Override
    public ReplyToReviewResult insertReply(UUID reviewId, UUID sellerId, String content, Instant createdAt) {
        UUID replyId = UUID.randomUUID();
        String sql = """
                INSERT INTO review_replies(id, review_id, seller_id, content, created_at)
                VALUES (:id, :reviewId, :sellerId, :content, :createdAt)
                """;
        try {
            jdbcTemplate.update(sql, new MapSqlParameterSource()
                    .addValue("id", replyId)
                    .addValue("reviewId", reviewId)
                    .addValue("sellerId", sellerId)
                    .addValue("content", content)
                    .addValue("createdAt", Timestamp.from(createdAt)));
        } catch (DuplicateKeyException ex) {
            throw new AppException(ErrorCode.REVIEW_REPLY_EXISTS);
        }
        return new ReplyToReviewResult(replyId, reviewId, sellerId, content, createdAt);
    }

    private ReviewForSellerReply mapReview(ResultSet rs, int rowNum) throws SQLException {
        return new ReviewForSellerReply(
                UUID.fromString(rs.getString("id")),
                UUID.fromString(rs.getString("seller_id")),
                UUID.fromString(rs.getString("buyer_id")),
                ReviewStatus.valueOf(rs.getString("status"))
        );
    }
}
