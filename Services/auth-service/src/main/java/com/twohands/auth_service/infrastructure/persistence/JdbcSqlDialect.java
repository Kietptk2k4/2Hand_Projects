package com.twohands.auth_service.infrastructure.persistence;

import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.SQLException;

/**
 * Builds JDBC SQL fragments that differ between PostgreSQL (native enums/jsonb) and H2 test schema (VARCHAR/CLOB).
 */
@Component
public class JdbcSqlDialect {

    private final boolean postgres;

    public JdbcSqlDialect(DataSource dataSource) {
        this.postgres = isPostgres(dataSource);
    }

    public boolean isPostgres() {
        return postgres;
    }

    public String castEnum(String paramName, String pgEnumType) {
        if (postgres) {
            return "CAST(:" + paramName + " AS " + pgEnumType + ")";
        }
        return ":" + paramName;
    }

    public String castJsonb(String paramName) {
        if (postgres) {
            return "CAST(:" + paramName + " AS jsonb)";
        }
        return ":" + paramName;
    }

    public String jsonColumnAsText(String columnName) {
        if (postgres) {
            return columnName + "::text";
        }
        return columnName;
    }

    private static boolean isPostgres(DataSource dataSource) {
        try (var connection = dataSource.getConnection()) {
            return "PostgreSQL".equalsIgnoreCase(connection.getMetaData().getDatabaseProductName());
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to detect database dialect", ex);
        }
    }
}
