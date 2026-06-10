package com.twohands.commerce_service.infrastructure.persistence.finance;

import com.twohands.commerce_service.domain.finance.RevenueTrendGranularity;
import com.twohands.commerce_service.domain.finance.SellerBalanceSummary;
import com.twohands.commerce_service.domain.finance.SellerFinanceReadRepository;
import com.twohands.commerce_service.domain.finance.SellerRevenueBucket;
import com.twohands.commerce_service.domain.finance.SellerRevenueSummary;
import com.twohands.commerce_service.domain.finance.SellerRevenueTrendPoint;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class SellerFinanceReadRepositoryAdapter implements SellerFinanceReadRepository {

    private static final String SUMMARY_SELECT = """
            SELECT COALESCE(SUM(CASE
                       WHEN oi.status IN ('PROCESSING', 'SHIPPED')
                            AND (:applyCreatedRange = FALSE OR (oi.created_at >= :from AND oi.created_at < :to))
                       THEN oi.final_price ELSE 0 END), 0) AS in_transit_amount,
                   COALESCE(SUM(CASE
                       WHEN oi.status IN ('PROCESSING', 'SHIPPED')
                            AND (:applyCreatedRange = FALSE OR (oi.created_at >= :from AND oi.created_at < :to))
                       THEN 1 ELSE 0 END), 0) AS in_transit_count,
                   COALESCE(SUM(CASE
                       WHEN oi.status = 'DELIVERED'
                            AND (:applyCreatedRange = FALSE OR (oi.created_at >= :from AND oi.created_at < :to))
                       THEN oi.final_price ELSE 0 END), 0) AS pending_confirm_amount,
                   COALESCE(SUM(CASE
                       WHEN oi.status = 'DELIVERED'
                            AND (:applyCreatedRange = FALSE OR (oi.created_at >= :from AND oi.created_at < :to))
                       THEN 1 ELSE 0 END), 0) AS pending_confirm_count,
                   COALESCE(SUM(CASE
                       WHEN oi.status = 'COMPLETED'
                            AND p.status = 'PAID'
                            AND (:applyCompletedRange = FALSE OR (oi.completed_at >= :from AND oi.completed_at < :to))
                       THEN oi.final_price ELSE 0 END), 0) AS recognized_amount,
                   COALESCE(SUM(CASE
                       WHEN oi.status = 'COMPLETED'
                            AND p.status = 'PAID'
                            AND (:applyCompletedRange = FALSE OR (oi.completed_at >= :from AND oi.completed_at < :to))
                       THEN 1 ELSE 0 END), 0) AS recognized_count
            FROM order_items oi
            INNER JOIN orders o ON o.id = oi.order_id
            LEFT JOIN payments p ON p.order_id = o.id
            WHERE oi.seller_id = :sellerId
              AND oi.status NOT IN ('CANCELLED', 'FAILED', 'RETURNED', 'PENDING')
            """;

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public SellerFinanceReadRepositoryAdapter(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public SellerRevenueSummary findRevenueSummary(UUID sellerId, Optional<Instant> from, Optional<Instant> toExclusive) {
        boolean hasRange = from.isPresent() && toExclusive.isPresent();
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("sellerId", sellerId)
                .addValue("applyCreatedRange", hasRange)
                .addValue("applyCompletedRange", hasRange)
                .addValue("from", from.map(Timestamp::from).orElse(null))
                .addValue("to", toExclusive.map(Timestamp::from).orElse(null));

        return jdbcTemplate.queryForObject(SUMMARY_SELECT, params, (rs, rowNum) -> {
            SellerRevenueBucket inTransit = toBucket(rs, "in_transit_amount", "in_transit_count");
            SellerRevenueBucket pendingConfirm = toBucket(rs, "pending_confirm_amount", "pending_confirm_count");
            SellerRevenueBucket recognized = toBucket(rs, "recognized_amount", "recognized_count");
            BigDecimal totalGross = inTransit.amount()
                    .add(pendingConfirm.amount())
                    .add(recognized.amount());
            return new SellerRevenueSummary(
                    inTransit,
                    pendingConfirm,
                    recognized,
                    totalGross,
                    SellerBalanceSummary.empty(),
                    from.orElse(null),
                    toExclusive.orElse(null)
            );
        });
    }

    @Override
    public List<SellerRevenueTrendPoint> findRecognizedRevenueTrend(
            UUID sellerId,
            Instant from,
            Instant toExclusive,
            RevenueTrendGranularity granularity
    ) {
        String truncUnit = switch (granularity) {
            case DAY -> "day";
            case WEEK -> "week";
            case MONTH -> "month";
        };

        String sql = """
                SELECT date_trunc('%s', sle.created_at AT TIME ZONE 'UTC') AS period_start,
                       COALESCE(SUM(sle.gross_amount), 0) AS recognized_amount,
                       COUNT(*) AS item_count
                FROM seller_ledger_entries sle
                WHERE sle.seller_id = :sellerId
                  AND sle.entry_type = 'CREDIT'
                  AND sle.status = 'POSTED'
                  AND sle.created_at >= :from
                  AND sle.created_at < :to
                GROUP BY 1
                ORDER BY 1
                """.formatted(truncUnit);

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("sellerId", sellerId)
                .addValue("from", Timestamp.from(from))
                .addValue("to", Timestamp.from(toExclusive));

        return jdbcTemplate.query(sql, params, this::mapTrendPoint);
    }

    private SellerRevenueBucket toBucket(ResultSet rs, String amountColumn, String countColumn) throws SQLException {
        return new SellerRevenueBucket(
                rs.getBigDecimal(amountColumn),
                rs.getLong(countColumn)
        );
    }

    private SellerRevenueTrendPoint mapTrendPoint(ResultSet rs, int rowNum) throws SQLException {
        Timestamp periodStart = rs.getTimestamp("period_start");
        return new SellerRevenueTrendPoint(
                periodStart == null ? null : periodStart.toInstant(),
                rs.getBigDecimal("recognized_amount"),
                rs.getLong("item_count")
        );
    }
}
