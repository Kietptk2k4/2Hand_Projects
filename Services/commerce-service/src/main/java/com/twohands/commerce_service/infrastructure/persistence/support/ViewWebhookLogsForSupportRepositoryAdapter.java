package com.twohands.commerce_service.infrastructure.persistence.support;

import com.twohands.commerce_service.domain.support.ViewWebhookLogsForSupportRepository;
import com.twohands.commerce_service.domain.support.WebhookLogSupportEntry;
import com.twohands.commerce_service.domain.support.WebhookLogSupportPagedResult;
import com.twohands.commerce_service.domain.support.WebhookLogSupportSearchCriteria;
import com.twohands.commerce_service.domain.support.WebhookLogSupportStats;
import com.twohands.commerce_service.domain.support.WebhookPayloadSanitizer;
import com.twohands.commerce_service.domain.support.WebhookSupportPageRequest;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class ViewWebhookLogsForSupportRepositoryAdapter implements ViewWebhookLogsForSupportRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public ViewWebhookLogsForSupportRepositoryAdapter(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public WebhookLogSupportPagedResult search(WebhookLogSupportSearchCriteria criteria, WebhookSupportPageRequest pageRequest) {
        UnionQuery unionQuery = buildUnionQuery(criteria);
        if (unionQuery.isEmpty()) {
            return emptyPage(pageRequest);
        }

        long totalElements = countUnion(unionQuery.sql(), unionQuery.params());
        int totalPages = totalElements == 0 ? 0 : (int) Math.ceil((double) totalElements / pageRequest.size());

        MapSqlParameterSource params = cloneParams(unionQuery.params());
        params.addValue("limit", pageRequest.size());
        params.addValue("offset", (pageRequest.page() - 1) * pageRequest.size());

        String searchSql = """
                SELECT *
                FROM (%s) webhook_logs
                ORDER BY received_at DESC
                LIMIT :limit OFFSET :offset
                """.formatted(unionQuery.sql());

        List<WebhookLogSupportEntry> items = jdbcTemplate.query(searchSql, params, this::mapRow);
        return new WebhookLogSupportPagedResult(
                items,
                pageRequest.page(),
                pageRequest.size(),
                totalElements,
                totalPages
        );
    }

    @Override
    public Optional<WebhookLogSupportEntry> findById(UUID logId, String provider) {
        if (logId == null || provider == null) {
            return Optional.empty();
        }
        WebhookLogSupportSearchCriteria criteria = new WebhookLogSupportSearchCriteria(
                provider,
                null,
                null,
                null,
                null,
                null,
                null
        );
        UnionQuery unionQuery = buildUnionQuery(criteria);
        if (unionQuery.isEmpty()) {
            return Optional.empty();
        }

        MapSqlParameterSource params = cloneParams(unionQuery.params());
        params.addValue("logId", logId);

        String sql = """
                SELECT *
                FROM (%s) webhook_logs
                WHERE log_id = :logId
                LIMIT 1
                """.formatted(unionQuery.sql());

        List<WebhookLogSupportEntry> rows = jdbcTemplate.query(sql, params, this::mapRow);
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.getFirst());
    }

    @Override
    public WebhookLogSupportStats aggregateStats(WebhookLogSupportSearchCriteria criteria) {
        UnionQuery unionQuery = buildUnionQuery(criteria);
        if (unionQuery.isEmpty()) {
            return new WebhookLogSupportStats(0L, 0L, 0L, 0L, 0L, 0L);
        }

        String sql = """
                SELECT COUNT(*) AS total,
                       SUM(CASE WHEN processing_status = 'PENDING' THEN 1 ELSE 0 END) AS pending,
                       SUM(CASE WHEN processing_status = 'INVALID_SIGNATURE' THEN 1 ELSE 0 END) AS invalid_signature,
                       SUM(CASE WHEN processing_status = 'PROCESSED' THEN 1 ELSE 0 END) AS processed,
                       SUM(CASE WHEN provider = 'PAYOS' THEN 1 ELSE 0 END) AS payos_count,
                       SUM(CASE WHEN provider = 'GHN' THEN 1 ELSE 0 END) AS ghn_count
                FROM (%s) webhook_logs
                """.formatted(unionQuery.sql());

        return jdbcTemplate.queryForObject(sql, unionQuery.params(), (rs, rowNum) -> new WebhookLogSupportStats(
                rs.getLong("total"),
                rs.getLong("pending"),
                rs.getLong("invalid_signature"),
                rs.getLong("processed"),
                rs.getLong("payos_count"),
                rs.getLong("ghn_count")
        ));
    }

    @Override
    public List<WebhookLogSupportEntry> searchAll(WebhookLogSupportSearchCriteria criteria, int maxRows) {
        UnionQuery unionQuery = buildUnionQuery(criteria);
        if (unionQuery.isEmpty()) {
            return List.of();
        }

        MapSqlParameterSource params = cloneParams(unionQuery.params());
        params.addValue("limit", maxRows);

        String sql = """
                SELECT *
                FROM (%s) webhook_logs
                ORDER BY received_at DESC
                LIMIT :limit
                """.formatted(unionQuery.sql());

        return jdbcTemplate.query(sql, params, this::mapRow);
    }

    private long countUnion(String unionSql, MapSqlParameterSource params) {
        String countSql = "SELECT COUNT(*) FROM (" + unionSql + ") webhook_logs";
        Long count = jdbcTemplate.queryForObject(countSql, params, Long.class);
        return count == null ? 0L : count;
    }

    private UnionQuery buildUnionQuery(WebhookLogSupportSearchCriteria criteria) {
        boolean includePayos = criteria.provider() == null || "PAYOS".equals(criteria.provider());
        boolean includeGhn = criteria.provider() == null || "GHN".equals(criteria.provider());

        if ("INVALID_SIGNATURE".equals(criteria.processingStatus())) {
            includeGhn = false;
        }

        MapSqlParameterSource params = new MapSqlParameterSource();
        if (criteria.processingStatus() != null) {
            params.addValue("processingStatus", criteria.processingStatus());
        }

        List<String> unionParts = new ArrayList<>();

        if (includePayos) {
            unionParts.add(buildPayosSelect(criteria, params));
        }
        if (includeGhn) {
            unionParts.add(buildGhnSelect(criteria, params));
        }

        if (unionParts.isEmpty()) {
            return UnionQuery.empty();
        }

        return new UnionQuery(String.join(" UNION ALL ", unionParts), params);
    }

    private String buildPayosSelect(WebhookLogSupportSearchCriteria criteria, MapSqlParameterSource params) {
        StringBuilder sql = new StringBuilder("""
                SELECT p.id AS log_id,
                       'PAYOS' AS provider,
                       p.payos_order_code AS reference_id,
                       p.event_type AS event_type,
                       CASE
                           WHEN NOT p.signature_valid THEN 'INVALID_SIGNATURE'
                           WHEN p.processed THEN 'PROCESSED'
                           ELSE 'PENDING'
                       END AS processing_status,
                       p.signature_valid AS signature_valid,
                       CONCAT('PAYOS:', p.payos_order_code, ':', p.event_type) AS idempotency_key,
                       p.payload::text AS payload_json,
                       p.created_at AS received_at,
                       pay.id AS payment_id,
                       NULL::uuid AS shipment_id,
                       pay.order_id AS order_id
                FROM payment_webhook_logs p
                LEFT JOIN payments pay ON pay.payos_order_code = p.payos_order_code
                WHERE 1 = 1
                """);
        appendCommonFilters(sql, params, criteria, "p.payos_order_code", "p.event_type", "p.created_at");
        appendPayosStatusFilter(sql, criteria);
        return sql.toString();
    }

    private String buildGhnSelect(WebhookLogSupportSearchCriteria criteria, MapSqlParameterSource params) {
        StringBuilder sql = new StringBuilder("""
                SELECT g.id AS log_id,
                       'GHN' AS provider,
                       g.ghn_order_code AS reference_id,
                       g.status AS event_type,
                       CASE WHEN g.processed THEN 'PROCESSED' ELSE 'PENDING' END AS processing_status,
                       NULL::boolean AS signature_valid,
                       CONCAT('GHN:', g.ghn_order_code, ':', g.status) AS idempotency_key,
                       g.payload::text AS payload_json,
                       g.created_at AS received_at,
                       NULL::uuid AS payment_id,
                       s.id AS shipment_id,
                       s.order_id AS order_id
                FROM ghn_webhook_logs g
                LEFT JOIN shipments s ON s.ghn_order_code = g.ghn_order_code
                WHERE 1 = 1
                """);
        appendCommonFilters(sql, params, criteria, "g.ghn_order_code", "g.status", "g.created_at");
        appendGhnStatusFilter(sql, criteria);
        return sql.toString();
    }

    private void appendCommonFilters(
            StringBuilder sql,
            MapSqlParameterSource params,
            WebhookLogSupportSearchCriteria criteria,
            String referenceColumn,
            String eventTypeColumn,
            String createdAtColumn
    ) {
        if (criteria.referenceId() != null) {
            sql.append(" AND ").append(referenceColumn).append(" = :referenceId");
            params.addValue("referenceId", criteria.referenceId());
        }
        if (criteria.searchQuery() != null) {
            sql.append(" AND ").append(referenceColumn).append(" ILIKE :searchPattern");
            params.addValue("searchPattern", "%" + criteria.searchQuery() + "%");
        }
        if (criteria.eventType() != null) {
            sql.append(" AND ").append(eventTypeColumn).append(" = :eventType");
            params.addValue("eventType", criteria.eventType());
        }
        if (criteria.from() != null) {
            sql.append(" AND ").append(createdAtColumn).append(" >= :from");
            params.addValue("from", Timestamp.from(criteria.from()));
        }
        if (criteria.to() != null) {
            sql.append(" AND ").append(createdAtColumn).append(" <= :to");
            params.addValue("to", Timestamp.from(criteria.to()));
        }
    }

    private void appendPayosStatusFilter(StringBuilder sql, WebhookLogSupportSearchCriteria criteria) {
        if (criteria.processingStatus() == null) {
            return;
        }
        sql.append("""
                 AND CASE
                     WHEN NOT p.signature_valid THEN 'INVALID_SIGNATURE'
                     WHEN p.processed THEN 'PROCESSED'
                     ELSE 'PENDING'
                 END = :processingStatus
                """);
    }

    private void appendGhnStatusFilter(StringBuilder sql, WebhookLogSupportSearchCriteria criteria) {
        if (criteria.processingStatus() == null) {
            return;
        }
        sql.append("""
                 AND CASE WHEN g.processed THEN 'PROCESSED' ELSE 'PENDING' END = :processingStatus
                """);
    }

    private WebhookLogSupportEntry mapRow(ResultSet rs, int rowNum) throws SQLException {
        String provider = rs.getString("provider");
        String payloadJson = rs.getString("payload_json");
        return new WebhookLogSupportEntry(
                UUID.fromString(rs.getString("log_id")),
                provider,
                rs.getString("reference_id"),
                rs.getString("event_type"),
                rs.getString("processing_status"),
                (Boolean) rs.getObject("signature_valid"),
                rs.getString("idempotency_key"),
                WebhookPayloadSanitizer.sanitize(provider, payloadJson),
                rs.getTimestamp("received_at").toInstant(),
                readUuid(rs, "payment_id"),
                readUuid(rs, "shipment_id"),
                readUuid(rs, "order_id")
        );
    }

    private UUID readUuid(ResultSet rs, String column) throws SQLException {
        String value = rs.getString(column);
        return value == null ? null : UUID.fromString(value);
    }

    private MapSqlParameterSource cloneParams(MapSqlParameterSource source) {
        MapSqlParameterSource target = new MapSqlParameterSource();
        for (String name : source.getParameterNames()) {
            target.addValue(name, source.getValue(name));
        }
        return target;
    }

    private WebhookLogSupportPagedResult emptyPage(WebhookSupportPageRequest pageRequest) {
        return new WebhookLogSupportPagedResult(
                List.of(),
                pageRequest.page(),
                pageRequest.size(),
                0L,
                0
        );
    }

    private record UnionQuery(String sql, MapSqlParameterSource params) {
        static UnionQuery empty() {
            return new UnionQuery("", new MapSqlParameterSource());
        }

        boolean isEmpty() {
            return sql == null || sql.isBlank();
        }
    }
}
