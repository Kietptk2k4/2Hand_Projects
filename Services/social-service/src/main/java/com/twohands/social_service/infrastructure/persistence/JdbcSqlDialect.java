package com.twohands.social_service.infrastructure.persistence;

import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.SQLException;

@Component
public class JdbcSqlDialect {

    private final boolean postgres;

    public JdbcSqlDialect(DataSource dataSource) {
        this.postgres = isPostgres(dataSource);
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

    private static boolean isPostgres(DataSource dataSource) {
        try (var connection = dataSource.getConnection()) {
            return "PostgreSQL".equalsIgnoreCase(connection.getMetaData().getDatabaseProductName());
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to detect database dialect", ex);
        }
    }
}
