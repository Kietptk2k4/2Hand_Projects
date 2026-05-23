package com.twohands.commerce_service.infrastructure.persistence.support;

import com.twohands.commerce_service.domain.support.ViewWebhookLogsForSupportRepository;
import com.twohands.commerce_service.domain.support.WebhookLogSupportEntry;
import com.twohands.commerce_service.domain.support.WebhookLogSupportPagedResult;
import com.twohands.commerce_service.domain.support.WebhookLogSupportSearchCriteria;
import com.twohands.commerce_service.domain.support.WebhookPayloadSanitizer;
import com.twohands.commerce_service.domain.support.WebhookSupportPageRequest;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Repository
public class ViewWebhookLogsForSupportRepositoryAdapter implements ViewWebhookLogsForSupportRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public ViewWebhookLogsForSupportRepositoryAdapter(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public WebhookLogSupportPagedResult search(WebhookLogSupportSearchCriteria criteria, WebhookSupportPageRequest pageRequest) {
        boolean includePayos = criteria.provider() == null || "PAYOS".equals(criteria.provider());
        boolean includeGhn = criteria.provider() == null || "GHN".equals(criteria.provider());

        if ("INVALID_SIGNATURE".equals(criteria.processingStatus())) {
            includeGhn = false;
        }

        MapSqlParameterSource params = new MapSqlParameterSource();
        applyCommonFilters(params, criteria);

        List<String> unionParts = new ArrayList<>();
        if (includePayos) {
            unionParts.add(buildPayosSelect(criteria, params));
        }
        if (includeGhn) {
            unionParts.add(buildGhnSelect(criteria, params));
        }

        if (unionParts.isEmpty()) {
            return emptyPage(pageRequest);
        }

        String unionSql = String.join(" UNION ALL ", unionParts);
        long totalElements = countUnion(unionSql, params);
        int totalPages = totalElements == 0 ? 0 : (int) Math.ceil((double) totalElements / pageRequest.size());

        params.addValue("limit", pageRequest.size());
        params.addValue("offset", (pageRequest.page() - 1) * pageRequest.size());

        String searchSql = """
                SELECT *
                FROM (%s) webhook_logs
                ORDER BY received_at DESC
                LIMIT :limit OFFSET :offset
                """.formatted(unionSql);

        List<WebhookLogSupportEntry> items = jdbcTemplate.query(searchSql, params, this::mapRow);

        return new WebhookLogSupportPagedResult(
                items,
                pageRequest.page(),
                pageRequest.size(),
                totalElements,
                totalPages
        );
    }

    private long countUnion(String unionSql, MapSqlParameterSource params) {
        String countSql = "SELECT COUNT(*) FROM (" + unionSql + ") webhook_logs";
        Long count = jdbcTemplate.queryForObject(countSql, params, Long.class);
        return count == null ? 0L : count;
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
                       0 AS retry_count,
                       CONCAT('PAYOS:', p.payos_order_code, ':', p.event_type) AS idempotency_key,
                       p.payload::text AS payload_json,
                       p.created_at AS received_at
                FROM payment_webhook_logs p
                WHERE 1 = 1
                """);
        if (criteria.referenceId() != null) {
            sql.append(" AND p.payos_order_code = :referenceId");
        }
        if (criteria.from() != null) {
            sql.append(" AND p.created_at >= :from");
        }
        if (criteria.to() != null) {
            sql.append(" AND p.created_at <= :to");
        }
        if (criteria.processingStatus() != null) {
            sql.append("""
                     AND CASE
                         WHEN NOT p.signature_valid THEN 'INVALID_SIGNATURE'
                         WHEN p.processed THEN 'PROCESSED'
                         ELSE 'PENDING'
                     END = :processingStatus
                    """);
        }
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
                       0 AS retry_count,
                       CONCAT('GHN:', g.ghn_order_code, ':', g.status) AS idempotency_key,
                       g.payload::text AS payload_json,
                       g.created_at AS received_at
                FROM ghn_webhook_logs g
                WHERE 1 = 1
                """);
        if (criteria.referenceId() != null) {
            sql.append(" AND g.ghn_order_code = :referenceId");
        }
        if (criteria.from() != null) {
            sql.append(" AND g.created_at >= :from");
        }
        if (criteria.to() != null) {
            sql.append(" AND g.created_at <= :to");
        }
        if (criteria.processingStatus() != null) {
            sql.append("""
                     AND CASE WHEN g.processed THEN 'PROCESSED' ELSE 'PENDING' END = :processingStatus
                    """);
        }
        return sql.toString();
    }

    private void applyCommonFilters(MapSqlParameterSource params, WebhookLogSupportSearchCriteria criteria) {
        if (criteria.referenceId() != null) {
            params.addValue("referenceId", criteria.referenceId());
        }
        if (criteria.from() != null) {
            params.addValue("from", Timestamp.from(criteria.from()));
        }
        if (criteria.to() != null) {
            params.addValue("to", Timestamp.from(criteria.to()));
        }
        if (criteria.processingStatus() != null) {
            params.addValue("processingStatus", criteria.processingStatus());
        }
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
                rs.getInt("retry_count"),
                rs.getString("idempotency_key"),
                WebhookPayloadSanitizer.sanitize(provider, payloadJson),
                rs.getTimestamp("received_at").toInstant()
        );
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
}
