package com.twohands.auth_service.infrastructure.persistence.adapter;

import com.twohands.auth_service.domain.rbac.RbacUserListCriteria;
import com.twohands.auth_service.domain.rbac.RbacUserListItem;
import com.twohands.auth_service.domain.rbac.RbacUserListPagedResult;
import com.twohands.auth_service.domain.rbac.RbacUserListRepository;
import com.twohands.auth_service.domain.rbac.RbacUserListSortField;
import com.twohands.auth_service.infrastructure.persistence.JdbcPgEnumTypes;
import com.twohands.auth_service.infrastructure.persistence.JdbcSqlDialect;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Repository
public class RbacUserListRepositoryAdapter implements RbacUserListRepository {

    private static final String BASE_FROM = """
            FROM users u
            LEFT JOIN user_profiles p ON p.user_id = u.id
            LEFT JOIN user_roles ur ON ur.user_id = u.id
            LEFT JOIN roles r ON r.id = ur.role_id
            """;

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final JdbcSqlDialect sqlDialect;

    public RbacUserListRepositoryAdapter(
            NamedParameterJdbcTemplate jdbcTemplate,
            JdbcSqlDialect sqlDialect
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.sqlDialect = sqlDialect;
    }

    @Override
    public RbacUserListPagedResult findPage(RbacUserListCriteria criteria) {
        MapSqlParameterSource params = buildParams(criteria);
        String whereClause = buildWhereClause(criteria);
        String groupBy = " GROUP BY u.id, u.email, u.status, p.display_name, u.created_at, u.email_normalized ";

        long totalItems = countUsers(whereClause, params);
        int totalPages = totalItems == 0 ? 0 : (int) ((totalItems + criteria.size() - 1) / criteria.size());
        boolean hasNext = criteria.page() < totalPages;

        String sql = """
                SELECT
                    u.id,
                    u.email,
                    u.status,
                    u.created_at,
                    COALESCE(NULLIF(TRIM(p.display_name), ''), u.email) AS display_name,
                    COALESCE(ARRAY_AGG(DISTINCT r.code) FILTER (WHERE r.code IS NOT NULL), ARRAY[]::varchar[]) AS role_codes
                """
                + BASE_FROM
                + whereClause
                + groupBy
                + orderByClause(criteria.sortField())
                + """
                LIMIT :limit OFFSET :offset
                """;

        params.addValue("limit", criteria.size());
        params.addValue("offset", (criteria.page() - 1) * criteria.size());

        List<RbacUserListItem> items = jdbcTemplate.query(sql, params, this::mapRow);

        return new RbacUserListPagedResult(
                items,
                criteria.page(),
                criteria.size(),
                totalItems,
                totalPages,
                hasNext
        );
    }

    private long countUsers(String whereClause, MapSqlParameterSource params) {
        String sql = "SELECT COUNT(DISTINCT u.id) " + BASE_FROM + whereClause;
        Long count = jdbcTemplate.queryForObject(sql, params, Long.class);
        return count == null ? 0L : count;
    }

    private MapSqlParameterSource buildParams(RbacUserListCriteria criteria) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("deletedStatus", "DELETED");

        criteria.status().ifPresent(status -> params.addValue("status", status));
        criteria.emailFragment().ifPresent(fragment ->
                params.addValue("emailFragment", "%" + fragment.toLowerCase() + "%")
        );

        return params;
    }

    private String buildWhereClause(RbacUserListCriteria criteria) {
        StringBuilder clause = new StringBuilder(" WHERE u.status <> ")
                .append(sqlDialect.castEnum("deletedStatus", JdbcPgEnumTypes.USER_STATUS));

        if (criteria.status().isPresent()) {
            clause.append(" AND u.status = ")
                    .append(sqlDialect.castEnum("status", JdbcPgEnumTypes.USER_STATUS));
        }

        if (criteria.emailFragment().isPresent()) {
            clause.append(" AND u.email_normalized LIKE :emailFragment");
        }

        return clause.toString();
    }

    private String orderByClause(RbacUserListSortField sortField) {
        return switch (sortField) {
            case EMAIL -> " ORDER BY u.email_normalized ASC, u.id ASC ";
            case DISPLAY_NAME ->
                    " ORDER BY COALESCE(NULLIF(TRIM(p.display_name), ''), u.email) ASC, u.email_normalized ASC, u.id ASC ";
            case CREATED_AT -> " ORDER BY u.created_at DESC, u.id DESC ";
            case STATUS -> " ORDER BY u.status ASC, u.email_normalized ASC, u.id ASC ";
        };
    }

    private RbacUserListItem mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new RbacUserListItem(
                (UUID) rs.getObject("id"),
                rs.getString("email"),
                rs.getString("display_name"),
                rs.getString("status"),
                readRoleCodes(rs),
                toInstant(rs.getTimestamp("created_at"))
        );
    }

    private List<String> readRoleCodes(ResultSet rs) throws SQLException {
        var array = rs.getArray("role_codes");
        if (array == null) {
            return List.of();
        }
        String[] values = (String[]) array.getArray();
        if (values == null || values.length == 0) {
            return List.of();
        }
        return Arrays.stream(values)
                .filter(value -> value != null && !value.isBlank())
                .sorted()
                .toList();
    }

    private Instant toInstant(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toInstant();
    }
}
