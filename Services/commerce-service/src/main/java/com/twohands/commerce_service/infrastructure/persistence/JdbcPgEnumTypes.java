package com.twohands.commerce_service.infrastructure.persistence;

/**
 * PostgreSQL custom enum type names used in {@code CAST(:param AS type_name)} for JDBC bindings.
 */
public final class JdbcPgEnumTypes {

    public static final String OUTBOX_STATUS = "outbox_status";

    private JdbcPgEnumTypes() {
    }
}
