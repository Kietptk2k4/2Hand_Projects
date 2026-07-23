package com.twohands.commerce_service.infrastructure.persistence.admin;

import com.twohands.commerce_service.common.pagination.PageQuery;
import com.twohands.commerce_service.domain.admin.AdminReviewListEntry;
import com.twohands.commerce_service.domain.admin.AdminReviewListSort;
import com.twohands.commerce_service.domain.admin.ViewAdminReviewsForModerationRepository;
import com.twohands.commerce_service.domain.review.ReviewStatus;
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
public class ViewAdminReviewsForModerationRepositoryAdapter implements ViewAdminReviewsForModerationRepository {

    private static final String BASE_FROM = """
            FROM reviews r
            INNER JOIN order_items oi ON oi.id = r.order_item_id
            WHERE 1 = 1
            """;

    private static final String SELECT_COLUMNS = """
            SELECT r.id AS review_id,
                   r.order_item_id,
                   oi.product_id,
                   oi.product_name_snapshot AS product_title,
                   oi.image_snapshot AS product_thumbnail_url,
                   r.buyer_id,
                   r.seller_id,
                   r.rating,
                   r.comment,
                   r.status::text AS status,
                   r.created_at
            """;

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public ViewAdminReviewsForModerationRepositoryAdapter(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public long count(Optional<ReviewStatus> status, Optional<Integer> rating, Optional<String> searchQuery) {
        MapSqlParameterSource params = baseParams(status, rating, searchQuery);
        Long count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) " + BASE_FROM + filterClause(status, rating, searchQuery),
                params,
                Long.class
        );
        return count == null ? 0L : count;
    }

    @Override
    public List<AdminReviewListEntry> find(
            Optional<ReviewStatus> status,
            Optional<Integer> rating,
            Optional<String> searchQuery,
            AdminReviewListSort sort,
            PageQuery pageQuery
    ) {
        MapSqlParameterSource params = baseParams(status, rating, searchQuery)
                .addValue("limit", pageQuery.limit())
                .addValue("offset", pageQuery.offset());

        return jdbcTemplate.query(
                SELECT_COLUMNS + BASE_FROM + filterClause(status, rating, searchQuery) + orderByClause(sort) + """
                        LIMIT :limit OFFSET :offset
                        """,
                params,
                this::mapEntry
        );
    }

    private String orderByClause(AdminReviewListSort sort) {
        return switch (sort) {
            case OLDEST -> " ORDER BY r.created_at ASC, r.id ASC ";
            case RATING_ASC -> " ORDER BY r.rating ASC, r.created_at DESC, r.id DESC ";
            case RATING_DESC -> " ORDER BY r.rating DESC, r.created_at DESC, r.id DESC ";
            case NEWEST -> " ORDER BY r.created_at DESC, r.id DESC ";
        };
    }

    private MapSqlParameterSource baseParams(
            Optional<ReviewStatus> status,
            Optional<Integer> rating,
            Optional<String> searchQuery
    ) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        status.ifPresent(value -> params.addValue("status", value.name()));
        rating.ifPresent(value -> params.addValue("rating", value));
        searchQuery.ifPresent(value -> params.addValue("searchPattern", "%" + value + "%"));
        return params;
    }

    private String filterClause(
            Optional<ReviewStatus> status,
            Optional<Integer> rating,
            Optional<String> searchQuery
    ) {
        StringBuilder clause = new StringBuilder();
        status.ifPresent(ignored -> clause.append(" AND r.status::text = :status"));
        rating.ifPresent(ignored -> clause.append(" AND r.rating = :rating"));
        searchQuery.ifPresent(ignored -> clause.append("""
                 AND (
                    CAST(r.id AS TEXT) ILIKE :searchPattern
                    OR CAST(r.order_item_id AS TEXT) ILIKE :searchPattern
                    OR oi.product_name_snapshot ILIKE :searchPattern
                )
                """));
        return clause.toString();
    }

    private AdminReviewListEntry mapEntry(ResultSet rs, int rowNum) throws SQLException {
        return new AdminReviewListEntry(
                rs.getObject("review_id", UUID.class),
                rs.getObject("order_item_id", UUID.class),
                rs.getObject("product_id", UUID.class),
                rs.getString("product_title"),
                rs.getString("product_thumbnail_url"),
                rs.getObject("buyer_id", UUID.class),
                rs.getObject("seller_id", UUID.class),
                rs.getInt("rating"),
                rs.getString("comment"),
                ReviewStatus.valueOf(rs.getString("status")),
                toInstant(rs.getTimestamp("created_at"))
        );
    }

    private Instant toInstant(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toInstant();
    }
}
