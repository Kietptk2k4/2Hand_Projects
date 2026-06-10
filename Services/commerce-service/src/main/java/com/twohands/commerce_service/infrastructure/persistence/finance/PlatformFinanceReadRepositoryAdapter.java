package com.twohands.commerce_service.infrastructure.persistence.finance;

import com.twohands.commerce_service.domain.finance.PlatformCodPipeline;
import com.twohands.commerce_service.domain.finance.PlatformFinanceReadRepository;
import com.twohands.commerce_service.domain.finance.PlatformFinanceSummary;
import com.twohands.commerce_service.domain.finance.PlatformPayoutStatusOverview;
import com.twohands.commerce_service.domain.finance.PlatformRevenueTrendPoint;
import com.twohands.commerce_service.domain.finance.PlatformTopSeller;
import com.twohands.commerce_service.domain.finance.RevenueTrendGranularity;
import com.twohands.commerce_service.domain.finance.SellerRevenueBucket;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public class PlatformFinanceReadRepositoryAdapter implements PlatformFinanceReadRepository {

    private static final String COD_PIPELINE_SELECT = """
            SELECT COALESCE(SUM(CASE WHEN oi.status IN ('PROCESSING', 'SHIPPED') THEN oi.final_price ELSE 0 END), 0)
                       AS in_transit_amount,
                   COALESCE(SUM(CASE WHEN oi.status IN ('PROCESSING', 'SHIPPED') THEN 1 ELSE 0 END), 0)
                       AS in_transit_count,
                   COALESCE(SUM(CASE WHEN oi.status = 'DELIVERED' THEN oi.final_price ELSE 0 END), 0)
                       AS pending_confirm_amount,
                   COALESCE(SUM(CASE WHEN oi.status = 'DELIVERED' THEN 1 ELSE 0 END), 0)
                       AS pending_confirm_count,
                   COALESCE(SUM(CASE WHEN oi.status = 'COMPLETED' AND p.status = 'PAID' THEN oi.final_price ELSE 0 END), 0)
                       AS recognized_amount,
                   COALESCE(SUM(CASE WHEN oi.status = 'COMPLETED' AND p.status = 'PAID' THEN 1 ELSE 0 END), 0)
                       AS recognized_count
            FROM order_items oi
            INNER JOIN orders o ON o.id = oi.order_id
            LEFT JOIN payments p ON p.order_id = o.id
            WHERE oi.status NOT IN ('CANCELLED', 'FAILED', 'RETURNED', 'PENDING')
            """;

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public PlatformFinanceReadRepositoryAdapter(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public PlatformFinanceSummary findPlatformSummary(Instant from, Instant toExclusive) {
        MapSqlParameterSource params = rangeParams(from, toExclusive);

        BigDecimal recognizedGmv = jdbcTemplate.queryForObject(
                """
                        SELECT COALESCE(SUM(oi.final_price), 0)
                        FROM order_items oi
                        INNER JOIN orders o ON o.id = oi.order_id
                        LEFT JOIN payments p ON p.order_id = o.id
                        WHERE oi.status = 'COMPLETED'
                          AND p.status = 'PAID'
                          AND oi.completed_at >= :from
                          AND oi.completed_at < :to
                        """,
                params,
                BigDecimal.class
        );

        Long recognizedItemCount = jdbcTemplate.queryForObject(
                """
                        SELECT COUNT(*)
                        FROM order_items oi
                        INNER JOIN orders o ON o.id = oi.order_id
                        LEFT JOIN payments p ON p.order_id = o.id
                        WHERE oi.status = 'COMPLETED'
                          AND p.status = 'PAID'
                          AND oi.completed_at >= :from
                          AND oi.completed_at < :to
                        """,
                params,
                Long.class
        );

        BigDecimal totalPlatformFee = jdbcTemplate.queryForObject(
                """
                        SELECT COALESCE(SUM(platform_fee_amount), 0)
                        FROM seller_ledger_entries
                        WHERE entry_type = 'CREDIT'
                          AND status = 'POSTED'
                          AND created_at >= :from
                          AND created_at < :to
                        """,
                params,
                BigDecimal.class
        );

        PlatformCodPipeline pipeline = findCodPipeline();
        BigDecimal codPipelineAmount = pipeline.inTransit().amount().add(pipeline.pendingConfirm().amount());

        MapSqlParameterSource payoutParams = new MapSqlParameterSource();
        Long pendingPayoutCount = jdbcTemplate.queryForObject(
                """
                        SELECT COUNT(*)
                        FROM seller_payout_requests
                        WHERE status = 'REQUESTED'
                        """,
                payoutParams,
                Long.class
        );
        BigDecimal pendingPayoutAmount = jdbcTemplate.queryForObject(
                """
                        SELECT COALESCE(SUM(amount), 0)
                        FROM seller_payout_requests
                        WHERE status = 'REQUESTED'
                        """,
                payoutParams,
                BigDecimal.class
        );

        BigDecimal paidPayoutAmount = jdbcTemplate.queryForObject(
                """
                        SELECT COALESCE(SUM(amount), 0)
                        FROM seller_payout_requests
                        WHERE status = 'PAID'
                          AND paid_at >= :from
                          AND paid_at < :to
                        """,
                params,
                BigDecimal.class
        );

        return new PlatformFinanceSummary(
                recognizedGmv == null ? BigDecimal.ZERO : recognizedGmv,
                recognizedItemCount == null ? 0L : recognizedItemCount,
                totalPlatformFee == null ? BigDecimal.ZERO : totalPlatformFee,
                codPipelineAmount,
                pendingPayoutCount == null ? 0L : pendingPayoutCount,
                pendingPayoutAmount == null ? BigDecimal.ZERO : pendingPayoutAmount,
                paidPayoutAmount == null ? BigDecimal.ZERO : paidPayoutAmount,
                from,
                toExclusive
        );
    }

    @Override
    public PlatformCodPipeline findCodPipeline() {
        return jdbcTemplate.queryForObject(COD_PIPELINE_SELECT, new MapSqlParameterSource(), (rs, rowNum) ->
                new PlatformCodPipeline(
                        toBucket(rs, "in_transit_amount", "in_transit_count"),
                        toBucket(rs, "pending_confirm_amount", "pending_confirm_count"),
                        toBucket(rs, "recognized_amount", "recognized_count")
                )
        );
    }

    @Override
    public List<PlatformTopSeller> findTopSellers(Instant from, Instant toExclusive, int limit) {
        String sql = """
                SELECT oi.seller_id,
                       COALESCE(ss.shop_name, 'Unknown shop') AS shop_name,
                       COALESCE(SUM(oi.final_price), 0) AS recognized_gross,
                       COALESCE(SUM(sle.platform_fee_amount), 0) AS platform_fee,
                       COUNT(*) AS item_count
                FROM order_items oi
                INNER JOIN orders o ON o.id = oi.order_id
                LEFT JOIN payments p ON p.order_id = o.id
                LEFT JOIN seller_shops ss ON ss.seller_id = oi.seller_id
                LEFT JOIN seller_ledger_entries sle
                       ON sle.order_item_id = oi.id
                      AND sle.entry_type = 'CREDIT'
                      AND sle.status = 'POSTED'
                WHERE oi.status = 'COMPLETED'
                  AND p.status = 'PAID'
                  AND oi.completed_at >= :from
                  AND oi.completed_at < :to
                GROUP BY oi.seller_id, ss.shop_name
                ORDER BY recognized_gross DESC, item_count DESC
                LIMIT :limit
                """;

        MapSqlParameterSource params = rangeParams(from, toExclusive).addValue("limit", limit);
        return jdbcTemplate.query(sql, params, this::mapTopSeller);
    }

    @Override
    public List<PlatformRevenueTrendPoint> findRevenueTrend(
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
                       COALESCE(SUM(sle.gross_amount), 0) AS gmv_amount,
                       COALESCE(SUM(sle.platform_fee_amount), 0) AS platform_fee_amount,
                       COUNT(*) AS item_count
                FROM seller_ledger_entries sle
                WHERE sle.entry_type = 'CREDIT'
                  AND sle.status = 'POSTED'
                  AND sle.created_at >= :from
                  AND sle.created_at < :to
                GROUP BY 1
                ORDER BY 1
                """.formatted(truncUnit);

        return jdbcTemplate.query(sql, rangeParams(from, toExclusive), this::mapTrendPoint);
    }

    @Override
    public List<PlatformPayoutStatusOverview> findPayoutOverview(Instant from, Instant toExclusive) {
        String sql = """
                SELECT status::text AS status,
                       COUNT(*) AS request_count,
                       COALESCE(SUM(amount), 0) AS total_amount
                FROM seller_payout_requests
                WHERE requested_at >= :from
                  AND requested_at < :to
                GROUP BY status
                ORDER BY status
                """;
        return jdbcTemplate.query(sql, rangeParams(from, toExclusive), this::mapPayoutOverview);
    }

    private MapSqlParameterSource rangeParams(Instant from, Instant toExclusive) {
        return new MapSqlParameterSource()
                .addValue("from", Timestamp.from(from))
                .addValue("to", Timestamp.from(toExclusive));
    }

    private SellerRevenueBucket toBucket(ResultSet rs, String amountColumn, String countColumn) throws SQLException {
        return new SellerRevenueBucket(rs.getBigDecimal(amountColumn), rs.getLong(countColumn));
    }

    private PlatformTopSeller mapTopSeller(ResultSet rs, int rowNum) throws SQLException {
        return new PlatformTopSeller(
                UUID.fromString(rs.getString("seller_id")),
                rs.getString("shop_name"),
                rs.getBigDecimal("recognized_gross"),
                rs.getBigDecimal("platform_fee"),
                rs.getLong("item_count")
        );
    }

    private PlatformRevenueTrendPoint mapTrendPoint(ResultSet rs, int rowNum) throws SQLException {
        Timestamp periodStart = rs.getTimestamp("period_start");
        return new PlatformRevenueTrendPoint(
                periodStart == null ? null : periodStart.toInstant(),
                rs.getBigDecimal("gmv_amount"),
                rs.getBigDecimal("platform_fee_amount"),
                rs.getLong("item_count")
        );
    }

    private PlatformPayoutStatusOverview mapPayoutOverview(ResultSet rs, int rowNum) throws SQLException {
        return new PlatformPayoutStatusOverview(
                rs.getString("status"),
                rs.getLong("request_count"),
                rs.getBigDecimal("total_amount")
        );
    }
}
