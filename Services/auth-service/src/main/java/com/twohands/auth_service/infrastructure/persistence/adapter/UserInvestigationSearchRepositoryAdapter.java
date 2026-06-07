package com.twohands.auth_service.infrastructure.persistence.adapter;

import com.twohands.auth_service.domain.user.InvestigationUserSearchItem;
import com.twohands.auth_service.domain.user.UserInvestigationSearchRepository;
import com.twohands.auth_service.infrastructure.persistence.JdbcPgEnumTypes;
import com.twohands.auth_service.infrastructure.persistence.JdbcSqlDialect;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Repository
public class UserInvestigationSearchRepositoryAdapter implements UserInvestigationSearchRepository {

	private final NamedParameterJdbcTemplate jdbcTemplate;
	private final JdbcSqlDialect sqlDialect;

	public UserInvestigationSearchRepositoryAdapter(
			NamedParameterJdbcTemplate jdbcTemplate,
			JdbcSqlDialect sqlDialect
	) {
		this.jdbcTemplate = jdbcTemplate;
		this.sqlDialect = sqlDialect;
	}

	@Override
	public List<InvestigationUserSearchItem> searchByEmailFragment(String emailFragment, int limit) {
		String sql = """
				SELECT
				    u.id,
				    u.email,
				    u.status,
				    COALESCE(NULLIF(TRIM(p.display_name), ''), u.email) AS display_name,
				    COALESCE(ARRAY_AGG(DISTINCT r.code) FILTER (WHERE r.code IS NOT NULL), ARRAY[]::varchar[]) AS role_codes
				FROM users u
				LEFT JOIN user_profiles p ON p.user_id = u.id
				LEFT JOIN user_roles ur ON ur.user_id = u.id
				LEFT JOIN roles r ON r.id = ur.role_id
				WHERE u.status <> %s
				  AND u.email_normalized LIKE :emailFragment
				GROUP BY u.id, u.email, u.status, p.display_name
				ORDER BY u.created_at DESC
				LIMIT :limit
				""".formatted(sqlDialect.castEnum("deletedStatus", JdbcPgEnumTypes.USER_STATUS));

		return jdbcTemplate.query(
				sql,
				new MapSqlParameterSource()
						.addValue("deletedStatus", "DELETED")
						.addValue("emailFragment", "%" + emailFragment + "%")
						.addValue("limit", limit),
				this::mapRow
		);
	}

	@Override
	public List<InvestigationUserSearchItem> findByUserId(UUID userId) {
		String sql = """
				SELECT
				    u.id,
				    u.email,
				    u.status,
				    COALESCE(NULLIF(TRIM(p.display_name), ''), u.email) AS display_name,
				    COALESCE(ARRAY_AGG(DISTINCT r.code) FILTER (WHERE r.code IS NOT NULL), ARRAY[]::varchar[]) AS role_codes
				FROM users u
				LEFT JOIN user_profiles p ON p.user_id = u.id
				LEFT JOIN user_roles ur ON ur.user_id = u.id
				LEFT JOIN roles r ON r.id = ur.role_id
				WHERE u.id = :userId
				  AND u.status <> %s
				GROUP BY u.id, u.email, u.status, p.display_name
				""".formatted(sqlDialect.castEnum("deletedStatus", JdbcPgEnumTypes.USER_STATUS));

		return jdbcTemplate.query(
				sql,
				new MapSqlParameterSource()
						.addValue("userId", userId)
						.addValue("deletedStatus", "DELETED"),
				this::mapRow
		);
	}

	private InvestigationUserSearchItem mapRow(ResultSet rs, int rowNum) throws SQLException {
		return new InvestigationUserSearchItem(
				(UUID) rs.getObject("id"),
				rs.getString("email"),
				rs.getString("display_name"),
				rs.getString("status"),
				readRoleCodes(rs)
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
}
